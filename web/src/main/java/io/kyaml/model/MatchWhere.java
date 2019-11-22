package io.kyaml.model;

import java.util.HashMap;

public class MatchWhere {
    public HashMap<String,String> match = new HashMap();
    public Constraint.And where = new Constraint.And();
}
