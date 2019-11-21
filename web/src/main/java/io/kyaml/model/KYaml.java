package io.kyaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KYaml {
   public List<Rule> rules = new ArrayList();
}
