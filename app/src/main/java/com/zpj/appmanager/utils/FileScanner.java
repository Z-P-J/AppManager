package com.zpj.appmanager.utils;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class FileScanner<T> {

    private static final String TAG = "ZFileScanner";

    private final ConcurrentLinkedQueue<File> folderList = new ConcurrentLinkedQueue<>();

    private final List<ScannerTask> taskList = new ArrayList<>();

    private String type;

    private class ScannerTask {

        private final CountDownLatch latch;
        private final OnScanListener<T> onScanListener;
        private final String type;
        private Future<?> future;

        public ScannerTask(CountDownLatch latch, OnScanListener<T> onScanListener,
                           String type) {
            this.latch = latch;
            this.onScanListener = onScanListener;
            this.type = type;
            start();
        }

        public void stop() {
            if (future != null) {
                future.cancel(true);
            }
        }

        public void start() {
            future = ThreadPoolUtils.submit(() -> {
                while (!folderList.isEmpty()) {
                    File file = folderList.poll();
                    if (file != null) {
                        if (file.isDirectory()) {
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    Log.d(TAG, "file=" + file.getAbsolutePath());
                                    if (f.isDirectory()) {
                                        folderList.add(f);
                                    } else {
                                        if (onScanListener != null) {
                                            if (f.getName().toLowerCase().endsWith(type)) {
                                                T item = onScanListener.onWrapFile(f);
                                                if (item != null) {
                                                    ThreadPoolUtils.post(() -> onScanListener.onScanningFiles(item));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (onScanListener != null) {
                                if (file.getName().toLowerCase().endsWith(type)) {
                                    T item = onScanListener.onWrapFile(file);
                                    if (item != null) {
                                        ThreadPoolUtils.post(() -> onScanListener.onScanningFiles(item));
                                    }
                                }
                            }
                        }
                    }

                }
                synchronized (taskList) {
                    taskList.remove(ScannerTask.this);
                }
                latch.countDown();
                future = null;
            });
        }

    }

    public FileScanner<T> setType(@NonNull String type) {
        this.type = type.toLowerCase();
        return this;
    }

    public FileScanner<T> bindLife(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy(LifecycleOwner owner) {
                synchronized (taskList) {
                    for (ScannerTask task : taskList) {
                        task.stop();
                    }
                    taskList.clear();
                }
            }

        });
        return this;
    }

    public void start(final OnScanListener<T> onScanListener) {
//        this.onScanListener = onScanListener;
        folderList.clear();
        taskList.clear();
        if (TextUtils.isEmpty(type)) {
            return;
        }
        ThreadPoolUtils.execute(() -> {
            if (onScanListener != null) {
                onScanListener.onScanBegin();
            }

            File file = Environment.getExternalStorageDirectory();
            folderList.addAll(Arrays.asList(file.listFiles()));
            CountDownLatch latch = new CountDownLatch(3);
            for (int i = 0; i < 3; i++) {
                taskList.add(new ScannerTask(latch, onScanListener, type));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (onScanListener != null) {
                onScanListener.onScanEnd();
            }
        });
    }

    public interface OnScanListener<T> {

        /**
         * 扫描开始
         */
        void onScanBegin();

        /**
         * 扫描结束
         */
        void onScanEnd();

        /**
         * 扫描进行中
         *
         * @param paramString 文件夹地址
         * @param progress    扫描进度
         */
        void onScanning(String paramString, int progress);

        T onWrapFile(File file);

        /**
         * 扫描进行中，文件的更新
         *
         */
        void onScanningFiles(T item);

    }


}
