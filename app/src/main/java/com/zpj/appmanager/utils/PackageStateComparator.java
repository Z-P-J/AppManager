package com.zpj.appmanager.utils;

import com.zpj.appmanager.model.InstalledAppInfo;

import java.util.Comparator;

public class PackageStateComparator implements Comparator<InstalledAppInfo> {

    private final PinyinComparator pinyinComparator = new PinyinComparator();

    @Override
    public int compare(InstalledAppInfo o1, InstalledAppInfo o2) {
        if (o1.isDamaged() && o2.isDamaged()) {
            return pinyinComparator.compare(o1, o2);
//                            return Long.compare(o1.getAppSize(), o2.getAppSize());
        } else if (o1.isDamaged()) {
            return -1;
        } else if (o2.isDamaged()) {
            return 1;
        } else {

            boolean isBackup1 = o1.isBackuped();
            boolean isBackup2 = o2.isBackuped();
            if (isBackup1 && isBackup2) {
                return Long.compare(o1.getAppSize(), o2.getAppSize());
            } else if (isBackup1){
                return -1;
            } else if (isBackup2) {
                return 1;
            } else {
                return pinyinComparator.compare(o1, o2);
            }
        }
    }
}
