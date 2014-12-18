package com.phonegap.plugins.updateapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simpleevent.xattender.R;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class UpdateApp extends CordovaPlugin {

    /* 版本号检查路径 */
    private String checkPath;
    /* 新版本号 */
    private int newVerCode;
    /* 新版本名称 */
    private String newVerName;
    /* APK 下载路径 */
    private String downloadPath;
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;
    /* 上下文 */
    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;

    protected static final String LOG_TAG = "UpdateApp";

    private Resources resources; // 资源
    private Configuration config; // 配置
    private DisplayMetrics dm; // 屏幕
    private SharedPreferences setting; // 存储
    private Editor editor; // 存储编辑器

    private String soft_update_no = "已经是最新版本";
    private String soft_update_title = "软件更新";
    private String soft_update_info = "检测到新版本，立即更新吗？";
    private String soft_update_updatebtn = "更新";
    private String soft_update_later = "稍后更新";
    private String soft_updating = "正在更新";
    private String soft_update_cancel = "取消";

    private JSONObject jsonObj = null;
    private String fileName = ""; // 多语言文件路径

    // <string name="soft_update_no">已经是最新版本</string>
    // <string name="soft_update_title">软件更新</string>
    // <string name="soft_update_info">检测到新版本，立即更新吗？</string>
    // <string name="soft_update_updatebtn">更新</string>
    // <string name="soft_update_later">稍后更新</string>
    // <string name="soft_updating">正在更新</string>
    // <string name="soft_update_cancel">取消</string>

    @Override
    public boolean execute(String action, JSONArray args,
            final CallbackContext callbackContext) throws JSONException {
        this.mContext = cordova.getActivity();

        JSONArray arr = args.getJSONArray(0);

        this.checkPath = arr.getString(0);
        String curLanguage = arr.getString(1);
        setLanguage(curLanguage);

        // String fileName = "res-plugin/updateapp/values-zh-rCN/string.json";
        String strJson = getJson(fileName);
        Log.i("strJson  >>>  ", strJson);
        jsonObj = new JSONObject(strJson);

        Log.d("", jsonObj.getString("soft_update_title"));

        // try {
        // AssetManager am = mContext.getResources().getAssets();
        // //InputStream is =
        // am.open("res-plugin/updatexx/values-zh-rCN/string.json");
        // InputStream inputStream =
        // mContext.getResources().getAssets().open("res-plugin/updatexx/values-zh-rCN/string.json");
        // InputStreamReader inputStreamReader = new
        // InputStreamReader(inputStream, "UTF-8");
        // BufferedReader bufferedReader = new
        // BufferedReader(inputStreamReader);
        // String info = "";
        // while ((info = bufferedReader.readLine()) != null) {
        // Log.i("fff", info);
        // //Toast.makeText(MainActivity.this, info, 1000).show();
        // }
        // //mContext.getAssets().open("sample.txt");
        // }catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // InputStream in =
        // mContext.getResources().openRawResource(R.raw.enstrings);
        //
        // BufferedReader br = new BufferedReader(new InputStreamReader(in));
        // String str = null;
        // try {
        // while ((str = br.readLine()) != null) {
        // Log.e("str >>>>>>> ", str);
        // }
        // in.close();
        // br.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        //

        // getResources().getString(R.string.value);
        // resources = mContext.getResources();

        // String ss =
        // mContext.getResources().getString(R.string.soft_update_info);
        // Log.e("ss >>>> ", ss);

        if ("checkAndUpdate".equals(action)) {
            // this.checkPath = args.getString(0);
            checkAndUpdate();
            return true;
        } else if ("getCurrentVersion".equals(action)) {
            callbackContext.success(this.getCurrentVerCode() + "");
            return true;
        } else if ("getServerVersion".equals(action)) {
            // this.checkPath = args.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    if (getServerVerInfo()) {
                        callbackContext.success(newVerCode + "");
                    } else {
                        callbackContext
                                .error("can't connect to the server!please check [checkpath]");
                    }
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 读取本地文件中JSON字符串
     * 
     * @param fileName
     * @return
     */
    private String getJson(String mFileName) {

        StringBuilder stringBuilder = new StringBuilder();
        AssetManager am = mContext.getResources().getAssets();
        String info = "";
        try {
            // InputStream is =
            // am.open("res-plugin/updatexx/values-zh-rCN/string.json");
            // InputStream inputStream =
            // mContext.getResources().getAssets().open("res-plugin/updateapp/values-zh-rCN/string.json");

            InputStream inputStream = mContext.getResources().getAssets()
                    .open(mFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line;
            while ((info = bufferedReader.readLine()) != null) {
                Log.i("fff", info);
                // Toast.makeText(MainActivity.this, info, 1000).show();
                stringBuilder.append(info);
            }
            // mContext.getAssets().open("sample.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
        // return info.toString();
    }

    /**
     * 2 * 将JSON字符串转化为Adapter数据 3 * 4 * @param str 5
     */
    private void setData(String str) {
        try {
            JSONArray array = new JSONArray(str);
            int len = array.length();
            Map<String, String> map;
            for (int i = 0; i < len; i++) {
                JSONObject object = array.getJSONObject(i);
                // map = new HashMap<String, String>();
                // map.put("operator", object.getString("operator"));
                // map.put("loginDate", object.getString("loginDate"));
                // map.put("logoutDate", object.getString("logoutDate"));
                // data.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setLanguage(String curLanguage) {
        resources = this.mContext.getResources();// 获得res资源对象
        config = resources.getConfiguration();// 获得设置对象
        dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。

        // setting = getSharedPreferences("setting", Activity.MODE_PRIVATE); //
        // 得到存储
        // editor = setting.edit(); // 得到存储编辑器
        // int language = setting.getInt("language", 1); // 获得存储内容，并给默认值(默认英语)

        if (curLanguage.equals("zh-Hans")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);

            fileName = "res-plugin/updateapp/values-zh-rCN/string.json";
        } else if (curLanguage.equals("en-US")) {
            config.locale = Locale.ENGLISH;
            resources.updateConfiguration(config, dm);
            fileName = "res-plugin/updateapp/values/string.json";

        } else {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);
            fileName = "res-plugin/updateapp/values-zh-rCN/string.json";
        }
        Log.e(" 当前使用语言>>>> ", curLanguage);

    }

    /**
     * 检查更新
     */
    private void checkAndUpdate() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (getServerVerInfo()) {
                    int currentVerCode = getCurrentVerCode();
                    if (newVerCode > currentVerCode) {
                        showNoticeDialog();
                    }
                }
            }
        };
        this.cordova.getThreadPool().execute(runnable);
    }

    /**
     * 获取应用当前版本代码
     * 
     * @param context
     * @return
     */
    private int getCurrentVerCode() {
        String packageName = this.mContext.getPackageName();
        int currentVer = -1;
        try {
            currentVer = this.mContext.getPackageManager().getPackageInfo(
                    packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.d(LOG_TAG, "获取应用当前版本代码异常：" + e.toString());
        }
        return currentVer;
    }

    /**
     * 获取服务器上的版本信息
     * 
     * @param path
     * @return
     * @throws Exception
     */
    private boolean getServerVerInfo() {
        try {
            StringBuilder verInfoStr = new StringBuilder();
            URL url = new URL(checkPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"), 8192);
            String line = null;
            while ((line = reader.readLine()) != null) {
                verInfoStr.append(line + "\n");
            }
            reader.close();

            JSONArray array = new JSONArray(verInfoStr.toString());
            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                newVerCode = obj.getInt("verCode");
                newVerName = obj.getString("verName");
                downloadPath = obj.getString("apkPath");
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "获取服务器上的版本信息异常：" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        Runnable runnable = new Runnable() {
            public void run() {
                // 构造对话框
                AlertDialog.Builder builder = new Builder(mContext);
                try {
                    builder.setTitle(jsonObj.getString("soft_update_title"));
                    builder.setMessage(jsonObj.getString("soft_update_info"));

                    // 更新
                    builder.setPositiveButton(
                            jsonObj.getString("soft_update_updatebtn"),
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                    // 显示下载对话框
                                    showDownloadDialog();
                                }
                            });
                    // 稍后更新
                    builder.setNegativeButton(
                            jsonObj.getString("soft_update_later"),
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
                    Dialog noticeDialog = builder.create();
                    noticeDialog.show();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        try {
            // 构造软件下载对话框
            AlertDialog.Builder builder = new Builder(mContext);
            builder.setTitle(jsonObj.getString("soft_updating"));

            // 给下载对话框增加进度条
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.softupdate_progress, null);
            mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
            builder.setView(v);
            // 取消更新
            builder.setNegativeButton(jsonObj.getString("soft_update_cancel"),
                    new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // 设置取消状态
                            cancelUpdate = true;
                        }
                    });
            mDownloadDialog = builder.create();
            mDownloadDialog.show();
            // 现在文件
            downloadApk();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            // 正在下载
            case DOWNLOAD:
                // 设置进度条位置
                mProgress.setProgress(progress);
                break;
            case DOWNLOAD_FINISH:
                // 安装文件
                installApk();
                break;
            default:
                break;
            }
        };
    };

    /**
     * 下载文件线程
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory()
                            + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(downloadPath);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, newVerName);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                } else {
                    Log.d(LOG_TAG, "手机没有SD卡");
                }
            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "下载文件线程异常MalformedURLException：" + e.toString());
            } catch (IOException e) {
                Log.d(LOG_TAG, "下载文件线程异常IOException：" + e.toString());
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    };

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePath, newVerName);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}