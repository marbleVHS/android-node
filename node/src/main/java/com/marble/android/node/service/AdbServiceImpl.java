package com.marble.android.node.service;

import com.marble.android.node.environment.Paths;
import com.marble.android.node.environment.runtime.CommandExecutor;
import org.springframework.stereotype.Service;

@Service
public class AdbServiceImpl implements AdbService {

    private final CommandExecutor commandExecutor;

    public AdbServiceImpl(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String executeAdbCommand(String command) {
        return commandExecutor.executeCommand(Paths.ADB_PATH + " " + command);
    }

}
