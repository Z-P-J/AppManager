package com.zpj.appmanager.ui.widget.navigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zpj.appmanager.R;
import com.zpj.utils.ScreenUtils;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class BottomBarTab extends FrameLayout {

    private FillImageView mIvIcon;
    private TextView mTvTitle;

    @DrawableRes
    private int mNormalIcon;

    @DrawableRes
    private int mSelectedIcon;

    @ColorInt
    private int mNormalTextColor;

    @ColorInt
    private int mSelectedTextColor;

    private int mTabPosition = -1;

    private TextView mTvUnreadCount;

    private Badge badge;

    private boolean isEmptyTab = true;

    public static BottomBarTab build(Context context, String title, @DrawableRes int normalIcon, @DrawableRes int selectedIcon) {
        BottomBarTab tab = new BottomBarTab(context);
        tab.setNormalIcon(normalIcon);
        tab.setSelectedIcon(selectedIcon);
        tab.setTitle(title);
        tab.isEmptyTab = false;
        return tab;
    }

    public BottomBarTab(Context context) {
        this(context, null);
    }

    public BottomBarTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.actionBarItemBackground}); // selectableItemBackgroundBorderless
        Drawable drawable = typedArray.getDrawable(0);
        setBackground(drawable);
        typedArray.recycle();

        LinearLayout lLContainer = new LinearLayout(context);
        lLContainer.setOrientation(LinearLayout.VERTICAL);
        lLContainer.setGravity(Gravity.CENTER);
        LayoutParams paramsContainer = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsContainer.gravity = Gravity.CENTER;
        lLContainer.setLayoutParams(paramsContainer);

        mIvIcon = new FillImageView(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mIvIcon.setLayoutParams(params);
//        mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.color_text_minor));
        lLContainer.addView(mIvIcon);

//        mNormalTextColor = getResources().getColor(R.color.color_text_minor);
        mNormalTextColor = Color.DKGRAY; // Color.parseColor("#535458")
        mSelectedTextColor = getResources().getColor(R.color.colorPrimary);
        mTvTitle = new TextView(context);
        mTvTitle.setTextColor(mNormalTextColor);
        mTvTitle.setTextSize(12);
        mTvTitle.getPaint().setFakeBoldText(true);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mTvTitle.setLayoutParams(params);
        lLContainer.addView(mTvTitle);

        addView(lLContainer);

        badge = new QBadgeView(context)
                .setBadgeTextSize(10, true)
                .setBadgePadding(4, true)
                .setBadgeGravity(Gravity.CENTER | Gravity.TOP)
                .setGravityOffset(12, 0, true)
                .bindTarget(lLContainer);


        int min = ScreenUtils.dp2pxInt(context, 20);
        int padding = ScreenUtils.dp2pxInt(context, 4);
        mTvUnreadCount = new TextView(context);
        mTvUnreadCount.setBackgroundResource(R.drawable.bg_msg_bubble);
        mTvUnreadCount.setMinWidth(min);
        mTvUnreadCount.setTextSize(12);
        mTvUnreadCount.setTextColor(Color.WHITE);
        mTvUnreadCount.setPadding(padding, 0, padding, 0);
        mTvUnreadCount.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams tvUnReadParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, min);
        tvUnReadParams.gravity = Gravity.CENTER;
        tvUnReadParams.leftMargin = ScreenUtils.dp2pxInt(context, 17);
        tvUnReadParams.bottomMargin = ScreenUtils.dp2pxInt(context, 14);
        mTvUnreadCount.setLayoutParams(tvUnReadParams);
        mTvUnreadCount.setVisibility(GONE);

        addView(mTvUnreadCount);
    }

    @Override
    public void setSelected(boolean selected) {
        mIvIcon.check(selected);
        mTvTitle.setTextColor(selected ? mSelectedTextColor : mNormalTextColor);
        super.setSelected(selected);
    }

    /**
     * 设置未读数量
     */
    public void setUnreadCount(int num) {
        badge.setBadgeNumber(num);
//        if (num <= 0) {
//            mTvUnreadCount.setText(String.valueOf(0));
//            mTvUnreadCount.setVisibility(GONE);
//        } else {
//            mTvUnreadCount.setVisibility(VISIBLE);
//            if (num > 99) {
//                mTvUnreadCount.setText("99+");
//            } else {
//                mTvUnreadCount.setText(String.valueOf(num));
//            }
//        }
    }

    /**
     * 获取当前未读数量
     */
    public int getUnreadCount() {
        int count = 0;
        if (TextUtils.isEmpty(mTvUnreadCount.getText())) {
            return count;
        }
        if (mTvUnreadCount.getText().toString().equals("99+")) {
            return 99;
        }
        try {
            count = Integer.parseInt(mTvUnreadCount.getText().toString());
        } catch (Exception ignored) {
        }
        return count;
    }

    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }

    public void setNormalIcon(@DrawableRes int mNormalIcon) {
        this.mNormalIcon = mNormalIcon;
        mIvIcon.setNormalImage(mNormalIcon);
    }

    public void setSelectedIcon(@DrawableRes int mSelectedIcon) {
        this.mSelectedIcon = mSelectedIcon;
        mIvIcon.setCheckedImage(mSelectedIcon);
    }

    public void setTitle(String title) {
        this.mTvTitle.setText(title);
    }

    public boolean isEmptyTab() {
        return isEmptyTab;
    }

}
