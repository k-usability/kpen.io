package io.kyaml;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.projectodd.yaml.Schema;
import org.projectodd.yaml.SchemaException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Validate {

    public static List<String> validate(String spec) {
        try {
            Schema schema = new Schema( Validate.class.getResourceAsStream("/kyaml-schema.yml"));
            schema.validate(IOUtils.toInputStream(spec, Charset.defaultCharset()));
        } catch(SchemaException e) {
            return Arrays.asList(e.getMessage());
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) throws IOException, SchemaException {
        List<String> res = Validate.validate(FileUtils.readFileToString(new File("/home/sbugrara/k-examples/evm/simple/simple00/spec.yaml"), Charset.defaultCharset()));
        System.out.println(res);
    }
}
