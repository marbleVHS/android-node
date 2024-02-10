package com.marble.android.node.environment.runtime;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.List;

@Component
public class BashExecutor implements CommandExecutor {

    @Override
    public String executeCommand(String command) {
        return executeCommand(command, false);
    }

    @SneakyThrows
    @Override
    public String executeCommand(String command, boolean ignoreError) {
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        if (!ignoreError) {
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = process.errorReader()) {
                errorReader.lines().forEachOrdered(line -> errorOutput.append(line).append("\n"));
                if (!errorOutput.isEmpty()) {
                    throw new CommandExecutionException(errorOutput.toString());
                }
            }
        }
        StringBuilder output = new StringBuilder();
        try (BufferedReader processOutReader = process.inputReader()) {
            processOutReader.lines().forEachOrdered(line -> output.append(line).append("\n"));
        }
        return output.toString();
    }

    @SneakyThrows
    @Override
    public Process startProcess(String command) {
        return new ProcessBuilder().command(List.of("bash", "-c", command)).start();
    }
}
