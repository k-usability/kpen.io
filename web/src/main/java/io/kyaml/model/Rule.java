package io.kyaml.model;

import lombok.Data;

@Data
public class Rule {
    public String name;
    public String inherits;
    public MatchWhere ifb;
    public MatchWhere thenb;
}
