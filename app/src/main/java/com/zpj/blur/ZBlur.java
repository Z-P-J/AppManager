package com.zpj.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZBlur implements ViewTreeObserver.OnPreDrawListener {

    private final static String TAG = "ZBlur";

    private static final Executor sExecutor = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE, 500,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
    );

    private final RenderScript mRenderScript;
    private final ScriptIntrinsicBlur mIntrinsicBlur;

    private final View mBlurView;
    private View mIntoView;

    private float mScale = 0.1f;
    private int mRadius = 8;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mForegroundColor = Color.TRANSPARENT;
    private Callback mCallback;

    private long lastTime;

    private ZBlur(@NonNull View blurView) {
        this.mBlurView = blurView;
        Context context = blurView.getContext();

        mRenderScript = RenderScript.create(context);
        mIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));

    }

    public static ZBlur with(@NonNull View view) {
        return new ZBlur(view);
    }

    public ZBlur setScale(float mScale) {
        this.mScale = mScale;
        return this;
    }

    public ZBlur setRadius(int mRadius) {
        this.mRadius = mRadius;
        return this;
    }

    public ZBlur setBackgroundColor(int color) {
        mBackgroundColor = color;
        return this;
    }

    public ZBlur setForegroundColor(int color) {
        mForegroundColor = color;
        return this;
    }

    public Bitmap blur(Bitmap bitmap) {
        return blur(scaleBitmap(new Canvas(), bitmap, mScale), mRadius);
    }

    public void blur(@NonNull View intoView, Callback callback) {
        this.mIntoView = intoView;
        this.mCallback = callback;
//        intoView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//                Log.d(TAG, "onViewAttachedToWindow");
//                mBlurView.getViewTreeObserver().addOnPreDrawListener(ZBlur.this);
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//                Log.d(TAG, "onViewDetachedFromWindow");
//                mBlurView.getViewTreeObserver().removeOnPreDrawListener(ZBlur.this);
//            }
//        });

        Choreographer.getInstance().postFrameCallback(invalidationLoop);

//        intoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                drawRunnable.run();
//                return true;
//            }
//        });

    }

    public void pauseBlur() {

    }

    public void startBlur() {

    }

    private final Choreographer.FrameCallback invalidationLoop = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            Choreographer.getInstance().postFrameCallbackDelayed(this, 1000 / 60);
            drawRunnable.run();
        }
    };

    @Override
    public boolean onPreDraw() {
        long time = SystemClock.elapsedRealtime();
        float fps = 1000f / (time - lastTime);
        if (fps > 60f) {
            return true;
        }
        lastTime = time;
//        sExecutor.execute(drawRunnable);
        drawRunnable.run();
        return true;
    }

    public interface Callback {
        @WorkerThread
        void onGetBitmap(Bitmap bitmap);
    }

    private final Runnable drawRunnable = new Runnable() {

        private final Rect mBlurRect = new Rect();
        private final Rect mViewRect = new Rect();
        private final Canvas c = new Canvas();
        private Bitmap mBitmap;

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            if (mBlurView == null || mCallback == null) {
                return;
            }

            if (mIntoView != null && (mIntoView.getWidth() < 1 || mIntoView.getHeight() < 1)) {
                return;
            }

            if (mBitmap == null) {
                Bitmap bitmap = null;
                try {
                    synchronized (c) {
                        synchronized (mBlurView) {
                            bitmap = snapshot(c, mBlurView, mBackgroundColor, mForegroundColor, mScale);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (bitmap == null) {
                    return;
                }
                mBitmap = bitmap;
            }


            long start1 = System.currentTimeMillis();
            Log.d(TAG, "deltaTime1111=" + (start1 - start));

            Bitmap bitmap;
            if (mIntoView == null) {
                bitmap = blur(mBitmap, mRadius);
            } else {
                mBlurView.getGlobalVisibleRect(mViewRect);
                mIntoView.getGlobalVisibleRect(mBlurRect);
                int x = Math.max(0, mBlurRect.left - mViewRect.left);
                int y = Math.max(0, mBlurRect.top - mViewRect.top);

//                Log.d(TAG, Thread.currentThread().getName() + " x=" + x + " y=" + y
//                        + " mViewRect=" + mViewRect + " mBlurRect=" + mBlurRect
//                        + " w=" + mBitmap.getWidth() + " h=" + mBitmap.getHeight());
                try {
                    Bitmap clip = Bitmap.createBitmap(mBitmap, (int) (x * mScale), (int) (y * mScale),
                            Math.max(1, (int) (mBlurRect.width() * mScale)), Math.max(1, (int) (mBlurRect.height() * mScale)));
                    bitmap = blur(clip, mRadius);
                    clip.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            long start2 = System.currentTimeMillis();
            Log.d(TAG, "deltaTime2222=" + (start2 - start1));

            mCallback.onGetBitmap(bitmap);

            long start3 = System.currentTimeMillis();
            Log.d(TAG, "deltaTime3333=" + (start3 - start2));

            Log.d(TAG, "deltaTime=" + (start3 - start));

        }
    };

    private Bitmap snapshot(Canvas c, View view, int bgColor, int fgColor, float scale) {
        float newScale = scale > 0 ? scale : 1;
        int w = (int) (view.getWidth() * newScale);
        int h = (int) (view.getHeight() * newScale);
        Bitmap output = Bitmap.createBitmap(w <= 0 ? 1 : w, h <= 0 ? 1 : h, Bitmap.Config.ARGB_4444);
        c.setBitmap(output);
//        c.save();
        c.scale(newScale, newScale);
        if (bgColor != 0) {
            c.drawColor(bgColor);
        }
        view.draw(c);
        if (fgColor != 0) {
            c.drawColor(fgColor);
        }
        return output;
    }

    Bitmap scaleBitmap(Canvas c, Bitmap bitmap, float scale) {
        final int iw = bitmap.getWidth();
        final int ih = bitmap.getHeight();
        final int w = (int) (iw * scale);
        final int h = (int) (ih * scale);

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        c.setBitmap(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        c.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, w, h),
                paint);
        return output;
    }

    private Bitmap blur(Bitmap image, float radius) {
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_4444);
        Allocation in = Allocation.createFromBitmap(mRenderScript, image);
        Allocation out = Allocation.createFromBitmap(mRenderScript, output);

        mIntrinsicBlur.setRadius(radius);
        mIntrinsicBlur.setInput(in);
        mIntrinsicBlur.forEach(out);

        out.copyTo(output);
        return output;
    }

}
