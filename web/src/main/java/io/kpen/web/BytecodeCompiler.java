package io.kpen.web;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.kpen.web.BytecodeCompiler.ErrorType.*;

public class BytecodeCompiler {
    enum ErrorType {
        CompilationFailure,
        MultipleContracts,
        NoContracts
    }

    public static CompilationResult getBytecode(File programFile, File generatedDir) throws IOException {
        CompilationResult result = new CompilationResult();

        Dotenv dotenv = Dotenv.load();

        String solcPath = dotenv.get("APP_SOLC_050");
        String cmd = solcPath + " --overwrite -o " + generatedDir.getAbsolutePath() + " --bin-runtime " + programFile.getAbsolutePath();
        CommandLine cmdLine = CommandLine.parse(cmd);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));
        Integer exitValue = null;
        try {
            exitValue = executor.execute(cmdLine);
        } catch (Throwable t) {

        } finally {
            if (exitValue == null || exitValue != 0) {
                String err = errorStream.toString();
                int i = err.indexOf(":");
                if (i >= 0) {
                    err = err.substring(i+1);
                    err = "Line " + err;
                }
                result.setSuccess(false);
                result.setErrorMessage(err);
                result.setErrorType(CompilationFailure);
                return result;
            }
        }

        List<File> binRuntimeFiles = getFilesWithSuffix(generatedDir, "bin-runtime");
        if (binRuntimeFiles.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("The program must be a single contract.");
            result.setErrorType(NoContracts);
            return result;
        } else if (binRuntimeFiles.size() > 1) {
            result.setSuccess(false);
            result.setErrorMessage("The program must be a single contract. Multiple contracts is not supported.");
            result.setErrorType(MultipleContracts);
            return result;
        }

        File binRuntimeFile = binRuntimeFiles.get(0);
        result.setBytecodeHex("0x" + FileUtils.readFileToString(binRuntimeFile, Charset.defaultCharset()));
        result.setSuccess(true);

        return result;
    }

    @Data
    public static class CompilationResult {
        private boolean success;
        private String bytecodeHex;
        private ErrorType errorType;
        private String errorMessage;
    }

    private static List<File> getFilesWithSuffix(File parent, String suffix) {
        if (parent == null) throw new RuntimeException();
        List<File> l = new ArrayList();
        File[] files = parent.listFiles();
        if (files == null) return Collections.EMPTY_LIST;
        for (File f : files) {
            if (f.getAbsolutePath().endsWith(suffix)) {
                l.add(f);
            }
        }
        return l;
    }
}
