package com.zpj.appmanager.ui.widget.indicator;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.zpj.appmanager.R;
import com.zpj.appmanager.constant.AppConfig;
import com.zpj.appmanager.utils.EventBus;
import com.zpj.bus.ZBus;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

public class SkinColorChangePagerTitleView extends ColorTransitionPagerTitleView {

    private boolean isSelected;

    public SkinColorChangePagerTitleView(Context context) {
        super(context);
        int normalTextColor = ContextCompat.getColor(context, R.color.color_text_normal);
        setNormalColor(normalTextColor);
        setSelectedColor(ContextCompat.getColor(context, R.color.colorPrimary));
        ZBus.with(this)
                .observe(EventBus.KEY_COLOR_CHANGE_EVENT, Boolean.class)
                .doOnChange(new ZBus.SingleConsumer<Boolean>() {
                    @Override
                    public void onAccept(Boolean isDark) {
                        int color = (AppConfig.isNightMode() || isDark) ? (AppConfig.isNightMode() ? Color.LTGRAY : Color.WHITE) : normalTextColor;
                        setNormalColor(color);
                        if (!isSelected) {
                            setTextColor(mNormalColor);
                        }
                    }
                })
                .subscribe();
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

//    @Subscribe
//    public void onColorChangeEvent(ColorChangeEvent event) {
//        int color = getResources().getColor((AppConfig.isNightMode() || event.isDark()) ? R.color.white : R.color.color_text_major);
//        setNormalColor(color);
//        if (!isSelected) {
//            setTextColor(mNormalColor);
//        }
//    }


}
