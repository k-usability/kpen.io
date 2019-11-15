package io.kpen.worker;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Kw {

    @Data
    public static class Result {
        public String outputFilePath;
        public String errorFilePath;
        public Integer statusCode;
        public Boolean timedOut;
        public Boolean proved;
    }

    public static Result run(Dotenv env, String genDir, String specFn, String kpath, String sempath, Integer timeoutSecs, Integer memLimitMb, String stdoutFn, String stderrFn) {
        String javaPath = env.get("APP_JAVA_PATH");
        String cpPath = kpath + "/target/release/k/lib/java/*";
        String smtpreludePath = genDir + "/evm.smt2";
        String specPath = genDir + "/" + specFn;
        String stdoutPath = genDir + "/" + stdoutFn;
        String stderrPath = genDir + "/" + stderrFn;

        List<String> args = new ArrayList();
        args.add(javaPath);
        args.add("-Dfile.encoding=UTF-8");
        args.add("-Djava.awt.headless=true");
        args.add("-Xms" + memLimitMb + "m");
        args.add("-Xmx" + memLimitMb + "m");
        args.add("-Xss32m");
        args.add("-XX:+TieredCompilation");
        args.add("-ea");
        args.add("-cp");
        args.add(cpPath);
        args.add("org.kframework.main.Main");
        args.add("-kprove");
        args.add("-v");
        args.add("--debug");
        args.add("-d");
        args.add(sempath);
        args.add("-m");
        args.add("VERIFICATION");
        args.add("--z3-impl-timeout");
        args.add("500");
        args.add("--deterministic-functions");
        args.add("--no-exc-wrap");
        args.add("--cache-func-optimized");
        args.add("--no-alpha-renaming");
        args.add("--format-failures");
        args.add("--boundary-cells");
        args.add("k,pc");
        args.add("--log-cells");
        args.add("k,output,statusCode,localMem,pc,gas,wordStack,callData,accounts,memoryUsed,#pc,#result,#target");
        args.add("--smt-prelude");
        args.add(smtpreludePath);
        args.add(specPath);

        System.out.println("Executing K prover: " + javaPath + " " + StringUtils.join(args, " "));
        Integer statusCode = null;
        Boolean proved = null;
        Boolean timedOut = null;

        File stdout = new File(stdoutPath);
        File stderr = new File(stderrPath);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectOutput(stdout);
        pb.redirectError(stderr);
        Process p = null;
        try {
            p = pb.start();
            boolean exitedWithinTimeout = p.waitFor(timeoutSecs, TimeUnit.SECONDS);
            if (exitedWithinTimeout) {
                System.out.println("Exited normally");
                statusCode = p.exitValue();
                timedOut = false;
            } else {
                System.out.println("Timed out. Destroying...");
                p.destroy();
                boolean destroyedCleanly = p.waitFor(5, TimeUnit.SECONDS);
                if (!destroyedCleanly) {
                    System.err.println("Did not destroy cleanly. Forcing...");
                    p.destroyForcibly();
                    p.waitFor();
                    System.err.println("Destroyed forcibly");
                }
                timedOut = true;
            }
        } catch (Throwable t) {
            System.err.println("Starting process failed: " + ExceptionUtils.getStackTrace(t));
        }


        if (stdout.exists()) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(stdout));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("#True")) {
                        proved = true;
                    }
                    if (line.equals("false")) {
                        proved = false;
                    }
                }
            } catch (Throwable t) {
                System.err.println("Could not read from stdout: " + ExceptionUtils.getStackTrace(t));
            }
        }

        Result res = new Result();
        res.setOutputFilePath(stdoutPath);
        res.setErrorFilePath(stderrPath);
        res.setStatusCode(statusCode);
        res.setTimedOut(timedOut);
        res.setProved(proved);

        return res;
    }
}
