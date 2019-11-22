package io.kyaml;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.gson.Gson;
import io.kyaml.model.KRule;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String progSpecStr = FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/ecrecover/ecrecoverloop01/spec.yaml"), Charset.defaultCharset());
        errors = Validate.validate(progSpecStr);
        if (!errors.isEmpty()) {
            System.out.println("Invalid spec: " + errors);
            throw new RuntimeException();
        }
        KYaml mainkyaml = new Deserialize(progSpecStr).run();

        Rule root = rootKYaml.rules.get(0);
        List<Rule> rules = new ArrayList();
        rules.add(root);
        rules.addAll(mainkyaml.rules);

        Map<Rule, ArrayList<Rule>> inheritedBy = new HashMap();
        HashMap<String,Rule> name2rule = new HashMap();
        for (Rule r : rules) {
            if (r.name != null) {
                name2rule.put(r.name, r);
            }
            inheritedBy.put(r, new ArrayList());
        }
        inheritedBy.get(root).addAll(mainkyaml.rules);


        for (Rule r : mainkyaml.rules) {
            if (r.inherits != null) {
                Rule parent = name2rule.get(r.inherits);
                inheritedBy.get(parent).add(r);
            }
        }

        List<Rule> leaves = new ArrayList();
        for (Rule r : mainkyaml.rules) {
            if (inheritedBy.get(r).isEmpty()) {
                System.out.println("LEAF: " + r.name);
                leaves.add(r);
            }
        }

        FileTemplateLoader loader = new FileTemplateLoader("web/src/main/resources");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile("evm-spec-tmpl-yaml.k");

        for (KRule r : new Translate(mainkyaml).toKRules()) {
            System.out.println(r.cells);
            String s = template.apply(r.cells);
            System.out.println(s);
        }
    }
}
