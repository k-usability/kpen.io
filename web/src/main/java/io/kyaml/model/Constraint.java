package io.kyaml.model;

import java.util.ArrayList;
import java.util.List;

public class Constraint {

    public static class Predicate extends Constraint {
        public String predicate;

        public Predicate(String str) {
            this.predicate = str;
        }
    }

    public static class And extends Constraint {
        public List<Constraint> and = new ArrayList();
    }

    public static class Or extends Constraint {
        public List<Constraint> or = new ArrayList();
    }

    public static class Not extends Constraint {
        public Constraint not;
    }
}
