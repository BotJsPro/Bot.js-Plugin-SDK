package org.botjs.plugin.sdk.demo.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.botjs.plugin.sdk.Plugin;
import org.botjs.plugin.sdk.demo.HelloWorldActivity;
import org.botjs.plugin.sdk.demo.services.HelloworldPluginService;
import org.botjs.plugin.sdk.demo.services.MyService;


public class PluginHelloWorld extends Plugin {

    public PluginHelloWorld(Context context, Context selfContext, Object runtime, Object topLevelScope) {
        super(context, selfContext, runtime, topLevelScope);
    }

    // 返回插件的JavaScript胶水层代码的assets目录路径
    @Override
    public String getAssetsScriptDir() {
        return "js-plugin";
    }

    // 插件public API，可被JavaScript代码调用
    public String getStringFromJava() {
        return "Hello, Bot.js Pro!";
    }

    // 插件public API，可被JavaScript代码调用
    public void say(String message) {
        // 获取当前上下文，并启动一个新的 Activity
        getSelfContext().startActivity(
                // 创建一个 Intent 对象，指定要启动的目标 Activity 为 MainActivity
                new Intent(getSelfContext(), HelloWorldActivity.class)
                        // 向 Intent 中添加额外的数据，键为 "message"，值为变量 message 中存储的内容
                        .putExtra("message", message)
                        // 添加标志，表示新启动的 Activity 应该作为一个新的任务启动
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );

    }

    @Override
    public ComponentName getService() {
        return
                // 创建服务组件的 ComponentName 对象
                new ComponentName(
                        // 获取当前上下文的包名
                        getSelfContext().getPackageName(),
                        // 获取 HelloworldPluginService 类的完全限定名
                        HelloworldPluginService.class.getName()
                );
    }

    // 启动服务的函数
    public boolean startMyService() {
        // 创建服务的意图
        Intent serviceIntent = new Intent();
        // 设置服务组件的 ComponentName
        serviceIntent.setComponent(new ComponentName(getSelfContext(), MyService.class));
        // 启动服务
        boolean started = getSelfContext().startService(serviceIntent) != null;
        // 返回启动结果
        return started;
    }

    // 结束服务的函数
    public boolean stopMyService() {
        // 创建停止服务的意图
        Intent serviceIntent = new Intent();
        // 设置服务组件的 ComponentName
        serviceIntent.setComponent(new ComponentName(getSelfContext(), MyService.class));
        // 停止服务
        boolean stopped = getSelfContext().stopService(serviceIntent);
        // 返回停止结果
        return stopped;
    }
}
