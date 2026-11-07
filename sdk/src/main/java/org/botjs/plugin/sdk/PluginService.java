//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.botjs.plugin.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class PluginService extends Service {
    private final IBinder mPluginBinder = new IBotJsRemoteCall.Stub() {
        public Map call(String action, Map args, final IBotJsRemoteCallback callback) throws RemoteException {
            if (action == null) {
                return (new Result("action is null")).toMap();
            } else {
                if (args == null) {
                    args = Collections.emptyMap();
                }

                RemoteCallback remoteCallback = callback == null ? null : new RemoteCallback() {
                    public Result call(String event, Map<String, Object> args) throws RemoteException {
                        return Result.fromMap(callback.call(event, args));
                    }
                };

                try {
                    return PluginService.this.onRemoteCall(action, args, remoteCallback).toMap();
                } catch (Exception var6) {
                    var6.printStackTrace();
                    return (new Result(PluginService.exceptionToString(var6))).toMap();
                }
            }
        }
    };

    public PluginService() {
    }

    protected abstract Result onRemoteCall(@NonNull String var1, @NonNull Map<String, Object> var2, @Nullable RemoteCallback var3) throws RuntimeException;

    @Nullable
    public IBinder onBind(Intent intent) {
        return this.mPluginBinder;
    }

    private static String exceptionToString(Throwable e) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, true);
        e.printStackTrace(ps);
        ps.close();
        return bos.toString();
    }

    public interface RemoteCallback {
        Result call(String var1, Map<String, Object> var2) throws RemoteException;
    }

    protected static class Result {
        @NonNull
        private final String error;
        @NonNull
        private final Map<String, ?> data;

        public Result(String error) {
            this(error, Collections.emptyMap());
        }

        public Result(@Nullable String error, Map<String, ?> data) {
            this.error = error == null ? "" : error;
            this.data = data == null ? Collections.emptyMap() : data;
        }

        public Result(Map<String, ?> data) {
            this("", data);
        }

        @NonNull
        public String getError() {
            return this.error;
        }

        @NonNull
        public Map<String, ?> getData() {
            return this.data;
        }

        private Map<String, ?> toMap() {
            Map<String, Object> map = new HashMap();
            map.put("error", this.error);
            map.put("data", this.data);
            return map;
        }

        public String toString() {
            return "Result{error='" + this.error + '\'' + ", data=" + this.data + '}';
        }

        protected static Result fromMap(Map<?, ?> map) {
            String error = (String)map.get("error");
            Map<String, ?> data = (Map)map.get("data");
            return new Result(error, data);
        }

        public static Result notImplemented(String action) {
            return new Result("not implemented: " + action);
        }
    }
}
