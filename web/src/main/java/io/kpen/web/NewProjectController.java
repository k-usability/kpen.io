package io.kpen.web;

import io.github.cdimascio.dotenv.Dotenv;
import io.kpen.jooq.tables.records.JobRecord;
import io.kpen.jooq.tables.records.PersonRecord;
import io.kpen.jooq.tables.records.ProjectRecord;
import io.kpen.util.Auth;
import io.kpen.util.S3;
import io.kpen.util.Tx;
import io.kyaml.KRuleGenerator;
import io.sentry.Sentry;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;

import static io.kpen.jooq.Tables.*;
import static io.kpen.web.BytecodeCompiler.*;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3010", "https://kpen.io"})
@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class NewProjectController {
    public static final String KTMPL_DIR = "data/ktmpl";
    public static final String PROGRAM_FILE_NAME = "program.sol";
    public static final String SPEC_FILE_NAME = "spec.yaml";

    @PostMapping(value = "/project")
    public NewProjectResp newProject(Authentication authentication, @RequestBody final NewProjectReq req) {
        return Tx.run(ctx -> newProject(ctx, req, Auth.getUser(authentication)));
    }

    public NewProjectResp newProject(DSLContext ctx, NewProjectReq req, Auth.User user) throws IOException {
        NewProjectResp resp = new NewProjectResp();

        Dotenv dotenv = Dotenv.load();

        System.out.println(req.getProgramText());
        System.out.println(req.getSpecText());
        String spacePath = "/tmp/kpen-" + UUID.randomUUID();
        String codePath = spacePath + "/program";

        File ktmplDir = new File(KTMPL_DIR);
        File spaceDir = new File(spacePath);
        File generatedDir = new File(codePath + "/generated");
        File resourcesDir = new File(spacePath + "/resources");
        File programFile = new File(codePath + "/" + PROGRAM_FILE_NAME);
        File specYamlFile = new File(codePath + "/" + SPEC_FILE_NAME);

        generatedDir.mkdir();

        System.out.println(spacePath);
        System.out.println(ktmplDir.getAbsolutePath());
        System.out.println("exists=" + ktmplDir.exists() + " dir=" + ktmplDir.isDirectory());
        System.out.println("Copying from " + ktmplDir.getAbsolutePath() + " to " + spaceDir.getAbsolutePath());

        FileUtils.copyDirectory(ktmplDir, spaceDir);

        FileUtils.write(programFile, req.getProgramText(), Charset.defaultCharset());
        CompilationResult compilationResult = getBytecode(programFile, generatedDir);

        FileUtils.write(specYamlFile, req.getSpecText(), Charset.defaultCharset());


        for (String libk : new String[]{
                "verification.k",
                "abstract-semantics.k",
                "abstract-semantics-segmented-gas.k",
                "evm-symbolic.k",
                "evm-data-map-symbolic.k",
                "ecrec-symbolic.k",
                "lemmas.k",
                "edsl-static-array.k",
                "evm.smt2"}) {
            File libf = getFile(resourcesDir, libk);
            copyFileToDirectory(libf, generatedDir);
        }

        File rootFileYaml = new File(KTMPL_DIR + "/resources/root.yaml");
        File kruleTemplateFile = new File(KTMPL_DIR + "/resources/evm-spec-tmpl-yaml.k.hbs");

        KRuleGenerator gen = new KRuleGenerator()
                .setRootYamlFile(rootFileYaml)
                .setOutputDir(generatedDir)
                .setSpecYamlFile(specYamlFile)
                .setKRuleTemplateFile(kruleTemplateFile)
                .addProperty("contractCode", "\"" + compilationResult.getBytecodeHex() + "\"");

        KRuleGenerator.Result kruleResult = gen.run();
        if (kruleResult.isSuccess()) {
            System.out.println(StringUtils.join(kruleResult.getKRuleFiles(), "\n"));
        }

        PersonRecord person = ctx.fetchOne(PERSON, PERSON.AUTH0_SUB.eq(user.getSub()));
        if (person == null) {
            person = ctx.newRecord(PERSON);
            person.setIsAdmin(false);
            person.setAuth0Sub(user.getSub());
            person.setName(user.getName());
            person.setEmail(user.getEmail());
            person.insert();
        }

        String bucketName = dotenv.get("APP_JOB_BUCKET");
        String bucketKey = PROGRAM_FILE_NAME + "-" + UUID.randomUUID().toString();

        S3.uploadDir(bucketName, bucketKey, Paths.get(codePath));

        OffsetDateTime now = OffsetDateTime.now();
        ProjectRecord project = ctx.newRecord(PROJECT)
                .setCreationDt(now)
                .setName(PROGRAM_FILE_NAME)
                .setS3Bucket(bucketName)
                .setS3Key(bucketKey)
                .setProgramFilename(PROGRAM_FILE_NAME)
                .setSpecFilename(SPEC_FILE_NAME)
                .setUserId(person.getId());

        if (!compilationResult.isSuccess()) {
            project.setCompilationErrorMessage(compilationResult.getErrorMessage());
            project.setIsCompilationError(true);
        } else if (!kruleResult.isSuccess()) {
            project.setCompilationErrorMessage(kruleResult.getErrorMessage());
            project.setIsCompilationError(true);
        } else {
            project.setCompilationErrorMessage(null);
            project.setIsCompilationError(false);
        }

        project.insert();
        resp.setProjectId(project.getId());

        for (File speck : new File(codePath + "/generated").listFiles()) {
            if (!speck.getName().endsWith("spec.k")) continue;

            JobRecord job = ctx.newRecord(JOB)
                    .setRequestDt(now)
                    .setBenchmarkName(PROGRAM_FILE_NAME)
                    .setSpecFilename(speck.getName())
                    .setKprove("k")
                    .setSemantics("evm-semantics")
                    .setS3Bucket(bucketName)
                    .setS3Key(bucketKey)
                    .setSpecName(speck.getName().replace("-spec.k", ""))
                    .setTimeoutSec(600)
                    .setMemlimitMb(3000)
                    .setTimedOut(false)
                    .setProjectId(project.getId());
            job.insert();
        }


        Sentry.capture("new project: https://kpen.io/project/" + resp.projectId);
        System.out.println("Called sentry");

        return resp;
    }

    private static File getFile(File parent, String child) {
        return new File(parent.getAbsoluteFile() + "/" + child);
    }

    @Data
    public static class NewProjectReq {
        private String programText;
        private String specText;
    }

    @Data
    public static class NewProjectResp {
        private Integer projectId;
    }
}
