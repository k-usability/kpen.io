package io.kspec;

import lombok.Data;

import java.util.List;

@Data
public class LanguageGroup {
    private String language;
    private List<ExampleGroup> exampleGroups;
}
