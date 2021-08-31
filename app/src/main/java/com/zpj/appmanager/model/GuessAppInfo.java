package com.zpj.appmanager.model;

import android.support.annotation.Keep;

import com.zpj.appmanager.utils.BeanUtils.Select;

@Keep
public class GuessAppInfo {

    @Select(selector = "icon")
    private String appIcon;
    @Select(selector = "title")
    private String appTitle;
    @Select(selector = "id")
    private String appId;
    @Select(selector = "subviewtype")
    private String appViewType;
    @Select(selector = "subapptype")
    private String appType;
    @Select(selector = "package")
    private String appPackage;
    @Select(selector = "m")
    private String appSize;
    @Select(selector = "comment")
    private String appComment;

    public String getPackageName() {
        return appPackage;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getAppComment() {
        return appComment;
    }

    public void setAppComment(String appComment) {
        this.appComment = appComment;
    }


}
