package org.botjs.plugin.sdk.demo.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import org.botjs.plugin.sdk.PluginService;

import java.util.Map;

public class MyService extends PluginService {

    private static final String TAG = "MyService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 服务已创建");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 服务已启动");
        // 在这里执行你的服务逻辑
        return START_STICKY; // 返回 START_STICKY 表示服务在被系统杀死后会自动重新启动
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 服务已销毁");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected Result onRemoteCall(@NonNull String var1, @NonNull Map<String, Object> var2, @Nullable RemoteCallback var3) throws RuntimeException {
        return null;
    }
}