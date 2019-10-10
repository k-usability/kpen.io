package io.kspec;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExampleGroup {
    private String title;
    private List<Example> exampleList = new ArrayList();
}
