package com.zpj.appmanager;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.target.ViewTarget;
import com.maning.librarycrashmonitor.MCrashMonitor;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.zpj.appmanager.constant.AppConfig;
import com.zpj.appmanager.utils.EventBus;
import com.zpj.blur.ZBlurry;
import com.zpj.fragmentation.SupportActivity;
import com.zpj.fragmentation.SupportFragment;
import com.zpj.progressbar.ZProgressBar;
import com.zpj.skin.SkinEngine;
import com.zpj.skin.applicator.SkinViewApplicator;
import com.zpj.statemanager.CustomizedViewHolder;
import com.zpj.statemanager.StateManager;
import com.zpj.utils.AppUtils;
import com.zpj.utils.ScreenUtils;
import com.zpj.widget.setting.SimpleSettingItem;
import com.zpj.widget.setting.SwitchSettingItem;

import java.lang.ref.WeakReference;

public class BaseApplication extends MultiDexApplication {

//    private BaseApplication application;

    private static WeakReference<SupportActivity> activityWeakReference;

    @Override
    public void onCreate() {
        super.onCreate();
        SkinEngine.registerSkinApplicator(SimpleSettingItem.class, new SimpleSettingItemApplicator());
        SkinEngine.registerSkinApplicator(SwitchSettingItem.class, new SettingItemApplicator());
//        SkinEngine.registerSkinApplicator(BaseToolBar.class, new ToolbarApplicator());
        int themeId = AppConfig.isNightMode() ? R.style.NightTheme : R.style.DayTheme;
        setTheme(themeId);
        SkinEngine.changeSkin(themeId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

//        DoraemonKit.install(this);

        long start = System.currentTimeMillis();

        //        MFileUtils.setCrashLogPath(Environment.getExternalStorageDirectory().getPath() + "/sjly/Crash/Log");
//        MFileUtils.setCrashPicPath(Environment.getExternalStorageDirectory().getPath() + "/sjly/Crash/ScreenShoot");
        MCrashMonitor.init(this, true, file -> {
//                MCrashMonitor.startCrashShowPage(getContext());
        });

//        String fileProvider = FileUtils.getFileProviderName(this);
//        // 微信设置
//        PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0");
//        PlatformConfig.setWXFileProvider(fileProvider);
//        // QQ设置
//        PlatformConfig.setQQZone("101830139", "5d63ae8858f1caab67715ccd6c18d7a5");
//        PlatformConfig.setQQFileProvider(fileProvider);


        FlowManager.init(this);

        // 配置状态切换StateManager
        StateManager
                .config()
                .setLoadingViewHolder(new CustomizedViewHolder() {
                    @Override
                    public void onViewCreated(View view) {
                        ZProgressBar progressBar = new ZProgressBar(context);
                        progressBar.setProgressBarColor(Color.parseColor("#2ad181"));
                        progressBar.setBorderColor(Color.parseColor("#c0f2d9"));
                        progressBar.setProgressBarRadius(ScreenUtils.dp2px(12));
                        float width = ScreenUtils.dp2px(3.5f);
                        progressBar.setProgressBarWidth(width);
                        progressBar.setBorderWidth(width);
                        int size = ScreenUtils.dp2pxInt(56);
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
                        progressBar.setLayoutParams(params);
                        this.container.addView(progressBar);
                        addTextViewWithPadding(R.string._text_loading, Color.GRAY);
                    }
                });

        ZBlurry.init(this);

        ViewTarget.setTagId(R.id.glide_tag_id);
//        Glide.with(getApplicationContext())
//                .setDefaultRequestOptions(GlideUtils.REQUEST_OPTIONS).applyDefaultRequestOptions()

        Log.d("AppAppApp", "signature=" + AppUtils.getAppSignatureMD5(this, getPackageName()));
        Log.d("AppAppApp", "duration=" + (System.currentTimeMillis() - start));

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (activity instanceof SupportActivity) {
                    activityWeakReference = new WeakReference<>((SupportActivity) activity);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

    }

    public static void startFragment(SupportFragment fragment) {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().start(fragment);
        } else {
            EventBus.post(fragment);
        }
    }

    public static class SettingItemApplicator extends SkinViewApplicator {
        public SettingItemApplicator() {
            super();
            addAttributeApplicator("z_setting_titleTextColor", new IAttributeApplicator<SwitchSettingItem>() {
                @Override
                public void onApply(SwitchSettingItem view, TypedArray typedArray, int typedArrayIndex) {
                    view.setTitleTextColor(typedArray.getColor(typedArrayIndex, AppConfig.isNightMode() ? Color.LTGRAY : Color.BLACK));
                }
            });
        }
    }

    public static class SimpleSettingItemApplicator extends SkinViewApplicator {
        public SimpleSettingItemApplicator() {
            super();
            addAttributeApplicator("z_setting_titleTextColor", new IAttributeApplicator<SimpleSettingItem>() {
                @Override
                public void onApply(SimpleSettingItem view, TypedArray typedArray, int typedArrayIndex) {
                    view.setTextColor(typedArray.getColorStateList(typedArrayIndex));
                }
            });
        }
    }

//    public static class ToolbarApplicator extends SkinViewApplicator {
//        public ToolbarApplicator() {
//            super();
//            addAttributeApplicator("z_toolbar_titleBarColor", new IAttributeApplicator<BaseToolBar>() {
//                @Override
//                public void onApply(BaseToolBar view, TypedArray typedArray, int typedArrayIndex) {
//                    view.setBackgroundColor(typedArray.getColor(typedArrayIndex, ThemeUtils.getDefaultBackgroundColor(view.getContext())), false);
//                }
//            });
//            addAttributeApplicator("z_toolbar_statusBarColor", new IAttributeApplicator<BaseToolBar>() {
//                @Override
//                public void onApply(BaseToolBar view, TypedArray typedArray, int typedArrayIndex) {
//                    view.setStatusBarColor(typedArray.getColor(typedArrayIndex, ThemeUtils.getDefaultBackgroundColor(view.getContext())));
//                }
//            });
//        }
//    }

}
