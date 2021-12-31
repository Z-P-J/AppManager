package com.zpj.appmanager.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.lxj.xpermission.PermissionConstants;
import com.lxj.xpermission.XPermission;
import com.zpj.appmanager.R;
import com.zpj.appmanager.ui.fragment.manager.ManagerFragment;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.appmanager.constant.AppConfig;
import com.zpj.appmanager.manager.AppBackupManager;
import com.zpj.appmanager.manager.AppInstalledManager;
import com.zpj.appmanager.receiver.AppReceiver;
import com.zpj.appmanager.utils.EventBus;
import com.zpj.toast.ZToast;
import com.zpj.utils.BrightnessUtils;
import com.zpj.utils.StatusBarUtils;

public class MainActivity extends BaseActivity {

    private ManagerFragment mainFragment;

    private FrameLayout flContainer;

    @Override
    protected void onDestroy() {
        AppReceiver.unregister(this);
        AppBackupManager.getInstance().onDestroy();
        AppInstalledManager.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.onGetActivityEvent(this);
        AppReceiver.register(this);

        flContainer = findViewById(R.id.fl_container);
        flContainer.setOnTouchListener((v, event) -> true);

        BrightnessUtils.setBrightness(this);

        StatusBarUtils.transparentStatusBar(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            getWindow().setNavigationBarColor(AppConfig.isNightMode() ? Color.BLACK : Color.WHITE);


        XPermission.create(this, PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        mainFragment = findFragment(ManagerFragment.class);
                        if (mainFragment == null) {
                            mainFragment = new ManagerFragment();
                            loadRootFragment(R.id.fl_container, mainFragment);
                        }
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    }

                    @Override
                    public void onDenied() {
                        ZToast.warning("请授予读取手机信息权限！");
                        finish();
                    }
                }).request();

    }

    private void handleIntent(Intent intent) {
    }

    private void showRequestPermissionPopup() {
        if (hasStoragePermissions(getApplicationContext())) {
            requestPermission();
        } else {
            ZDialog.alert()
                    .setTitle("权限申请")
                    .setContent("本软件需要读写手机存储的权限用于文件的下载与查看，是否申请该权限？")
                    .setPositiveButton("去申请", (fragment, which) -> requestPermission())
                    .setNegativeButton("拒绝", (fragment, which) -> ActivityCompat.finishAfterTransition(MainActivity.this))
                    .show(this);
        }
    }

    private boolean hasStoragePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        XPermission.create(getApplicationContext(), PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                        flContainer.setOnTouchListener(null);

                        postDelayed(() -> handleIntent(getIntent()), 250);
                    }

                    @Override
                    public void onDenied() {
                        showRequestPermissionPopup();
                    }
                }).request();
    }


//    @Override
//    public void onComplete(Object o) {
//        EventBus.showLoading("QQ第三方登录中...");
//        JSONObject jsonObject = (JSONObject) o;
//        Log.e("onComplete", "jsonObject: " + jsonObject.toString());
//        try {
//            //得到token、expires、openId等参数
//            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
//            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
//            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
//
//            getTencent().setAccessToken(token, expires);
//            getTencent().setOpenId(openId);
//            Log.e("onComplete", "token: " + token);
//            Log.e("onComplete", "expires: " + expires);
//            Log.e("onComplete", "openId: " + openId);
//
//            //获取个人信息
//            getQQInfo();
//        } catch (Exception e) {
//        }
//    }
//
//    @Override
//    public void onError(UiError uiError) {
//        ZToast.error("QQ登录失败！" + uiError.errorMessage);
//    }
//
//    @Override
//    public void onCancel() {
//        ZToast.warning("QQ登录取消");
//    }
//
//    @Override
//    public void onWarning(int i) {
//
//    }
//
//
//    private void getQQInfo() {
//        //获取基本信息
//        QQToken qqToken = getTencent().getQQToken();
//        UserInfo info = new UserInfo(this, qqToken);
//        info.getUserInfo(new IUiListener() {
//            @Override
//            public void onComplete(Object object) {
//                Log.e("onComplete", "个人信息：" + object.toString());
//
//                try {
//                    JSONObject userInfo = (JSONObject) object;
//                    String nickName = userInfo.getString("nickname");
//                    String logo1 = userInfo.getString("figureurl_qq_1");
//                    String logo2 = userInfo.getString("figureurl_qq_2");
//                    Log.d("onComplete", "openId=" + getTencent().getOpenId().trim());
//                    Log.d("onComplete", "nickname=" + nickName);
//                    Log.d("onComplete", "logo1=" + logo1);
//                    Log.d("onComplete", "logo1=" + logo2);
//                    UserManager.getInstance().signInByQQ(getTencent().getOpenId(), nickName, logo1, logo2);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    EventBus.hideLoading();
//                    ZToast.error("QQ登录失败！" + e.getMessage());
//                }
//
//            }
//
//            @Override
//            public void onError(UiError uiError) {
//                ZToast.error("QQ登录失败！" + uiError.errorMessage);
//            }
//
//            @Override
//            public void onCancel() {
//            }
//
//            @Override
//            public void onWarning(int i) {
//
//            }
//        });
//    }

}
