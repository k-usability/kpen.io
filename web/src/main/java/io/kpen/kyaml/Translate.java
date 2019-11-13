package io.kpen.kyaml;

import io.kspec.ExampleConfig;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;

public class Translate {

    public static void main(String[] args) throws IOException {
        String kyamlFile = "/home/sbugrara/k-examples/evm/simple00/spec.yaml";
        File outputDir = new File("/home/sbugrara/kyaml-generated");
        FileUtils.deleteDirectory(outputDir);
        FileUtils.forceMkdir(outputDir);


        Yaml yaml = new Yaml(new Constructor(ExampleConfig.class));

    }
}
