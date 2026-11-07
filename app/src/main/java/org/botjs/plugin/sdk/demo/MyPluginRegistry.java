package org.botjs.plugin.sdk.demo;

import android.content.Context;

import org.botjs.plugin.sdk.Plugin;
import org.botjs.plugin.sdk.PluginLoader;
import org.botjs.plugin.sdk.PluginRegistry;
import org.botjs.plugin.sdk.demo.plugin.PluginHelloWorld;


public class MyPluginRegistry extends PluginRegistry {

    static {
        // 注册默认插件
        registerDefaultPlugin(new PluginLoader() {
            @Override
            public Plugin load(Context context, Context selfContext, Object runtime, Object topLevelScope) {
                return new PluginHelloWorld(context, selfContext, runtime, topLevelScope);
            }
        });
    }
}