package io.kyaml.model;

public class Rule {
    public String name;
    public String inherits;
    public MatchWhere ifb = new MatchWhere();
    public MatchWhere thenb = new MatchWhere();

    public Rule inheritsRule;

    @Override
    public String toString() {
        return name;
    }
}
