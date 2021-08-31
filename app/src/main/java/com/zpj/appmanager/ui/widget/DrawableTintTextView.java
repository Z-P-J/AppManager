package com.zpj.appmanager.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.zpj.appmanager.R;

public class DrawableTintTextView extends AppCompatTextView {

    private int drawableTintColor;

    public DrawableTintTextView(Context context) {
        this(context, null);
    }

    public DrawableTintTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableTintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTintTextView);
        drawableTintColor = typedArray.getColor(R.styleable.DrawableTintTextView_drawable_tint_color, Color.TRANSPARENT);
        typedArray.recycle();
        tintDrawables();
    }

//    @Override
//    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
//        super.setCompoundDrawables(left, top, right, bottom);
//        tintDrawables();
//    }
//
//    @Override
//    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
//        super.setCompoundDrawablesRelative(start, top, end, bottom);
//        tintDrawables();
//    }
//
//    @Override
//    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {
//        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
//    }

    public void setDrawableStart(int drawableId) {
        setDrawableStart(drawableId, drawableTintColor);
    }

    public void setDrawableTop(int drawableId) {
        setDrawableTop(drawableId, drawableTintColor);
    }

    public void setDrawableEnd(int drawableId) {
        setDrawableEnd(drawableId, drawableTintColor);
    }

    public void setDrawableBottom(int drawableId) {
        setDrawableBottom(drawableId, drawableTintColor);
    }

    public void setDrawableStart(int drawableId, int drawableTintColor) {
        setDrawableStart(getResources().getDrawable(drawableId), drawableTintColor);
    }

    public void setDrawableTop(int drawableId, int drawableTintColor) {
        setDrawableTop(getResources().getDrawable(drawableId), drawableTintColor);
    }

    public void setDrawableEnd(int drawableId, int drawableTintColor) {
        setDrawableEnd(getResources().getDrawable(drawableId), drawableTintColor);
    }

    public void setDrawableBottom(int drawableId, int drawableTintColor) {
        setDrawableBottom(getResources().getDrawable(drawableId), drawableTintColor);
    }

    public void setDrawableStart(Drawable drawable) {
        setDrawableStart(drawable, drawableTintColor);
    }

    public void setDrawableTop(Drawable drawable) {
        setDrawableTop(drawable, drawableTintColor);
    }

    public void setDrawableEnd(Drawable drawable) {
        setDrawableEnd(drawable, drawableTintColor);
    }

    public void setDrawableBottom(Drawable drawable) {
        setDrawableBottom(drawable, drawableTintColor);
    }

    public void setDrawableStart(Drawable drawable, int drawableTintColor) {
        this.drawableTintColor = drawableTintColor;
        Drawable[] drawables = getCompoundDrawablesRelative();
        drawables[0] = drawable;
        tintDrawables(drawables);
    }

    public void setDrawableTop(Drawable drawable, int drawableTintColor) {
        this.drawableTintColor = drawableTintColor;
        Drawable[] drawables = getCompoundDrawablesRelative();
        drawables[1] = drawable;
        tintDrawables(drawables);
    }

    public void setDrawableEnd(Drawable drawable, int drawableTintColor) {
        this.drawableTintColor = drawableTintColor;
        Drawable[] drawables = getCompoundDrawablesRelative();
        drawables[2] = drawable;
        tintDrawables(drawables);
    }

    public void setDrawableBottom(Drawable drawable, int drawableTintColor) {
        this.drawableTintColor = drawableTintColor;
        Drawable[] drawables = getCompoundDrawablesRelative();
        drawables[3] = drawable;
        tintDrawables(drawables);
    }

    public void setDrawableTintColor(int drawableTintColor) {
        this.drawableTintColor = drawableTintColor;
        tintDrawables();
    }

    private void tintDrawables() {
        tintDrawables(getCompoundDrawablesRelative());
    }

    private void tintDrawables(Drawable[] drawables) {
        if (drawableTintColor != Color.TRANSPARENT) {
            for (int i = 0; i < drawables.length; i++) {
                Drawable drawable = drawables[i];
                if (drawable != null) {
                    final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
                    DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(drawableTintColor));
                    drawables[i] = wrappedDrawable;
                }
            }
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        }
    }

}
