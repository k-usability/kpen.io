package io.kyaml;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.gson.Gson;
import io.kyaml.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class DeserializeTest {

    public static void main(String[] args) throws IOException {
        File rootFileYaml = new File("/home/sbugrara/kpen.io/web/data/ktmpl/resources/root.yaml");
        File outputDir = new File("/home/sbugrara/kevm-verify-benchmarks/0-overflow00-0.5.0/generated/");
        File specFileYaml = new File("/home/sbugrara/k-examples/evm/simple/simple00/spec.yaml");
        File kruleTemplateFile = new File("/home/sbugrara/kpen.io/web/data/ktmpl/resources/evm-spec-tmpl-yaml.k.hbs");

        KRuleGenerator gen = new KRuleGenerator()
                .setRootYamlFile(rootFileYaml)
                .setOutputDir(outputDir)
                .setSpecYamlFile(specFileYaml)
                .setKRuleTemplateFile(kruleTemplateFile);

        List<File> kruleFiles = gen.run();
        System.out.println(StringUtils.join(kruleFiles, "\n"));
    }

    public static void overwriteWith(Rule current, Rule element) {
        current.ifb.match.putAll(element.ifb.match);
        current.ifb.where.and.addAll(element.ifb.where.and);
        current.thenb.match.putAll(element.thenb.match);
        current.thenb.where.and.addAll(element.thenb.where.and);
    }
}
