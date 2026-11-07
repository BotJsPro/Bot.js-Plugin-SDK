//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.botjs.plugin.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Keep;



import java.util.Map;

@Keep
public abstract class Plugin implements ServiceConnection {
    private final Object mRuntime;
    private final Object mTopLevelScope;
    private final Context mContext;
    private final Context mSelfContext;
    private Connection mConnection = null;
    private boolean mDisconnected = false;

    public Plugin(Context context, Context selfContext, Object runtime, Object topLevelScope) {
        this.mRuntime = runtime;
        this.mTopLevelScope = topLevelScope;
        this.mContext = context;
        this.mSelfContext = selfContext;
    }

    public Context getSelfContext() {
        return this.mSelfContext;
    }

    public Object getRuntime() {
        return this.mRuntime;
    }

    public Object getTopLevelScope() {
        return this.mTopLevelScope;
    }

    public Context getContext() {
        return this.mContext;
    }

    public abstract String getAssetsScriptDir();

    public ComponentName getService() {
        return null;
    }

    @Keep
    public static int getVersion() {
        return 2;
    }

    public Connection getConnection() {
        return this.mConnection;
    }

    public boolean isDisconnected() {
        return this.mDisconnected;
    }

    public Connection requireConnection() throws RemoteException {
        if (this.mConnection != null) {
            return this.mConnection;
        } else if (this.mDisconnected) {
            throw new RemoteException("plugin disconnected: " + this.getClass() + ": " + this);
        } else {
            throw new RemoteException("plugin is not connected: " + this.getClass() + ": " + this);
        }
    }

    public Connection waitForConnection() throws InterruptedException, RemoteException {
        if (this.mConnection != null) {
            return this.mConnection;
        } else if (this.mDisconnected) {
            throw new RemoteException("plugin disconnected: " + this.getClass() + ": " + this);
        } else {
            synchronized(this) {
                if (this.mConnection != null) {
                    return this.mConnection;
                } else {
                    this.wait();
                    if (this.mDisconnected) {
                        throw new RemoteException("plugin disconnected: " + this.getClass() + ": " + this);
                    } else {
                        return this.mConnection;
                    }
                }
            }
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        IBotJsRemoteCall connection = IBotJsRemoteCall.Stub.asInterface(service);

        try {
            service.linkToDeath(new IBinder.DeathRecipient() {
                public void binderDied() {
                    Plugin.this.onServiceDisconnected();
                }
            }, 0);
        } catch (RemoteException var7) {
            var7.printStackTrace();
        }

        synchronized(this) {
            this.mConnection = new Connection(connection);
            this.notifyAll();
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        this.onServiceDisconnected();
    }

    private void onServiceDisconnected() {
        this.mDisconnected = true;
        synchronized(this) {
            this.mConnection = null;
            this.notifyAll();
        }
    }

    public static class Connection {
        private final IBotJsRemoteCall mRemoteCall;

        public Connection(IBotJsRemoteCall mRemoteCall) {
            this.mRemoteCall = mRemoteCall;
        }

        public Map<String, ?> call(String action, Map<String, ?> args, final RemoteCallback callback) throws RemoteException {
            Log.i("Plugin", "call: action = " + action + ", args = " + args + ", callback = " + callback);
            PluginService.Result result = PluginService.Result.fromMap(this.mRemoteCall.call(action, args, callback == null ? null : new IBotJsRemoteCallback.Stub() {
                public Map call(String event, Map args) throws RemoteException {
                    return callback.call(event, args);
                }
            }));
            Log.i("Plugin", "call: action = " + action + ", result = " + result);
            if (!result.getError().isEmpty()) {
                throw new RemoteException(result.getError());
            } else {
                return result.getData();
            }
        }
    }

    public interface RemoteCallback {
        Map<String, ?> call(String var1, Map<String, ?> var2) throws RemoteException;
    }
}
