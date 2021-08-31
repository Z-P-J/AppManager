package com.zpj.appmanager.utils;

import android.util.Log;

import com.zpj.toast.ZToast;

import java.io.File;

public class RootUtil {

    public static boolean isRooted() {
        File file = null;
        String[] paths = {"/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"};
        for (String path : paths) {
            file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

}
