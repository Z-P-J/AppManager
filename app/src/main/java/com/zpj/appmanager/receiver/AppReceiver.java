package com.zpj.appmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.zpj.bus.ZBus;

public class AppReceiver extends BroadcastReceiver {

    private static AppReceiver APP_RECEIVER;

//    public static AppReceiver getInstance() {
//        if (APP_RECEIVER == null) {
//            synchronized (AppReceiver.class) {
//                if (APP_RECEIVER == null) {
//                    APP_RECEIVER = new AppReceiver();
//                }
//            }
//        }
//        return APP_RECEIVER;
//    }

    public static void register(Context context) {
        if (APP_RECEIVER == null) {
            synchronized (AppReceiver.class) {
                if (APP_RECEIVER == null) {
                    APP_RECEIVER = new AppReceiver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                    intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                    intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                    intentFilter.addDataScheme("package");
                    context.registerReceiver(APP_RECEIVER, intentFilter);
                }
            }
        }
    }

    public static void unregister(Context context) {
        if (APP_RECEIVER != null) {
            synchronized (AppReceiver.class) {
                if (APP_RECEIVER != null) {
                    context.unregisterReceiver(APP_RECEIVER);
                    APP_RECEIVER = null;
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        // 接受更新安装广播
//        if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction())) {
//            String packageName = intent.getDataString();
//            ZToast.warning("安装了:" + packageName + "包名的程序");
//        }
//        //接收安装广播
//        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
//            String packageName = intent.getDataString();
//            ZToast.warning("安装了:" + packageName + "包名的程序");
//            ZNotify.with(context)
//                    .buildNotify()
//                    .setId(hashCode())
//                    .setContentTitle("安装成功")
//                    .setContentText("应用" + packageName + "安装成功")
//                    .show();
//        }
//        //接收卸载广播
//        if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
//            String packageName = intent.getDataString();
//            ZToast.warning("卸载了:" + packageName + "包名的程序");
//        }

        if (intent != null) {
            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                    || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())
                    || Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                ZBus.post(intent.getData().getSchemeSpecificPart(), intent.getAction());
                Log.d("AppReceiver", "安装的app的包名是-------->" + intent.getDataString() + " intent.getData().getSchemeSpecificPart()=" + intent.getData().getSchemeSpecificPart());
//                if (TextUtils.equals(packageName, intent.getData().getSchemeSpecificPart())) {
//
//                }
            }
        }
    }
}
