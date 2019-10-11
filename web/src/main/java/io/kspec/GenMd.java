package io.kspec;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.WordUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class GenMd {
    public static Collection<String> LANGUAGES = Arrays.asList("evm");
    public static final String OUT_DIR_KSPECIO = "out-kspec.io";
    public static final String KEXAMPLES_REPO = "/home/sbugrara/k-examples";

    public static void main(String[] args) throws IOException {
        File outdir = new File(OUT_DIR_KSPECIO);
        outdir.delete();
        outdir.mkdir();
        System.out.println("output dir: " + outdir.getAbsolutePath());

        FileTemplateLoader loader = new FileTemplateLoader("src/main/resources");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile("kspec-io-md");

        for (LanguageGroup langGrp : getLanguageGroups()) {
            File outlangdir = new File(OUT_DIR_KSPECIO + "/" + langGrp.getLanguage());
            outlangdir.mkdir();

            for (ExampleGroup exGrp : langGrp.getExampleGroups()) {
                String s = template.apply(exGrp);
                FileUtils.write(new File(outlangdir.getAbsolutePath() + "/" + exGrp.getTitle().toLowerCase() + ".md"), s, Charset.defaultCharset());
            }
        }

        System.out.println("DONE");
    }

    public static List<LanguageGroup> getLanguageGroups() throws IOException {

        List<LanguageGroup> languageGroups = new ArrayList();
        File outdir = new File(OUT_DIR_KSPECIO);
        outdir.delete();
        outdir.mkdir();
        System.out.println("output dir: " + outdir.getAbsolutePath());

        FileTemplateLoader loader = new FileTemplateLoader("src/main/resources");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        Template template = handlebars.compile("kspec-io-md");

        File kexdir = new File(KEXAMPLES_REPO);
        for (File langdir : kexdir.listFiles()) {
            String lang = langdir.getName();
            if (!LANGUAGES.contains(lang)) continue;

            LanguageGroup languageGroup = new LanguageGroup();
            languageGroups.add(languageGroup);
            languageGroup.setLanguage(lang);

            Yaml yaml = new Yaml(new Constructor(ExampleConfig.class));

            List<ExampleGroup> exampleGroups = new ArrayList();
            for (File exgrpDir : langdir.listFiles()) {
                String title = WordUtils.capitalizeFully(exgrpDir.getName());
                ExampleGroup exgrp = new ExampleGroup();
                exampleGroups.add(exgrp);
                exgrp.setTitle(title);

                List<File> exFileList = new ArrayList(Arrays.asList(exgrpDir.listFiles()));
                Collections.sort(exFileList, Comparator.comparing(o -> o.getName()));

                List<Example> exampleList = new ArrayList();
                for (File exdir : exFileList) {
                    String exname = exdir.getName();
                    String expath = exdir.getAbsolutePath();
                    String programFileName = null;
                    for (File f : exdir.listFiles()) {
                        if (f.getName().endsWith(".sol")) {
                            programFileName = f.getName();
                            break;
                        }
                    }

                    ExampleConfig config = yaml.load(new FileInputStream(new File(expath + "/config.yaml")));

                    Example ex = new Example();
                    ex.setDescription(config.description);
                    ex.setName(exname);
                    ex.setProjectId(config.kpenioprojectid);
                    ex.setExplanation(FileUtils.readFileToString(new File(expath + "/explanation.md"), Charset.defaultCharset()));

                    String spec = FileUtils.readFileToString(new File(expath + "/spec.ini"), Charset.defaultCharset());
                    String program = FileUtils.readFileToString(new File(expath + "/" + programFileName), Charset.defaultCharset());
                    spec = "{{< highlight ini \"linenos=inline\" >}}\n" + spec + "\n{{< / highlight >}}";
                    program = "{{< highlight solidity \"linenos=inline\" >}}\n" + program + "\n{{< / highlight >}}";

                    ex.setSpec(spec);
                    ex.setProgram(program);
                    if (config.kpenioprojectid != null && !config.kpenioprojectid.isEmpty()) {
                        ex.setLink("https://kpen.io/project/" + config.kpenioprojectid);
                    }
                    exampleList.add(ex);
                }
                exgrp.setExampleList(exampleList);
            }

            languageGroup.setExampleGroups(exampleGroups);
        }

        return languageGroups;
    }
}
