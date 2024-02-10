package com.marble.android.node.environment.runtime;

public interface CommandExecutor {

    String executeCommand(String command);

    String executeCommand(String command, boolean ignoreError);

    Process startProcess(String command);

}
