package io.kyaml.model;

import lombok.Data;

@Data
public class Conjunct {
    public String value;
    public Not not;
    public Or or;

    public Conjunct() {

    }

    public Conjunct(String value) {
        this.value = value;
    }
}
