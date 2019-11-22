package io.kyaml;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.gson.Gson;
import io.kyaml.model.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class DeserializeTest {

    public static void main(String[] args) throws IOException {
        //String specStr = FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/ecrecover/ecrecoverloop01/spec.yaml"), Charset.defaultCharset());
        String rootSpecStr = FileUtils.readFileToString(new File("/home/sbugrara/kpen.io/web/data/ktmpl/resources/root.yaml"), Charset.defaultCharset());
        List<String> errors = Validate.validate(rootSpecStr);
        if (!errors.isEmpty()) {
            System.out.println("Invalid spec: " + errors);
            throw new RuntimeException();
        }
        KYaml rootKYaml = new Deserialize(rootSpecStr).run();

        //String progSpecStr = FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/ecrecover/ecrecoverloop01/spec.yaml"), Charset.defaultCharset());
        String progSpecStr = FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/overflow/overflow00/spec.yaml"), Charset.defaultCharset());
        errors = Validate.validate(progSpecStr);
        if (!errors.isEmpty()) {
            System.out.println("Invalid spec: " + errors);
            throw new RuntimeException();
        }
        KYaml mainkyaml = new Deserialize(progSpecStr).run();

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

        FileTemplateLoader loader = new FileTemplateLoader("web/src/main/resources");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile("evm-spec-tmpl-yaml.k");

        for (Rule r : finalRules) {
            KRule kr = new Translate(r).toKRule();
            String s = template.apply(kr.cells);
            FileUtils.writeStringToFile(new File("/home/sbugrara/kevm-verify-benchmarks/0-overflow00-0.5.0/generated/" + r.name + ".k"), s, Charset.defaultCharset());
        }
    }

    public static void overwriteWith(Rule current, Rule element) {
        current.ifb.match.putAll(element.ifb.match);
        current.ifb.where.and.addAll(element.ifb.where.and);
        current.thenb.match.putAll(element.thenb.match);
        current.thenb.where.and.addAll(element.thenb.where.and);
    }
}
