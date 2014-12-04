
##cordova-plugin-updateapp
================
#### 备注: by mbyte 优化R.String 依赖

把：import com.simpleevent.xattender.R;
   换成自己的包名；
   或者把用到的 R.string  直接在UpdateApp.java 里定义，这样不依赖于 string.xml 文件了；


基于cordova插件形式开发的版本升级插件，支持android和iOS。

####说明：
========
+ 该版本插件android和iOS均是基于服务器上的版本信息文件
+ iOS的更新是基于企业证书签名，内网分发的应用编写的
+ iOS下需要基于appstore的lookup接口做检查更新的话，这个插件不适用，需要稍微做些修改

####安装：
========
1. `cordova plugin add https://github.com/south-pacific/cordova-plugin-updateapp.git`
2. `cordova build android` 或者 `cordova build ios`
3. 在服务器上放置版本信息文件`androidVersion.json` 或者 `iosVersion.json`

####使用：
================
Javascript
// 获取APP当前版本号
window.plugins.updateApp.getCurrentVerInfo(function (currentVersionCode) {
    console.log(currentVersionCode);
});

// 获取服务器上APP的版本号，versionServer为版本信息文件地址
window.plugins.updateApp.getServerVerInfo(function (serverVersionCode) {
    console.log(serverVersionCode);
}, function () {
    console.log("出现异常");
}, versionServer);

// 检查并更新，versionServer为版本信息文件地址
window.plugins.updateApp.checkAndUpdate(versionServer);

####androidVersion.json:
=========
`[{"verCode":"最新版apk的versionCode","verName":"最新版apk的versionName","apkPath":"apk的地址"}]`

iosVersion.json:
=========
`{"verName":"最新版ipa的版本号","ipaPath":"ipa的地址"}`

