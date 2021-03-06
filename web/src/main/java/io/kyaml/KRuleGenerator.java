package io.kyaml;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.kpen.web.BytecodeCompiler;
import io.kyaml.model.KRule;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class KRuleGenerator {
    private File rootFile;
    private File specFile;
    private File outputDir;
    private File kruleTemplateFile;
    private HashMap<String,String> properties = new HashMap();

    public KRuleGenerator addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public KRuleGenerator setRootYamlFile(File rootFile) {
        this.rootFile = rootFile;
        return this;
    }

    public KRuleGenerator setOutputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public KRuleGenerator setSpecYamlFile(File specFile) {
        this.specFile = specFile;
        return this;
    }

    public KRuleGenerator setKRuleTemplateFile(File kruleTemplateFile) {
        this.kruleTemplateFile = kruleTemplateFile;
        return this;
    }

    @Data
    public static class Result {
        private boolean success;
        private String errorMessage;
        private List<File> kRuleFiles;
    }

    public Result run() throws KYamlException, IOException {
        String rootSpecStr = FileUtils.readFileToString(rootFile, Charset.defaultCharset());
        List<String> errors = Validate.validate(rootSpecStr);
        if (!errors.isEmpty()) {
            System.out.println("Root invalid spec: " + errors);
            Result res = new Result();
            res.errorMessage = StringUtils.join(errors, " ");
            return res;
        }
        KYaml rootKYaml = new Deserialize(rootSpecStr)
                .run();

        String progSpecStr = FileUtils.readFileToString(specFile, Charset.defaultCharset());
        errors = Validate.validate(progSpecStr);
        if (!errors.isEmpty()) {
            System.out.println("Spec invalid: " + errors);
            Result res = new Result();
            res.errorMessage = StringUtils.join(errors, " ");
            return res;
        }
        KYaml mainkyaml = new Deserialize(progSpecStr)
                .run();

        Rule root = rootKYaml.rules.get(0);
        {
            HashMap<String, Rule> name2rule = new HashMap();
            for (Rule r : mainkyaml.rules) {
                if (r.name != null) {
                    name2rule.put(r.name, r);
                }
            }
            for (Rule r : mainkyaml.rules) {
                if (r.inherits == null) {
                    r.inheritsRule = root;
                } else {
                    r.inheritsRule = name2rule.get(r.inherits);
                }
            }
        }

        List<Rule> rules = new ArrayList();
        rules.addAll(mainkyaml.rules);
        rules.add(root);
        HashSet<Rule> internal = new HashSet();
        for (Rule r : rules) {
            if (r.inheritsRule != null) {
                internal.add(r.inheritsRule);
            }
        }
        System.out.println("Internals: " + internal);

        List<Rule> leaves = new ArrayList();
        for (Rule r : rules) {
            if (!internal.contains(r)) {
                System.out.println("LEAF: " + r.name + " " + r.inheritsRule + " " + r.inheritsRule.inheritsRule);
                leaves.add(r);
            }
        }

        int namei = 0;
        List<Rule> finalRules = new ArrayList();
        for (Rule leaf : leaves) {
            Rule current = new Rule();
            if (leaf.name == null) {
                current.name = "rule" + namei++;
            } else {
                current.name = leaf.name;
            }
            current.name += "-spec";
            finalRules.add(current);

            List<Rule> path = new ArrayList();
            Rule r = leaf;
            while (r != null) {
                path.add(0, r);
                r = r.inheritsRule;
            }
            System.out.println(leaf + "->" + path);
            for (Rule p : path) {
                overwriteWith(current, p);
            }
        }

        String templateStr = FileUtils.readFileToString(kruleTemplateFile, Charset.defaultCharset());
        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(templateStr);

        List<File> kruleFiles = new ArrayList();
        for (Rule r : finalRules) {
            KRule kr = new Translate(r)
                    .addProperties(properties)
                    .toKRule();

            HashMap<String,Object> kvmap = new HashMap();
            kvmap.putAll(properties);
            kvmap.putAll(kr.cells);
            System.out.println(kvmap);
            String s = template.apply(kvmap);
            File outFile = new File(outputDir.getAbsolutePath() + "/" + r.name + ".k");
            FileUtils.writeStringToFile(outFile, s, Charset.defaultCharset());
            kruleFiles.add(outFile);
        }

        Result res = new Result();
        res.success = true;
        res.kRuleFiles = kruleFiles;
        return res;
    }

    public static void overwriteWith(Rule current, Rule element) {
        current.ifb.match.putAll(element.ifb.match);
        current.ifb.where.and.addAll(element.ifb.where.and);
        current.thenb.match.putAll(element.thenb.match);
        current.thenb.where.and.addAll(element.thenb.where.and);
    }
}
