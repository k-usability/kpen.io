package io.kyaml;

import io.kyaml.model.KYaml;
import io.kyaml.model.Spec;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Translate {

    class Visitor {

        public KYaml visit(Object o) throws IllegalArgumentException {
            if (o instanceof List) {
                KYaml kyaml = new KYaml();
                List l = (List) o;
                for (Object os : l) {
                    Spec spec = visitSpec(os);
                    kyaml.specs.add(spec);
                }
                return kyaml;
            }

            throw new IllegalArgumentException("KYaml must be a list of spec blocks at the top level");
        }

        public Map<String,Object> map(Object o, String... properties) {
            if (o instanceof Map) {
                Set<String> propset = new HashSet(Arrays.asList(properties));
                Map<String,Object> m = (Map) o;
                for (String k : m.keySet()) {
                    if (!propset.contains(k)) {
                        throw new IllegalArgumentException("Must be a map with only the properties " + properties);
                    }
                }
                return m;
            }
            throw new IllegalArgumentException("Must be a map with only the properties " + properties);
        }

        public Spec visitSpec(Object o) {
            Map<String,Object> spec = map(o, "spec");
            Map<String,Object> ifb = map(spec, "if", "then");

            throw new IllegalArgumentException("Spec block must be specified with the \"spec\" property.");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        for (String kyamlFile : new String[] {
                "/home/sbugrara/k-examples/./evm/overflow/overflow00/spec.yaml",
        }) {
            System.out.println("KYAML: " + kyamlFile);
            Yaml yaml = new Yaml();
            Object o = yaml.load(new FileInputStream(new File(kyamlFile)));
            if (o instanceof List) {

            } else {

            }

        }
    }
}
