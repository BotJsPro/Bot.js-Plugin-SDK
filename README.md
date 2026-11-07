# Bot.js Plugin SDK

Bot.js 插件开发 SDK。提供一套基于 **Android Service + Binder IPC** 的插件契约，让插件以独立 APK/模块的形式被 Bot.js 宿主加载，并通过 JS 胶水层把 Java 能力暴露给脚本使用。

- 插件入口：`PluginService`（跨进程 Binder 服务）
- 宿主连接器：`Plugin`（`ServiceConnection` + `Connection.call()`）
- 发现与创建：`PluginRegistry` + `PluginLoader`
- JS 胶水层：插件 `assets/<dir>/index.js`，由 `Plugin.getAssetsScriptDir()` 指定目录
- 远程协议：`IBotJsRemoteCall` / `IBotJsRemoteCallback`（手写 AIDL 等价实现）
- 许可证：Apache License 2.0

> 本 SDK 本身不是可独立运行的插件，而是“插件契约 + IPC 桥接层”。插件 APK 按约定实现 `PluginService`、`Plugin`、`PluginRegistry/PluginLoader` 后，才能被宿主加载。

---

## 工作原理

```
Bot.js 宿主进程                          插件进程
┌─────────────────────┐                ┌──────────────────────┐
│ $plugins.load(...)   │                │ PluginService        │
│   ↓                  │                │   onRemoteCall(...)  │
│ PluginRegistry.load  │                │                      │
│   ↓                  │   Binder IPC   │ IBotJsRemoteCall.Stub│
│ Plugin (ServiceConn) │ ◄────────────► │ IBotJsRemoteCallback │
│   Connection.call()  │                │                      │
└─────────────────────┘                └──────────────────────┘
↑
assets/<dir>/index.js  (JS 胶水层)
```

1. 插件 APK 依赖本 SDK，实现：
   - `XxxPlugin extends Plugin`：声明 Service、assets 脚本目录、对外 Java API；
   - `XxxService extends PluginService`：处理远程 `action`；
   - `XxxRegistry extends PluginRegistry`：静态注册 `PluginLoader`。
2. 宿主调用 `$plugins.load(...)`，通过 `PluginRegistry.load()` / `PluginRegistry.loadDefault()` 创建 `Plugin` 实例。
3. 宿主根据 `Plugin.getService()` 得到 `ComponentName`，调用 `bindService(new Intent().setComponent(componentName), plugin, flags)`。`Plugin` 本身只是 `ServiceConnection`，不会自动绑定 Service。
4. `Plugin.onServiceConnected()` 拿到 `IBotJsRemoteCall`，封装为 `Connection`。
5. 宿主加载 `assets/<getAssetsScriptDir()>/index.js`，把 Java `Plugin` 对象注入 `module.exports` 函数，得到 JS API 对象。
6. 脚本调用 JS API → `Connection.call(action, args, callback)` → 插件 `PluginService.onRemoteCall()` → 返回 `{error, data}`。

> 注意：`$plugins.load(name)` 对应 `PluginRegistry.load(name, ...)`，只会匹配通过 `registerPlugin(name, loader)` 注册的 loader；`registerDefaultPlugin(loader)` 注册的是 `"::default"`，只影响 `loadDefault`。

---

## 目录结构（SDK）

```
sdk/
├── src/main/java/org/botjs/plugin/sdk/
│   ├── IBotJsRemoteCall.java        # 宿主 → 插件 的 Binder 接口
│   ├── IBotJsRemoteCallback.java    # 插件 → 宿主 的回调接口
│   ├── Plugin.java                  # 宿主侧连接器（ServiceConnection）
│   ├── PluginLoader.java            # 插件工厂接口
│   ├── PluginRegistry.java          # 静态注册表
│   ├── PluginService.java           # 插件侧 Service 基类
│   └── PluginUtils.java             # 参数取值工具
└── build.gradle.kts
```

Demo 模块额外包含：

```
app/src/main/
├── java/org/botjs/plugin/sdk/demo/
│   ├── MyPluginRegistry.java         # 注册插件/默认插件
│   ├── plugin/PluginHelloWorld.java  # 插件实现
│   └── services/
│       ├── HelloworldPluginService.java  # Binder 服务（处理 add）
│       └── MyService.java                # 继承 PluginService、但 onBind 返回 null 的普通后台服务示例
└── assets/js-plugin/index.js         # JS 胶水层
```

---

## 快速开始

### 1. 依赖 SDK

插件模块中依赖 SDK。是否使用 `compileOnly` 取决于运行时由谁提供 SDK 类：

```kotlin
dependencies {
    // 仅当宿主/插件框架在运行时保证提供同一份 SDK 类时使用 compileOnly
    compileOnly("com.github.BotJsPro:Bot.js-Plugin-SDK:1.0.0")

    // 如果插件 APK 的 Service 运行在独立应用进程，且该进程不能从宿主获得 SDK 类，
    // 则需要让插件 APK 自己能加载 PluginService/Plugin 等基类：
    // implementation("com.github.BotJsPro:Bot.js-Plugin-SDK:1.0.0")
}
```

`PluginRegistry` 的静态注册表只在同一 `ClassLoader` / 同一进程内共享。动态加载到宿主进程的插件代码应与宿主共享同一份 SDK 类；但独立插件进程中的 `PluginService` 仍必须能加载 SDK 基类。

### 2. 实现插件

```java
public class PluginHelloWorld extends Plugin {
    public PluginHelloWorld(Context context, Context selfContext, Object runtime, Object topLevelScope) {
        super(context, selfContext, runtime, topLevelScope);
    }

    @Override
    public String getAssetsScriptDir() {
        return "js-plugin"; // 对应 assets/js-plugin/index.js
    }

    @Override
    public ComponentName getService() {
        return new ComponentName(
            getSelfContext().getPackageName(),
            HelloworldPluginService.class.getName()
        );
    }

    // 对外 Java API，可被 JS 胶水层调用
    public String getStringFromJava() {
        return "Hello, Bot.js Pro!";
    }

    public void say(String message) {
        getSelfContext().startActivity(
            new Intent(getSelfContext(), HelloWorldActivity.class)
                .putExtra("message", message)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public boolean startMyService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(getSelfContext(), MyService.class));
        return getSelfContext().startService(serviceIntent) != null;
    }

    public boolean stopMyService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(getSelfContext(), MyService.class));
        return getSelfContext().stopService(serviceIntent);
    }
}
```

> `Plugin.getService()` 基类默认返回 `null`。如果插件需要 Binder 远程调用，必须重写它并返回有效的 Service `ComponentName`。

### 3. 实现 Binder 服务

```java
public class HelloworldPluginService extends PluginService {
    private static final String ACTION_ADD = "add";

    @Override
    protected Result onRemoteCall(String action, Map<String, Object> args, RemoteCallback callback) {
        if (ACTION_ADD.equals(action)) {
            Number a = PluginUtils.getNotNull(args, "a");
            Number b = PluginUtils.getNotNull(args, "b");
            double sum = a.doubleValue() + b.doubleValue();
            return new Result(Collections.singletonMap("sum", sum));
        }
        return Result.notImplemented(action);
    }
}
```

### 4. 注册插件

如果 JS 侧使用 `$plugins.load("org.botjs.plugin.sdk.demo")`，注册名必须与 `load(name)` 的参数一致：

```java
public class MyPluginRegistry extends PluginRegistry {
    static {
        PluginLoader loader = new PluginLoader() {
            @Override
            public Plugin load(Context context, Context selfContext, Object runtime, Object topLevelScope) {
                return new PluginHelloWorld(context, selfContext, runtime, topLevelScope);
            }
        };

        // 供 PluginRegistry.load("org.botjs.plugin.sdk.demo", ...) 使用
        registerPlugin("org.botjs.plugin.sdk.demo", loader);

        // 可选：同时注册为默认插件，供 PluginRegistry.loadDefault(...) 使用
        registerDefaultPlugin(loader);
    }
}
```

> 如果保持 Demo 源码里只调用 `registerDefaultPlugin(loader)`，则 `$plugins.load("org.botjs.plugin.sdk.demo")` 不会命中；此时宿主应使用默认加载路径（对应 `PluginRegistry.loadDefault(...)`），或补上 `registerPlugin("org.botjs.plugin.sdk.demo", loader)`。

### 5. 编写 JS 胶水层

`app/src/main/assets/js-plugin/index.js`：

```js
module.exports = function (plugin) {
  function helloWorld() {}

  // Demo 中这里导出的是“属性”（字符串），调用时不要加括号
  helloWorld.getAssetsScriptDir = plugin.getAssetsScriptDir();
  helloWorld.stringFromJava = plugin.getStringFromJava();

  // 方法
  helloWorld.say = function (message) {
    plugin.say(message);
  };

  helloWorld.remoteAdd = function (a, b) {
    return plugin
      .waitForConnection()
      .call("add", { a: a, b: b }, null)
      .get("sum");
  };

  helloWorld.startMyService = function () {
    return plugin.startMyService();
  };

  helloWorld.stopMyService = function () {
    return plugin.stopMyService();
  };

  return helloWorld;
};
```

> 说明：`plugin.runtime` / `plugin.topLevelScope` 在 Rhino 中可直接按属性访问（等价于 `getRuntime()` / `getTopLevelScope()`），示例中未使用可省略。

---

## 第一代 API（Rhino）调用示例

> 适用：Bot.js 第一代 API，Rhino 引擎。  
> 本示例中的 `helloWorld` 对应插件 `assets/js-plugin/index.js` 中 `module.exports` 返回的对象。  
> 注意：`$plugins.load(name)` 最终按 `PluginRegistry.load(name, ...)` 查找，`name` 必须与 `registerPlugin(name, loader)` 一致。下面假设已注册 `"org.botjs.plugin.sdk.demo"`。

```js
// ============================================================
// Bot.js 第一代 API (Rhino) - 加载并调用插件胶水层示例
// ============================================================

// 1. 加载插件
//    name 必须与 registerPlugin(name, loader) 的 name 一致。
//    加载成功后返回 index.js 胶水层导出的 API 对象。
var helloWorld;
try {
    helloWorld = $plugins.load("org.botjs.plugin.sdk.demo");
    console.log("插件加载成功");
} catch (e) {
    console.error("插件加载失败: " + e);
    // 加载失败属于致命错误，终止脚本，避免后续访问空对象
    throw e;
}

// 2. 读取 getAssetsScriptDir
//    Demo 的 index.js 把它导出为“属性”（字符串），不是方法，所以不加括号。
//    若你的胶水层把它写成函数，请改为 helloWorld.getAssetsScriptDir()。
try {
    var assetsDir = helloWorld.getAssetsScriptDir;
    console.log("getAssetsScriptDir 的值: " + assetsDir);
} catch (e) {
    console.error("读取 getAssetsScriptDir 失败: " + e);
}

// 3. 读取 stringFromJava
//    同样由 index.js 导出为属性（字符串），读取时不加括号。
try {
    var str = helloWorld.stringFromJava;
    console.log("stringFromJava 的值: " + str);
} catch (e) {
    console.error("读取 stringFromJava 失败: " + e);
}

// 4. 调用 say()
//    透传到 Java 层 PluginHelloWorld.say()，会启动 HelloWorldActivity，
//    并通过 Intent extra("message") 把消息传过去。
try {
    helloWorld.say("你好，这是来自 Bot.js 脚本的消息");
    console.log("say() 调用完成");
} catch (e) {
    console.error("say() 调用失败: " + e);
}

// 5. 调用 remoteAdd()
//    通过 Binder 远程调用插件 Service 的 "add" action，同步返回 sum。
//    注意：这是同步阻塞调用，不要在高频循环或主线程中长时间调用。
try {
    var a = 10;
    var b = 32;
    var sum = helloWorld.remoteAdd(a, b);
    console.log("remoteAdd(" + a + ", " + b + ") 返回的 sum: " + sum);
} catch (e) {
    console.error("remoteAdd() 调用失败: " + e);
}

// 6. 启动普通后台服务 MyService
//    返回值只表示 startService() 调用是否成功，不代表服务已进入运行状态。
try {
    var started = helloWorld.startMyService();
    console.log("startMyService() 返回: " + started);
} catch (e) {
    console.error("startMyService() 调用失败: " + e);
}

// 7. 停止普通后台服务 MyService
//    返回值只表示 stopService() 调用是否成功。
try {
    var stopped = helloWorld.stopMyService();
    console.log("stopMyService() 返回: " + stopped);
} catch (e) {
    console.error("stopMyService() 调用失败: " + e);
}

console.log("所有插件 API 调用示例执行完毕");
```

### 预期日志

```
插件加载成功
getAssetsScriptDir 的值: js-plugin
stringFromJava 的值: Hello, Bot.js Pro!
say() 调用完成
remoteAdd(10, 32) 返回的 sum: 42
startMyService() 返回: true
stopMyService() 返回: true
所有插件 API 调用示例执行完毕
```

> 说明：`console.log` 在 Bot.js 日志中通常显示为 `D` 级，`console.error` 为 `E` 级；`say()` 会弹出 `HelloWorldActivity`，请确认该 Activity 已在 `AndroidManifest.xml` 中声明。

---

## 注意事项与已知限制

1. **注册名与加载路径要一致**：`$plugins.load(name)` 对应 `PluginRegistry.load(name, ...)`，只匹配 `registerPlugin(name, loader)`；`registerDefaultPlugin(loader)` 注册的是 `"::default"`，只影响 `loadDefault`。如果 JS 按包名加载，注册代码必须使用同名 `registerPlugin("包名", loader)`。

2. **Plugin 不会自动 bindService**：宿主拿到 `Plugin` 后，需要用 `plugin.getService()` 得到 `ComponentName`，再调用 `bindService(new Intent().setComponent(...), plugin, flags)`。之后 `Plugin.onServiceConnected()` 才会被调用。`Plugin.getService()` 基类默认返回 `null`，需要远程调用时必须重写。

3. **共享 SDK 类与 compileOnly**：`PluginRegistry` 的静态注册表只在同一 `ClassLoader` / 同一进程内共享。动态加载到宿主进程的插件代码应与宿主共享同一份 SDK 类；`compileOnly` 仅适用于宿主/插件框架在运行时确实提供 SDK 类的场景。若插件 APK 的 `PluginService` 运行在独立应用进程，该进程必须能加载 `PluginService` / `Plugin` 等基类，不能只依赖宿主的类。

4. **跨进程参数**：`Map` 只能放 Parcel 支持的类型（基本类型、`String`、`Bundle`、`Parcelable` 等）。不要传 JS 函数、不可序列化对象或超大 Map，否则会失败或触发 `TransactionTooLargeException`。

5. **Service 安全与 Manifest**：同应用/同 UID 场景下可将 Service 设置为 `android:exported="false"`；跨应用宿主需要 bind 时，通常需要 `android:exported="true"` 并加签名级权限，同时在 Binder 调用入口（如 `Stub.onTransact` 或 `call` 开头）校验 `Binder.getCallingUid()` / 调用包名。`HelloworldPluginService`、`MyService`、`HelloWorldActivity` 都应在 `AndroidManifest.xml` 中声明。

6. **连接状态**：`Plugin.onServiceConnected()` 未重置 `mDisconnected`，重连后 `isDisconnected()` 可能仍为 `true`；`waitForConnection()` 无超时，建议在插件侧保证连接前不调用远程方法。

7. **线程模型**：`onRemoteCall` 运行在 Binder 线程，不能做 UI 操作或长耗时任务；`Connection.call()` 是同步阻塞调用。`PluginService` 捕获到异常后会转成 `Result.error`，宿主侧 `Connection.call()` 会因非空 error 抛出 `RemoteException`。

8. **胶水层风格**：Demo 中 `getAssetsScriptDir` 导出为属性，`say` / `remoteAdd` 导出为方法。生产插件建议统一：**对外 API 一律用函数**，属性只用于纯数据，避免调用方误判。例如：

```js
helloWorld.getAssetsScriptDir = function () {
  return plugin.getAssetsScriptDir();
};
```

9. **`PluginUtils.getNotNull`**：使用 unchecked cast，调用方需保证 `args` 中类型正确；key 缺失会抛 `NullPointerException`，类型不匹配可能抛 `ClassCastException`。

10. **日志脱敏**：`Plugin.Connection.call()` 会打印完整 `args`，生产环境注意脱敏。
```