package com.zpj.appmanager.ui.multidata;

import android.view.View;

import com.zpj.appmanager.ui.widget.TitleHeaderLayout;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.HeaderMultiData;
import com.zpj.appmanager.R;

import java.util.List;

public abstract class BaseHeaderMultiData<T> extends HeaderMultiData<T>
        implements View.OnClickListener {

    protected final String title;

    public BaseHeaderMultiData(String title) {
        super();
        this.title = title;
    }

    public BaseHeaderMultiData(String title, List<T> list) {
        super(list);
        this.title = title;
    }

    @Override
    public int getChildCount() {
        return super.getChildCount();
    }

    @Override
    public int getHeaderLayoutId() {
        return R.layout.item_header_title;
    }

    @Override
    public void onBindHeader(EasyViewHolder holder, List<Object> payloads) {
        TitleHeaderLayout headerLayout = holder.getView(R.id.layout_title_header);
        headerLayout.setTitle(title);
        headerLayout.setOnMoreClickListener(showMoreButton() ? this : null);
    }

    @Override
    public final void onClick(View v) {
        onHeaderClick();
    }

    protected boolean showMoreButton() {
        return true;
    }

    public void onHeaderClick() {

    }

}
