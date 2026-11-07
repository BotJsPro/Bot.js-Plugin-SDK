// 在assets的相应目录(由Plugin.getAssetsScriptDir返回)中添加index.js文件，用于作为胶水层导出插件API。
module.exports = function (plugin) {
  // 获取插件的运行时环境
  let runtime = plugin.runtime;
  // 获取插件的顶层作用域
  let scope = plugin.topLevelScope;

  // 定义一个函数 helloWorld
  function helloWorld() {}
  helloWorld.getAssetsScriptDir = plugin.getAssetsScriptDir();
  // 将从Java代码获取的字符串赋给 helloWorld 对象的 stringFromJava 属性
  helloWorld.stringFromJava = plugin.getStringFromJava();
  // 定义 helloWorld 对象的 say 方法，该方法调用插件的 say 方法
  helloWorld.say = function (message) {
    plugin.say(message);
  };
  // 定义 helloWorld 对象的 remoteAdd 方法，用于远程调用插件的 add 方法
  helloWorld.remoteAdd = function (a, b) {
    // 等待连接，并调用远程的 'add' 方法，传递参数 a 和 b
    return plugin
      .waitForConnection()
      .call(
        "add",
        {
          a: a,
          b: b,
        },
        null
      )
      .get("sum"); // 返回结果中的 sum 属性
  };
  helloWorld.startMyService = function () {
   return plugin.startMyService();
  };

  helloWorld.stopMyService = function () {
   return plugin.stopMyService();
  };

  // 返回 helloWorld 对象，作为导出的插件API
  return helloWorld;
};
