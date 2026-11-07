package org.botjs.plugin.sdk.demo.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import org.botjs.plugin.sdk.PluginService;
import org.botjs.plugin.sdk.PluginUtils;

import java.util.Collections;
import java.util.Map;

public class HelloworldPluginService extends PluginService {
    private static final String ACTION_ADD = "add";

    @Override
    protected Result onRemoteCall(@NonNull String action, @NonNull Map<String, Object> args, @Nullable RemoteCallback callback) throws RuntimeException {
        if (ACTION_ADD.equals(action)) {
            return invokeAdd(args);
        }
        return Result.notImplemented(action);
    }

    private Result invokeAdd(Map<String, Object> args) {
        Number a   = PluginUtils.getNotNull(args, "a");
        Number b   = PluginUtils.getNotNull(args, "b");
        double sum = a.doubleValue() + b.doubleValue();
        return new Result(Collections.singletonMap("sum", sum));
    }
}