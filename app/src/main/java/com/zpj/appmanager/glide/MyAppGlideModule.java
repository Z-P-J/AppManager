package com.zpj.appmanager.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;
import com.zpj.appmanager.glide.apk.ApkModelLoaderFactory;
import com.zpj.appmanager.model.InstalledAppInfo;

import java.io.InputStream;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade(500));
        builder.setDefaultTransitionOptions(Bitmap.class, BitmapTransitionOptions.withCrossFade(500));
//        builder.setDefaultRequestOptions(new RequestOptions().skipMemoryCache(true));

    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.prepend(InstalledAppInfo.class, InputStream.class, new ApkModelLoaderFactory(context));
    }
}
