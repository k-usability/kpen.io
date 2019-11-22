package io.kyaml;

import io.kyaml.model.Constraint;
import io.kyaml.model.KRule;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Translate {
    private Rule rule;
    private HashMap<String,String> properties = new HashMap();

    public Translate(Rule rule) {
        this.rule = rule;
    }

    public Translate addProperties(Map<String,String> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public Object clean(Object o) {
        if (o instanceof String) {
            String res = ((String) o).replaceAll("\\\\#", "#");
            for (String key : properties.keySet()) {
                res = res.replace(key, properties.get(key));
            }
            return res;
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

    public KRule toKRule() {
        KRule k = new KRule();
        k.cells.put("rule-name", rule.name.toUpperCase());
        for (Map.Entry<String, String> e : rule.ifb.match.entrySet()) {
            k.cells.put(e.getKey(), clean(e.getValue()));
        }

        k.cells.put("requires", clean(toKConstraint(rule.ifb.where, "")));

        for (Map.Entry<String, String> e : rule.thenb.match.entrySet()) {
            String cell = e.getKey();
            Object value = clean(e.getValue());

            if (k.cells.containsKey(cell)) {
                Object ifvalue = k.cells.get(cell);
                k.cells.put(cell, ifvalue + " => " + value);
            } else {
                k.cells.put(cell, "_ => " + value);
            }
        }

        k.cells.put("ensures", clean(toKConstraint(rule.thenb.where, "")));

        return k;
    }

    public String toKConstraint(Constraint c, String tabs) {
        if (c == null) {
            return "true";
        }
        tabs += "\t";
        if (c instanceof Constraint.And) {
            List<String> l = new ArrayList();
            Constraint.And a = (Constraint.And) c;
            if (a.and.isEmpty()) {
                return "true";
            }
            boolean first = true;
            for (Constraint ac : a.and) {
                l.add(tabs + (first ? "" : "andBool ") + toKConstraint(ac, tabs));
                first = false;
            }
            return "(" + StringUtils.join(l, "\n") + ")";
        } else if (c instanceof Constraint.Or) {
            List<String> l = new ArrayList();
            Constraint.Or a = (Constraint.Or) c;
            if (a.or.isEmpty()) {
                return "false";
            }
            boolean first = true;
            for (Constraint oc : a.or) {
                l.add(tabs + (first ? "" : "orBool ")  + toKConstraint(oc, tabs));
                first = false;
            }
            return "(" + StringUtils.join(l, "\n") + ")";
        } else if (c instanceof Constraint.Not) {
            Constraint.Not n = (Constraint.Not) c;
            String value = "notBool " + toKConstraint(n.not, tabs);
            return value;
        } else if (c instanceof Constraint.Predicate) {
            Constraint.Predicate p = (Constraint.Predicate) c;
            return clean(p.predicate).toString();
        } else {
            throw new RuntimeException("Cannot handle: " + c);
        }
    }
}
