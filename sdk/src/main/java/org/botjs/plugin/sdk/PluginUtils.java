

package org.botjs.plugin.sdk;

import java.util.Map;

public class PluginUtils {
    public PluginUtils() {
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else {
            return obj;
        }
    }

//    public static <T> T getNotNull(Map<String, ?> args, String key) {
//        T value = args.get(key);
//        if (value == null) {
//            throw new NullPointerException(key + " is null in args: " + args);
//        } else {
//            return value;
//        }
//    }
//    public static <T> T getNotNull(Map<String, ?> args, String key) {
//        Object rawValue = args.get(key);
//        if (rawValue == null) {
//            throw new NullPointerException(key + " is null in args: " + args);
//        } else {
//            // 将原始值转换为所需类型
//            @SuppressWarnings("unchecked")
//            T value = (T) rawValue;
//            return value;
//        }
//    }
    public static <T> T getNotNull(Map<String, ?> args, String key) {
        T value = (T)args.get(key);
        if (value == null)
            throw new NullPointerException(key + " is null in args: " + args);
        return value;
    }
}
