package com.zpj.appmanager.ui.fragment.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zpj.appmanager.R;
import com.zpj.appmanager.ui.widget.InfoCardView;
import com.zpj.blur.ZBlurry;
import com.zpj.fragmentation.ISupportFragment;
import com.zpj.fragmentation.dialog.animator.DialogAnimator;
import com.zpj.fragmentation.dialog.animator.ScaleAlphaAnimator;
import com.zpj.fragmentation.dialog.impl.FullScreenDialogFragment;
import com.zpj.appmanager.constant.AppConfig;
import com.zpj.appmanager.model.InstalledAppInfo;
import com.zpj.utils.DateUtils;

public class PackageDetailDialogFragment extends FullScreenDialogFragment {

    private ImageView ivBg;

    private InstalledAppInfo appInfo;

    public static PackageDetailDialogFragment with(InstalledAppInfo appInfo) {
        PackageDetailDialogFragment fragment = new PackageDetailDialogFragment();
        fragment.appInfo = appInfo;
        return fragment;
    }

    @Override
    protected boolean enableSwipeBack() {
        return true;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.fragment_package_detail;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);

        ivBg = findViewById(R.id.iv_bg);
        ISupportFragment fragment = getPreFragment();
        if (fragment instanceof Fragment) {
            ZBlurry.with(((Fragment)fragment).getView())
                    .backgroundColor(Color.WHITE)
                    .scale(0.2f)
                    .radius(18)
                    .foregroundColor(Color.parseColor(AppConfig.isNightMode() ? "#bb000000" : "#bbffffff"))
                    .blur(bitmap -> {
                        if (ivBg != null) {
                            ivBg.setImageBitmap(bitmap);
                        }
                    });
        }

//        getImplView().setAlpha(0f);

        findViewById(R.id.iv_back).setOnClickListener(view1 -> dismiss());
        findViewById(R.id.iv_info).setOnClickListener(view12 -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", appInfo.getPackageName(), null));
            context.startActivity(intent);
        });

        ImageView ivIcon = findViewById(R.id.iv_icon);
        Glide.with(context).load(appInfo).into(ivIcon);


        TextView tvName = findViewById(R.id.tv_name);
        TextView tvVersionName = findViewById(R.id.tv_version_name);
        TextView tvVersionCode = findViewById(R.id.tv_version_code);

        tvName.setText(appInfo.getName());
        tvVersionName.setText("Version Name: " + appInfo.getVersionName());
        tvVersionCode.setText("Version Code: " + appInfo.getVersionCode());

        InfoCardView cvPackageName = findViewById(R.id.card_package_name);
        InfoCardView cvApkPath = findViewById(R.id.card_apk_path);
        InfoCardView cvTargetSdk = findViewById(R.id.card_target_sdk);
        InfoCardView cvMinSdk = findViewById(R.id.card_min_sdk);
        InfoCardView cvPackageSize = findViewById(R.id.card_package_size);
        InfoCardView cvFirstInstallTime = findViewById(R.id.card_first_install_time);
        InfoCardView cvLastUpdateTime = findViewById(R.id.card_last_update_time);
        cvPackageName.setContent(appInfo.getPackageName());
        cvApkPath.setContent(appInfo.getApkFilePath());
        cvTargetSdk.setContent(String.valueOf(appInfo.getTargetSdk()));
        cvMinSdk.setContent(String.valueOf(appInfo.getMinSdk()));
        cvPackageSize.setContent(appInfo.getFormattedAppSize());
        cvFirstInstallTime.setContent(DateUtils.formatDataTime(appInfo.getFirstInstallTime()));
        cvLastUpdateTime.setContent(DateUtils.formatDataTime(appInfo.getLastUpdateTime()));
    }
//
//    @Override
//    public void doShowAnimation() {
//        getImplView().animate().alpha(1f).setDuration(360).start();
//    }
//
//    @Override
//    public void doDismissAnimation() {
//        getImplView().animate().alpha(0f).setDuration(360).start();
//    }


}
