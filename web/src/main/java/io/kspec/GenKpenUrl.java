package io.kspec;

import io.github.cdimascio.dotenv.Dotenv;
import io.kpen.util.Tx;
import io.kpen.web.GetProjectController;
import org.apache.commons.lang3.StringUtils;
import static io.kpen.web.GetProjectController.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static io.kspec.GenMd.*;

public class GenKpenUrl {

    public static void main(String[] args) throws Throwable {
        Dotenv dotenv = Dotenv.load();
        Tx.runex(ctx -> {
            File f = new File("kspec.csv");
            PrintWriter writer = new PrintWriter("kspec.csv");
            List<String> headers = new ArrayList();
            headers.add("Language");
            headers.add("Group");
            headers.add("Example");
            headers.add("Link");
            headers.add("State");

            writer.println(StringUtils.join(headers, ","));
            for (LanguageGroup langGrp : getLanguageGroups()) {

                for (ExampleGroup exGrp : langGrp.getExampleGroups()) {

                    for (Example ex : exGrp.getExampleList()) {

                        List<String> l = new ArrayList();
                        l.add(langGrp.getLanguage());
                        l.add(exGrp.getTitle());
                        l.add(ex.getName());

                        String projectId = ex.getProjectId();
                        if (projectId != null) {
                            GetProjectResp resp = getProject(ctx, Integer.parseInt(projectId));
                            l.add(ex.getLink());
                            l.add(resp.getState());
                        } else {
                            l.add("");
                            l.add("");
                        }

                        writer.println(StringUtils.join(l, ","));
                    }
                }
            }

            writer.close();
            System.out.println("DONE: " + f.getAbsolutePath());
            return null;
        });
    }
}
