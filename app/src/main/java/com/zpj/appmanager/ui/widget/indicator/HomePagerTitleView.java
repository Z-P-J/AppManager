package com.zpj.appmanager.ui.widget.indicator;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zpj.appmanager.R;
import com.zpj.appmanager.constant.AppConfig;
import com.zpj.appmanager.utils.EventBus;
import com.zpj.bus.ZBus;
import com.zpj.utils.ScreenUtils;

import net.lucode.hackware.magicindicator.buildins.ArgbEvaluatorHolder;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

public class HomePagerTitleView extends CommonPagerTitleView {

    private static final float MAX_SCALE = 1.4f;

    private final ImageView ivTitle;

    private final int mSelectedColor;
    protected int mNormalColor;
    private boolean isSelected;

    public HomePagerTitleView(Context context) {
        super(context);
        ivTitle = new ImageView(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dp2pxInt(context, 14));
        params.gravity = Gravity.CENTER;
        setContentView(ivTitle, params);
        mSelectedColor = ContextCompat.getColor(context, R.color.colorPrimary);
        int majorTextColor = ContextCompat.getColor(context, R.color.color_text_major);
        mNormalColor = majorTextColor;
        ZBus.with(this)
                .observe(EventBus.KEY_COLOR_CHANGE_EVENT, Boolean.class)
                .doOnChange(new ZBus.SingleConsumer<Boolean>() {
                    @Override
                    public void onAccept(Boolean isDark) {
                        int color = (AppConfig.isNightMode() || isDark) ? (AppConfig.isNightMode() ? Color.LTGRAY : Color.WHITE) : majorTextColor;
                        setNormalColor(color);
                        if (!isSelected) {
                            ivTitle.setColorFilter(mNormalColor);
                        }
                    }
                })
                .subscribe();
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        super.onEnter(index, totalCount, enterPercent, leftToRight);
        int color = ArgbEvaluatorHolder.eval(enterPercent, mNormalColor, mSelectedColor);
        ivTitle.setColorFilter(color);
        setScaleX(1.0f + (MAX_SCALE - 1.0f) * enterPercent);
        setScaleY(1.0f + (MAX_SCALE - 1.0f) * enterPercent);
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        super.onLeave(index, totalCount, leavePercent, leftToRight);
        int color = ArgbEvaluatorHolder.eval(leavePercent, mSelectedColor, mNormalColor);
        ivTitle.setColorFilter(color);
        setScaleX(MAX_SCALE - (MAX_SCALE - 1.0f) * leavePercent);
        setScaleY(MAX_SCALE - (MAX_SCALE - 1.0f) * leavePercent);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        super.onSelected(index, totalCount);
        isSelected = true;
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        super.onDeselected(index, totalCount);
        isSelected = false;
    }

    public void setImageResource(@DrawableRes int id) {
        ivTitle.setImageResource(id);
    }

    public void setNormalColor(int normalColor) {
        mNormalColor = normalColor;
    }


}
