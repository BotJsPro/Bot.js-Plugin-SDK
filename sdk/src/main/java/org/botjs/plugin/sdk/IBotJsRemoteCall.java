package org.botjs.plugin.sdk;


import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.Map;

public interface IBotJsRemoteCall extends IInterface {
    Map call(String var1, Map var2, IBotJsRemoteCallback var3) throws RemoteException;

    public abstract static class Stub extends Binder implements IBotJsRemoteCall {
        private static final String DESCRIPTOR = "org.botjs.plugin.sdk.IBotJsRemoteCall";
        static final int TRANSACTION_call = 1;

        public Stub() {
            this.attachInterface(this, "org.botjs.plugin.sdk.IBotJsRemoteCall");
        }

        public static IBotJsRemoteCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            } else {
                IInterface iin = obj.queryLocalInterface("org.botjs.plugin.sdk.IBotJsRemoteCall");
                return (IBotJsRemoteCall)(iin != null && iin instanceof IBotJsRemoteCall ? (IBotJsRemoteCall)iin : new Proxy(obj));
            }
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = "org.botjs.plugin.sdk.IBotJsRemoteCall";
            switch (code) {
                case 1:
                    data.enforceInterface(descriptor);
                    String _arg0 = data.readString();
                    ClassLoader cl = this.getClass().getClassLoader();
                    Map _arg1 = data.readHashMap(cl);
                    IBotJsRemoteCallback _arg2 = IBotJsRemoteCallback.Stub.asInterface(data.readStrongBinder());
                    Map _result = this.call(_arg0, _arg1, _arg2);
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

        public static boolean setDefaultImpl(IBotJsRemoteCall impl) {
            if (Proxy.sDefaultImpl == null && impl != null) {
                Proxy.sDefaultImpl = impl;
                return true;
            } else {
                return false;
            }
        }

        public static IBotJsRemoteCall getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }

        private static class Proxy implements IBotJsRemoteCall {
            private IBinder mRemote;
            public static IBotJsRemoteCall sDefaultImpl;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "org.botjs.plugin.sdk.IBotJsRemoteCall";
            }

            public Map call(String action, Map args, IBotJsRemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();

                Map var8;
                try {
                    _data.writeInterfaceToken("org.botjs.plugin.sdk.IBotJsRemoteCall");
                    _data.writeString(action);
                    _data.writeMap(args);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _status = this.mRemote.transact(1, _data, _reply, 0);
                    if (_status || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        ClassLoader cl = this.getClass().getClassLoader();
                        Map _result = _reply.readHashMap(cl);
                        return _result;
                    }

                    var8 = Stub.getDefaultImpl().call(action, args, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return var8;
            }
        }
    }

    public static class Default implements IBotJsRemoteCall {
        public Default() {
        }

        public Map call(String action, Map args, IBotJsRemoteCallback callback) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }
}
