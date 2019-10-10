package io.kpen.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Data
@ToString
public class Ini {
    private HashMap<String,Section> sections = new HashMap();
    private List<Section> leaves = new ArrayList();
    private List<Section> specs = new ArrayList();
    private Section root;
    private Section pgm;

    @Data
    @ToString
    public static class Section {
        private String name;
        private Section parent;
        private boolean isLeaf;
    }

    public static Ini parse(String inistr) {
        Ini ini = new Ini();
        Section cur  = null;
        for (String line : StringUtils.split(inistr, "\n")) {
            line = line.trim();
            if (line.startsWith("[")) {
                cur = new Section();
                cur.setName(line.replace("[", "").replace("]", "").trim());
                ini.sections.put(cur.getName(), cur);
            }
        }

        ini.root = ini.sections.get("root");
        if (ini.root == null) {
            ini.root = new Section();
            ini.root.setName("root");
        }
        ini.pgm = ini.sections.get("pgm");
        if (ini.pgm == null) {
            ini.pgm = new Section();
            ini.pgm.setName("pgm");
        }
        ini.root.parent = ini.pgm;

        for (Section s : ini.sections.values()) {
            if (s != ini.root && s != ini.pgm) {
                ini.specs.add(s);
            }
        }

        Set<Section> internal = new HashSet();
        for (Section s : ini.specs) {
            List<String> components = new ArrayList(Arrays.asList(StringUtils.split(s.getName(), '-')));

            while (!components.isEmpty()) {
                String pop = components.remove(components.size()-1);
                String parent = StringUtils.join(components, "-");
                s.parent = ini.sections.get(parent);
                if (s.parent != null) break;
            }

            if (s.parent != null) {
                internal.add(s.parent);
            }
        }

        for (Section s : ini.specs) {
            if (!internal.contains(s)) {
                s.isLeaf = true;
                ini.leaves.add(s);
            }
        }

        for (Section s : ini.specs) {
            if (s.parent == null) {
                s.parent = ini.root;
            }
        }

        return ini;
    }

    public static void main(String[] args) throws IOException {
        String fname = "/home/sbugrara/kevm-verify-benchmarks/multisig13/spec.ini";
        String inistr = FileUtils.readFileToString(new File(fname), Charset.defaultCharset());
        Ini ini = Ini.parse(inistr);

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ini));

    }
}
