package io.kyaml;

import io.kyaml.model.KRule;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Translate {
    private KYaml ky;

    public Translate(KYaml kyaml) {
        this.ky = kyaml;
    }

    public String clean(Object o) {
        return Objects.toString(o).replaceAll("\\\\#", "#");
    }

    public List<KRule> toKRules() {
        List<KRule> res = new ArrayList();
        for (Rule r : ky.rules) {
            KRule k = new KRule();
            for (Map.Entry<String,String> e : r.ifb.match.entrySet()) {
                k.cells.put(e.getKey(), clean(e.getValue()));
            }

            for (Map.Entry<String,String> e : r.thenb.match.entrySet()) {
                String cell = e.getKey();
                String value = clean(e.getValue());

                if (k.cells.containsKey(cell)) {
                    Object ifvalue = k.cells.get(cell);
                    k.cells.put(cell, ifvalue + " => " + value);
                } else {
                    k.cells.put(cell, "_ => " + value);
                }
            }
            res.add(k);
        }
        return res;
    }
}
