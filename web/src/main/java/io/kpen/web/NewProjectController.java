package io.kpen.web;

import io.github.cdimascio.dotenv.Dotenv;
import io.kpen.jooq.tables.records.JobRecord;
import io.kpen.jooq.tables.records.PersonRecord;
import io.kpen.jooq.tables.records.ProjectRecord;
import io.kpen.util.Auth;
import io.kpen.util.Tx;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;

import static io.kpen.jooq.Tables.*;
import static io.kpen.web.BytecodeCompiler.*;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3010", "https://kpen.io"})
@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class NewProjectController {
    public static final String KTMPL_DIR = "data/ktmpl";
    public static final String SPEC_MIN_INI_TOKEN = "%SPEC.MIN.INI%";
    public static final String PROGRAM_FILE_NAME = "program.sol";
    public static final String SPEC_MIN_FILE_NAME = "spec.min.ini";
    public static final String SPEC_FULL_FILE_NAME = "spec.ini";

    @PostMapping(value = "/project")
    public NewProjectResp newProject(Authentication authentication, @RequestBody final NewProjectReq req) throws Throwable {
        return Tx.runex(ctx -> newProject(ctx, req, Auth.getUser(authentication)));
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
        File specMinFile = new File(codePath + "/" + SPEC_MIN_FILE_NAME);
        File specFullFile = new File(codePath + "/" + SPEC_FULL_FILE_NAME);

        generatedDir.mkdir();

        System.out.println(spacePath);
        System.out.println(ktmplDir.getAbsolutePath());
        System.out.println("exists=" + ktmplDir.exists() + " dir=" + ktmplDir.isDirectory());
        System.out.println("Copying from " + ktmplDir.getAbsolutePath() + " to " + spaceDir.getAbsolutePath());

        FileUtils.copyDirectory(ktmplDir, spaceDir);

        FileUtils.write(programFile, req.getProgramText(), Charset.defaultCharset());
        CompilationResult compilationResult = getBytecode(programFile, generatedDir);

        FileUtils.write(specMinFile, req.getSpecText(), Charset.defaultCharset());


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

        String specsetTmpl = FileUtils.readFileToString(new File(ktmplDir + "/resources/specset-tmpl.ini"), Charset.defaultCharset());
        String specFull = specsetTmpl.replaceAll(SPEC_MIN_INI_TOKEN, req.getSpecText());
        if (compilationResult.isSuccess()) {
            specFull = specFull.replaceAll("contract_code:", "contract_code: \"" + compilationResult.getBytecodeHex() + "\"");
        }
        FileUtils.write(specFullFile, specFull, Charset.defaultCharset());

        String genpyPath = getFile(resourcesDir, "gen-spec.py").getAbsolutePath();
        String moduleTmpl = getFile(resourcesDir, "module-tmpl.k").getAbsolutePath();
        String specTmpl = getFile(resourcesDir, "spec-tmpl.k").getAbsolutePath();

        if (compilationResult.isSuccess()) {
            Ini ini = Ini.parse(specFull);
            for (Ini.Section section : ini.getLeaves()) {
                String[] cmd = new String[]{
                        dotenv.get("APP_PY3"),
                        genpyPath,
                        moduleTmpl,
                        specTmpl,
                        specFullFile.getAbsolutePath(),
                        section.getName(),
                        section.getName()
                };
                String cmdstr = StringUtils.join(cmd, " ");
                //cmdstr = "/usr/bin/python3 -h";
                System.out.println(cmdstr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                CommandLine cmdLine = CommandLine.parse(cmdstr);
                DefaultExecutor executor = new DefaultExecutor();
                executor.setWorkingDirectory(new File(codePath));
                executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));
                int exitValue = executor.execute(cmdLine);
                if (exitValue != 0) {
                    throw new RuntimeException("Spec generation failed");
                }
                String kFilename = section.getName() + "-spec.k";
                File kFile = getFile(generatedDir, kFilename);
                String speck = outputStream.toString();
                System.out.println("Writing to K file: " + kFile.getAbsolutePath());
                FileUtils.writeStringToFile(kFile, speck, Charset.defaultCharset());
            }
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

        String bucketName = "kjob";
        String bucketKey = PROGRAM_FILE_NAME + "-" + UUID.randomUUID().toString();

        OffsetDateTime now = OffsetDateTime.now();
        ProjectRecord project = ctx.newRecord(PROJECT)
                .setCreationDt(now)
                .setName(PROGRAM_FILE_NAME)
                .setS3Bucket(bucketName)
                .setS3Key(bucketKey)
                .setProgramFilename(PROGRAM_FILE_NAME)
                .setSpecFilename(SPEC_MIN_FILE_NAME)
                .setIsCompilationError(!compilationResult.isSuccess())
                .setCompilationErrorMessage(compilationResult.getErrorMessage())
                .setUserId(person.getId());
        project.insert();
        resp.setProjectId(project.getId());

        S3 s3 = new S3();
        s3.uploadDir(bucketName, bucketKey, Paths.get(codePath));

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
