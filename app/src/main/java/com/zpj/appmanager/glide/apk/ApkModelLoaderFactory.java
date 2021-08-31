package com.zpj.appmanager.glide.apk;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.zpj.appmanager.model.InstalledAppInfo;

import java.io.InputStream;

public class ApkModelLoaderFactory implements ModelLoaderFactory<InstalledAppInfo, InputStream> {

    private final Context context;

    public ApkModelLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<InstalledAppInfo, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new ApkIconModelLoader(context);
    }

    @Override
    public void teardown() {

    }

}