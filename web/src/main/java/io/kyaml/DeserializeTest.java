package io.kyaml;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.gson.Gson;
import io.kyaml.model.KYaml;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class DeserializeTest {

    public static void main(String[] args) throws IOException {
        //String specStr = FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/ecrecover/ecrecoverloop01/spec.yaml"), Charset.defaultCharset());
        String specStr = FileUtils.readFileToString(new File("/home/sbugrara/kpen.io/web/data/ktmpl/resources/root.yaml"), Charset.defaultCharset());
        List<String> errors = Validate.validate(specStr);
        if (!errors.isEmpty()) {
            System.out.println("Invalid spec: " + errors);
            throw new RuntimeException();
        }
        KYaml kyaml = new Deserialize(specStr).run();
        Map<String,Object> cells = new Translate(kyaml).toCellMap();

        FileTemplateLoader loader = new FileTemplateLoader("web/src/main/resources");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile("evm-spec-tmpl-yaml.k");
        String s = template.apply(cells);
        System.out.println(s);
    }
}
