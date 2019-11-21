package io.kyaml;

import com.google.gson.Gson;
import io.kyaml.model.Constraint;
import io.kyaml.model.MatchWhere;
import io.kyaml.model.KYaml;
import io.kyaml.model.Rule;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Deserialize {
    private String specstr;

    public Deserialize(String specstr) {
        this.specstr = specstr;
    }

    public KYaml run() {
        Yaml yaml = new Yaml();
        LinkedHashMap m = yaml.load(specstr);
        String json = new Gson().toJson(m);
        System.out.println(json);

        KYaml ky = toKYaml(m);

        return ky;
    }

    public KYaml toKYaml(LinkedHashMap m) {
        List rules = getList(m, "spec");

        KYaml ky = new KYaml();
        ky.rules.addAll(toRuleList(rules));
        return ky;
    }

    private static List<Rule> toRuleList(List rules) {
        List<Rule> prules = new ArrayList();
        for (Object o : rules) {
            prules.add(toRule(getMap(o, "rule")));
        }
        return prules;
    }

    private static Rule toRule(Object m) {
        Rule rule = new Rule();
        rule.name = getString(m, "name");
        System.out.println(((Map) m).keySet());
        rule.inherits = getString(m, "inherits");
        rule.ifb = toMatchWhere(getMap(m, "if"));
        rule.thenb = toMatchWhere(getMap(m, "then"));

        return rule;
    }

    private static MatchWhere toMatchWhere(LinkedHashMap m) {
        MatchWhere mw = new MatchWhere();
        if (m == null) {
            return mw;
        }
        mw.match = getMap(m, "match");
        mw.where = toWhereList(getList(m, "where"));
        return mw;
    }

    public static Constraint toWhereList(List l) {
        Constraint.And and = new Constraint.And();
        if (l == null) return and;

        for (Object oc : l) {
            and.and.add(toWhereObject(oc));
        }
        return and;
    }

    public static Constraint toWhereObject(Object oc) {
        if (oc instanceof String) {
            return new Constraint.Predicate((String) oc);
        } else if (oc instanceof Map) {
            Map<String,Object> m = (Map) oc;
            if (m.size() != 1) {
                throw new KYamlException("Where clause must have exactly one key that is either one of {or, not}");
            }
            String key = m.keySet().iterator().next();
            if (key.equals("or")) {
                Constraint.Or or = new Constraint.Or();
                for (Object o : getList(oc, "or")) {
                    or.or.add(toWhereObject(o));
                }
                return or;
            } else if (key.equals("not")) {
                Constraint.Not not = new Constraint.Not();
                not.not = toWhereObject(getString(m, "not"));
                return not;
            } else {
                throw new KYamlException("Where clause must have exactly one key that is either one of {or, not}");
            }
        } else {
            throw new RuntimeException();
        }
    }

    private static LinkedHashMap getMap(Object o, String key) {
        LinkedHashMap<String,Object> m = (LinkedHashMap) o;
        return (LinkedHashMap) m.get(key);
    }

    private static List getList(Object o, String key) {
        LinkedHashMap<String,Object> m = (LinkedHashMap) o;
        return (List) m.get(key);
    }

    private static String getString(Object o, String key) {
        LinkedHashMap<String,Object> m = (LinkedHashMap) o;
        return (String) m.get(key);
    }
}
