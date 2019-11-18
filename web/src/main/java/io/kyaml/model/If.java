package io.kyaml.model;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class If {
    public LinkedHashMap<String,String> match;
    public Conjunct[] where;
}
