package com.zpj.appmanager.ui.animator;

import android.animation.TypeEvaluator;

public class KickBackEvaluator implements TypeEvaluator<Float> {

    private static final float s = 1.70158f;
    private float mDuration = 0f;

    public void setDuration(float duration) {
        mDuration = duration;
    }

    public Float evaluate(float fraction, Float startValue, Float endValue) {
        float t = mDuration * fraction;
        float b = startValue;
        float c = endValue - startValue;
        float d = mDuration;
        return calculate(t, b, c, d);
    }

    private Float calculate(float t, float b, float c, float d) {
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }
}
