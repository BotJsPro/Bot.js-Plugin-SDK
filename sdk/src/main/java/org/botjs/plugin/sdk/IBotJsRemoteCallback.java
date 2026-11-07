//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.botjs.plugin.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

public interface IBotJsRemoteCallback extends IInterface {
    Map call(String var1, Map var2) throws RemoteException;

    public abstract static class Stub extends Binder implements IBotJsRemoteCallback {
        private static final String DESCRIPTOR = "org.botjs.plugin.sdk.IBotJsRemoteCallback";
        static final int TRANSACTION_call = 1;

        public Stub() {
            this.attachInterface(this, "org.botjs.plugin.sdk.IBotJsRemoteCallback");
        }

        public static IBotJsRemoteCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            } else {
                IInterface iin = obj.queryLocalInterface("org.botjs.plugin.sdk.IBotJsRemoteCallback");
                return (iin != null && iin instanceof IBotJsRemoteCallback ? (IBotJsRemoteCallback)iin : new Proxy(obj));
            }
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = "org.botjs.plugin.sdk.IBotJsRemoteCallback";
            switch (code) {
                case 1:
                    data.enforceInterface(descriptor);
                    String _arg0 = data.readString();
                    ClassLoader cl = this.getClass().getClassLoader();
                    Map _arg1 = data.readHashMap(cl);
                    Map _result = this.call(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeMap(_result);
                    return true;
                case 1598968902:
                    reply.writeString(descriptor);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        public static boolean setDefaultImpl(IBotJsRemoteCallback impl) {
            if (Proxy.sDefaultImpl == null && impl != null) {
                Proxy.sDefaultImpl = impl;
                return true;
            } else {
                return false;
            }
        }

        public static IBotJsRemoteCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }

        private static class Proxy implements IBotJsRemoteCallback {
            private IBinder mRemote;
            public static IBotJsRemoteCallback sDefaultImpl;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "org.botjs.plugin.sdk.IBotJsRemoteCallback";
            }

            public Map call(String event, Map args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();

                HashMap _result;
                try {
                    _data.writeInterfaceToken("org.botjs.plugin.sdk.IBotJsRemoteCallback");
                    _data.writeString(event);
                    _data.writeMap(args);
                    boolean _status = this.mRemote.transact(1, _data, _reply, 0);
                    if (!_status && Stub.getDefaultImpl() != null) {
                        Map var11 = Stub.getDefaultImpl().call(event, args);
                        return var11;
                    }

                    _reply.readException();
                    ClassLoader cl = this.getClass().getClassLoader();
                    _result = _reply.readHashMap(cl);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }
        }
    }

    public static class Default implements IBotJsRemoteCallback {
        public Default() {
        }

        public Map call(String event, Map args) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }
}
