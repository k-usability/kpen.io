package io.kyaml;

import com.google.gson.Gson;
import io.kyaml.model.If;
import io.kyaml.model.KYaml;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TranslateDeserialize {

    public static void main(String[] args) throws IOException {
        for (String kyamlFile : new String[] {"/home/sbugrara/k-examples/./evm/call/call00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/bytes/bytes00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/requires/requires00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/simple/simple00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/simple/simple02/spec.yaml",
                "/home/sbugrara/k-examples/./evm/keccak/encodepacked-keccak01-0.4.24/spec.yaml",
                "/home/sbugrara/k-examples/./evm/staticarray/staticarray00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/overflow/overflow00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/ecrecover/ecrecoverloop00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/ecrecover/ecrecover00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/ecrecover/ecrecoverloop01/spec.yaml",
                "/home/sbugrara/k-examples/./evm/storage/storage02/spec.yaml",
                "/home/sbugrara/k-examples/./evm/storage/storage01/spec.yaml",
                "/home/sbugrara/k-examples/./evm/storage/storage00/spec.yaml",
                "/home/sbugrara/k-examples/./evm/staticloop/staticloop00/spec.yaml",
        }) {
            System.out.println("KYAML: " + kyamlFile);
            Constructor c = new Constructor(KYaml[].class);
            TypeDescription matchDesc = new TypeDescription(If.class);
            matchDesc.putMapPropertyType("match", String.class, String.class);
            c.addTypeDescription(matchDesc);
            c.setPropertyUtils(new PropertyUtils() {
                @Override
                public Property getProperty(Class<? extends Object> type, String name)  {
                    if (name.equals("if")) {
                        name = "ifb";
                    } else if (name.equals("then")) {
                        name = "thenb";
                    }

                    Property p = super.getProperty(type, name);
                    System.out.println(type + " " + name + " " + p);
                    return p;
                }
            });
            Yaml yaml = new Yaml(c);
            KYaml[] specs = yaml.load(new FileInputStream(new File(kyamlFile)));
            String json = new Gson().toJson(specs);
            System.out.println(json);

            //String s = specs[0].getSpec().getIfb().match.toString();
//            String s1 = s.replaceAll("\\\\#", "#");

  //          System.out.println(s1);

            for (KYaml sp : specs) {


            }
        }
    }
}
