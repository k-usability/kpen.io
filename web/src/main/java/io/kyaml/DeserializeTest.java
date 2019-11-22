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
        File specFileYaml = new File("/home/sbugrara/k-examples/evm/overflow/overflow00/spec.yaml");
        File kruleTemplateFile = new File("/home/sbugrara/kpen.io/web/data/ktmpl/resources/evm-spec-tmpl-yaml.k.hbs");
        String code = "\"0x608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063fe0d94c1146044575b600080fd5b348015604f57600080fd5b50607960048036036020811015606457600080fd5b8101908080359060200190929190505050608f565b6040518082815260200191505060405180910390f35b6000806001830190508091505091905056fea165627a7a72305820fbb52294b87f58c290f1b4a624b5152e0dd074db32048a743b8ff993cc4cf6180029\"";

        KRuleGenerator gen = new KRuleGenerator()
                .setRootYamlFile(rootFileYaml)
                .setOutputDir(outputDir)
                .setSpecYamlFile(specFileYaml)
                .setKRuleTemplateFile(kruleTemplateFile)
                .addProperty("{CODE}", code);

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
