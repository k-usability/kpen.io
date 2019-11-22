package io.kyaml;

import io.kyaml.model.Constraint;
import io.kyaml.model.KRule;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Translate {
    private KYaml ky;

    public Translate(KYaml kyaml) {
        this.ky = kyaml;
    }

    public Object clean(Object o) {
        if (o instanceof String) {
            return ((String) o).replaceAll("\\\\#", "#");
        } else if (o instanceof List) {
            ArrayList l = new ArrayList();
            for (Object e : (List) o) {
                l.add(clean(e));
            }
            return l;
        } else if (o instanceof Map) {
            HashMap<String,Object> m = new HashMap();
            HashMap<String,Object> i = (HashMap<String,Object>) o;
            for (String k : i.keySet()) {
                m.put(k, clean(i.get(k)));
            }
            return m;
        } else if (o instanceof Integer || o instanceof Boolean || o instanceof Double) {
            return Objects.toString(o);
        } else {
            throw new RuntimeException("Cant handle type: " + o.getClass());
        }
    }

    public List<KRule> toKRules() {
        List<KRule> res = new ArrayList();
        for (Rule r : ky.rules) {
            KRule k = new KRule();
            for (Map.Entry<String,String> e : r.ifb.match.entrySet()) {
                k.cells.put(e.getKey(), clean(e.getValue()));
            }

            k.cells.put("requires", clean(toKConstraint(r.ifb.where, "")));

            for (Map.Entry<String,String> e : r.thenb.match.entrySet()) {
                String cell = e.getKey();
                Object value = clean(e.getValue());

                if (k.cells.containsKey(cell)) {
                    Object ifvalue = k.cells.get(cell);
                    k.cells.put(cell, ifvalue + " => " + value);
                } else {
                    k.cells.put(cell, "_ => " + value);
                }
            }

            k.cells.put("ensures", clean(toKConstraint(r.thenb.where, "")));

            res.add(k);
        }

        return res;
    }

    public String toKConstraint(Constraint c, String tabs) {
        tabs += "\t";
        if (c instanceof Constraint.And) {
            List<String> l = new ArrayList();
            Constraint.And a = (Constraint.And) c;
            if (a.and.isEmpty()) {
                return "true";
            }
            for (Constraint ac : a.and) {
                l.add(tabs + "andBool " + toKConstraint(ac, tabs));
            }
            return "(" + StringUtils.join(l, "\n") + ")";
        } else if (c instanceof Constraint.Or) {
            List<String> l = new ArrayList();
            Constraint.Or a = (Constraint.Or) c;
            if (a.or.isEmpty()) {
                return "false";
            }
            for (Constraint oc : a.or) {
                l.add(tabs + "orBool " + toKConstraint(oc, tabs));
            }
            return "(" + StringUtils.join(l, "\n") + ")";
        } else if (c instanceof Constraint.Not) {
            Constraint.Not n = (Constraint.Not) c;
            String value = "notBool " + toKConstraint(n.not, tabs);
            return "(" + value + ")";
        } else if (c instanceof Constraint.Predicate) {
            Constraint.Predicate p = (Constraint.Predicate) c;
            return clean(p.predicate).toString();
        } else {
            throw new RuntimeException();
        }
    }
}
