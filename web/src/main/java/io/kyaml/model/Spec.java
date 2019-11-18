package io.kyaml.model;

import lombok.Data;

@Data
public class Spec {
    public String name;
    public String inherits;
    public If ifb;
    public Then thenb;
}
