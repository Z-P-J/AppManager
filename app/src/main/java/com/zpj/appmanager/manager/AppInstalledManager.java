package com.zpj.appmanager.manager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.zpj.appmanager.model.InstalledAppInfo;
import com.zpj.appmanager.utils.ThreadPoolUtils;
import com.zpj.utils.ContextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppInstalledManager { //  extends BroadcastReceiver

    private static final String TAG = AppInstalledManager.class.getName();

    private volatile static AppInstalledManager manager;

    private final List<InstalledAppInfo> installedAppInfoList = new ArrayList<>();
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private final AtomicBoolean isLoaded = new AtomicBoolean(false);

    private final List<WeakReference<CallBack>> callbacks = new ArrayList<>();

    public static AppInstalledManager getInstance() {
        if (manager == null) {
            synchronized (AppInstalledManager.class) {
                if (manager == null) {
                    manager = new AppInstalledManager();
                }
            }
        }
        return manager;
    }

    private AppInstalledManager() {
        isLoaded.set(false);
        isLoading.set(false);
    }

    private synchronized void onAppRemoved(String packageName) {
        for (InstalledAppInfo info : installedAppInfoList) {
            if (info.getPackageName().equals(packageName)) {
                installedAppInfoList.remove(info);
                return;
            }
        }
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        //接收安装广播
//        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
//            String packageName = intent.getDataString();
//            System.out.println();
//            ZToast.warning("安装了:" + packageName + "包名的程序");
//            new HttpObserver<>(emitter -> {
//                PackageManager packageManager = context.getPackageManager();
//                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
//                getInstance().onAppAdded(packageManager, packageInfo);
//            }).subscribe();
//        }
//        //接收卸载广播
//        if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
//            String packageName = intent.getDataString();
//            ZToast.warning("卸载了:" + packageName + "包名的程序");
//            new HttpObserver<>(emitter -> getInstance().onAppRemoved(packageName)).subscribe();
//        }
//    }

    public AppInstalledManager addListener(CallBack callBack) {
        synchronized (callbacks) {
            callbacks.add(new WeakReference<>(callBack));
        }
        return this;
    }

    public void removeListener(CallBack callBack) {
        synchronized (callbacks) {
            for (WeakReference<CallBack> appBackupListener : callbacks) {
                if (appBackupListener.get() != null && appBackupListener.get() == callBack) {
                    callbacks.remove(appBackupListener);
                    break;
                }
            }
        }
    }

    public void onDestroy() {
        manager = null;
        installedAppInfoList.clear();
        callbacks.clear();
        isLoaded.set(false);
        isLoading.set(false);
    }

    private void onNext(InstalledAppInfo installedAppInfo) {
        synchronized (callbacks) {
            for (WeakReference<CallBack> callBackWeakReference : callbacks) {
                CallBack callBack = callBackWeakReference.get();
                onNext(callBack, installedAppInfo);
            }
        }
    }

    private void onNext(CallBack callBack, InstalledAppInfo installedAppInfo) {
        if (callBack != null) {
            if (installedAppInfo.isUserApp()) {
                // 非系统应用
                callBack.onGetUserApp(installedAppInfo);
            } else {
                // 系统应用
                callBack.onGetSystemApp(installedAppInfo);
            }

            if (installedAppInfo.isBackuped()) {
                // 已备份
                callBack.onGetBackupApp(installedAppInfo);
            }

            if (!installedAppInfo.isEnabled()) {
                // 已禁用
                callBack.onGetForbidApp(installedAppInfo);
            }

        }
    }

    private void onFinished() {
        synchronized (callbacks) {
            for (WeakReference<CallBack> callBackWeakReference : callbacks) {
                if (callBackWeakReference.get() != null) {
                    callBackWeakReference.get().onLoadAppFinished();
                }
            }
            callbacks.clear();
        }
    }

    public void loadApps(CallBack callBack) {
        if (callBack == null) {
            loadApps();
            return;
        }
        if (isLoaded.get() && !isLoading.get()) {
            for (InstalledAppInfo appInfo : installedAppInfoList) {
                onNext(callBack, appInfo);
            }
            callBack.onLoadAppFinished();
        } else if (!isLoaded.get() && !isLoading.get()) {
            addListener(callBack);
            loadApps();
        } else {
            synchronized (callbacks) {
                if (isLoaded.get() && !isLoading.get()) {
                    loadApps(callBack);
                } else {
                    callbacks.add(new WeakReference<>(callBack));
                }
            }
        }
    }

    public void loadApps() {
        if (isLoaded.get() && !isLoading.get()) {
            return;
        }
        isLoaded.set(false);
        isLoading.set(true);
        installedAppInfoList.clear();

        ThreadPoolUtils.execute(() -> {
            PackageManager manager = ContextUtils.getApplicationContext().getPackageManager();
            List<PackageInfo> packageInfoList = manager.getInstalledPackages(0);
            List<InstalledAppInfo> installedAppInfos = new ArrayList<>();
            for (PackageInfo packageInfo : packageInfoList) {
                installedAppInfos.add(InstalledAppInfo.parseFromPackageInfo(manager, packageInfo));
            }

            ThreadPoolUtils.post(() -> {
                if (manager == null) {
                    return;
                }
                installedAppInfoList.addAll(installedAppInfos);
                isLoaded.set(true);
                isLoading.set(false);
                for (InstalledAppInfo info : installedAppInfos) {
                    onNext(info);
                }
                onFinished();
            });

        });
    }

    public interface CallBack {
        void onGetUserApp(InstalledAppInfo appInfo);

        void onGetSystemApp(InstalledAppInfo appInfo);

        void onGetBackupApp(InstalledAppInfo appInfo);

        void onGetForbidApp(InstalledAppInfo appInfo);

        void onGetHiddenApp(InstalledAppInfo appInfo);

        void onLoadAppFinished();
    }

}
