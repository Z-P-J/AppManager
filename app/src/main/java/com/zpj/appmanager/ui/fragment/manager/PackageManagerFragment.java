package com.zpj.appmanager.ui.fragment.manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zpj.appmanager.R;
import com.zpj.appmanager.ui.fragment.base.RecyclerLayoutFragment;
import com.zpj.appmanager.ui.fragment.dialog.PackageDetailDialogFragment;
import com.zpj.appmanager.ui.fragment.dialog.RecyclerPartShadowDialogFragment;
import com.zpj.appmanager.ui.widget.ExpandIcon;
import com.zpj.appmanager.ui.widget.LetterSortSideBar;
import com.zpj.appmanager.ui.widget.RoundedDrawableTextView;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.progressbar.ZProgressBar;
import com.zpj.recyclerview.EasyAdapter;
import com.zpj.recyclerview.EasyRecyclerLayout;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.appmanager.constant.Keys;
import com.zpj.appmanager.model.InstalledAppInfo;
import com.zpj.appmanager.utils.FileScanner;
import com.zpj.appmanager.utils.PackageStateComparator;
import com.zpj.appmanager.utils.PinyinComparator;
import com.zpj.skin.SkinEngine;
import com.zpj.toast.ZToast;
import com.zpj.utils.AppUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageManagerFragment extends RecyclerLayoutFragment<InstalledAppInfo> {

    private static final String TAG = "PackageFragment";

    protected final List<InstalledAppInfo> tempData = new ArrayList<>();

    private ZProgressBar progressBar;
    private TextView tvFilter;
    private TextView tvInfo;

    private RelativeLayout headerLayout;

    private LetterSortSideBar sortSideBar;

    private int sortPosition = 0;
    private int filterPosition = 0;

    private int lastProgress = 0;

    public static PackageManagerFragment newInstance(boolean showToolbar) {
        Bundle args = new Bundle();
        args.putBoolean(Keys.SHOW_TOOLBAR, showToolbar);
        PackageManagerFragment fragment = new PackageManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void start(boolean showToolbar) {
        start(PackageManagerFragment.newInstance(showToolbar));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_package_manager;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_app_installed;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);
        boolean showToolbar = getArguments() != null && getArguments().getBoolean(Keys.SHOW_TOOLBAR, false);
        if (showToolbar) {
            toolbar.setVisibility(View.VISIBLE);
            setToolbarTitle("???????????????");
        } else {
            setEnableSwipeBack(false);
        }
        tvFilter = findViewById(R.id.tv_filter);
        ExpandIcon expandIconView = findViewById(R.id.expand_icon);
        View.OnClickListener listener = v -> showFilterDialog(expandIconView);
        expandIconView.setOnClickListener(listener);
        tvFilter.setOnClickListener(listener);
        tvInfo = findViewById(R.id.tv_info);
        progressBar = findViewById(R.id.progress_bar);

        ImageView ivSort = findViewById(R.id.iv_sort);
        ivSort.setOnClickListener(view1 -> showSortDialog(ivSort));

        headerLayout = findViewById(R.id.layout_header);

        TextView tvHint = findViewById(R.id.tv_hint);
        sortSideBar = findViewById(R.id.sortView);
        sortSideBar.setVisibility(View.GONE);
        sortSideBar.setIndexChangedListener(new LetterSortSideBar.OnIndexChangedListener() {
            @Override
            public void onSideBarScrollUpdateItem(String word) {
                tvHint.setVisibility(View.VISIBLE);
                tvHint.setText(word);

                int firstItemPosition = 0;
                int lastItemPosition = 0;
                RecyclerView.LayoutManager layoutManager = recyclerLayout.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    //?????????????????????view?????????
                    firstItemPosition = linearManager.findFirstVisibleItemPosition();
                    lastItemPosition = linearManager.findLastVisibleItemPosition();
                }
                int delta = lastItemPosition - firstItemPosition;

                int index = -1;
                for (InstalledAppInfo info : data) {
                    if (info.getLetter().equals(word)) {
                        index = data.indexOf(info);
                        break;
                    }
                }
                if (index != -1) {
                    int pos = index + delta / 2;
                    if (pos > data.size()) {
                        pos = data.size() - 1;
                    }
                    recyclerLayout.getRecyclerView().scrollToPosition(pos);
                }
            }

            @Override
            public void onSideBarScrollEndHideText() {
                tvHint.setVisibility(View.GONE);
            }
        });

        recyclerLayout.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int mScrollState = -1;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mScrollState = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mScrollState != -1) {
                    //????????????????????????
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    //???????????????layoutManager?????????LinearLayoutManager
                    // ??????LinearLayoutManager??????????????????????????????????????????view???????????????
                    int firstItemPosition = 0;
                    int lastItemPosition = 0;
                    int position;
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                        //?????????????????????view?????????
                        firstItemPosition = linearManager.findFirstVisibleItemPosition();
                        lastItemPosition = linearManager.findLastVisibleItemPosition();
                    }
                    if (lastItemPosition >= data.size() - 1) {
                        position = data.size() - 1;
                    } else {
                        position = firstItemPosition;
                    }

//                    sideBarLayout.OnItemScrollUpdateText(data.get(firstItemPosition).getLetter());
                    sortSideBar.onItemScrollUpdateText(data.get(position).getLetter());
                    if (mScrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        mScrollState = -1;
                    }
                }
            }
        });

    }

    @Override
    protected void buildRecyclerLayout(EasyRecyclerLayout<InstalledAppInfo> recyclerLayout) {
        super.buildRecyclerLayout(recyclerLayout);
        recyclerLayout.setEnableSwipeRefresh(false).setEnableSelection(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onBackPressedSupport() {
        if (recyclerLayout.isSelectMode()) {
            recyclerLayout.exitSelectMode();
            return true;
        }
        return super.onBackPressedSupport();
    }

    @Override
    public void onClick(EasyViewHolder holder, View view, InstalledAppInfo data) {
//        ZToast.normal("todo ????????????");
        AppUtils.installApk(context, data.getApkFilePath());
    }

    @Override
    public boolean onLongClick(EasyViewHolder holder, View view, InstalledAppInfo data) {
        return false;
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<InstalledAppInfo> list, int position, List<Object> payloads) {
        InstalledAppInfo appInfo = list.get(position);

        holder.setText(R.id.tv_name, appInfo.getName());
        holder.getView(R.id.layout_right).setOnClickListener(v -> {
            onMenuClicked(v, appInfo);
        });
        String versionName = appInfo.getVersionName();
        holder.setText(R.id.tv_size, appInfo.getFormattedAppSize());
        holder.setText(R.id.tv_version, versionName);
        if (appInfo.isDamaged()) {
            holder.setVisible(R.id.tv_version, !TextUtils.isEmpty(versionName));
            RoundedDrawableTextView tvState = holder.getView(R.id.tv_state);
            tvState.setText("?????????");
            tvState.setTintColor(context.getResources().getColor(R.color.red));
//            holder.setText(R.id.tv_info, appInfo.getFormattedAppSize() + " | ?????????");
            holder.getImageView(R.id.iv_icon).setImageResource(R.drawable.ic_file_apk);
        } else {
            holder.setVisible(R.id.tv_version, true);
            Log.d("onBindViewHolder", "name=" + appInfo.getName());
            Log.d("onBindViewHolder", "size=" + appInfo.getFileLength());

            Glide.with(context).load(appInfo).into(holder.getImageView(R.id.iv_icon));

            String info = "?????????";

            RoundedDrawableTextView tvState = holder.getView(R.id.tv_state);
            tvState.setText(info);
            tvState.setTintColor(context.getResources().getColor(R.color.pink_fc4f74));
        }
    }

    @Override
    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
        if (data.isEmpty()) {
            postOnEnterAnimationEnd(new Runnable() {
                @Override
                public void run() {
                    loadApk();
                }
            });
            return true;
        }
        return false;
    }


    private void loadApk() {
        new FileScanner<InstalledAppInfo>()
                .setType(".apk")
                .bindLife(this)
                .start(new FileScanner.OnScanListener<InstalledAppInfo>() {
                    @Override
                    public void onScanBegin() {
                        Log.d(TAG, "onScanBegin");
                        post(() -> {
                            tvInfo.setText("?????????...");
                            progressBar.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onScanEnd() {
                        Log.d(TAG, "onScanEnd");
                        post(() -> {
                            tvInfo.setText("???" + data.size() + "????????????");
                            progressBar.setVisibility(View.GONE);
                            tempData.clear();
                            tempData.addAll(data);
                            sort();
                        });
                    }

                    @Override
                    public void onScanning(String paramString, int progress) {
                        if (progress != lastProgress) {
                            lastProgress = progress;
                            post(() -> {
                                tvInfo.setText("?????????" + data.size() + "????????????????????????" + progress + "%");
                                progressBar.setVisibility(View.VISIBLE);
                            });
                        }
                    }

                    @Override
                    public InstalledAppInfo onWrapFile(File file) {
                        Log.d(TAG, "onWrapFile file=" + file.getAbsolutePath());
                        return InstalledAppInfo.parseFromApk(context, file);
                    }

                    @Override
                    public void onScanningFiles(InstalledAppInfo item) {
                        Log.d(TAG, "onScanningFiles");
                        if (item != null) {
                            synchronized (data) {
                                data.add(item);
                                if (data.size() < 20) {
                                    recyclerLayout.notifyDataSetChanged();
                                } else {
                                    recyclerLayout.notifyItemInserted(data.size() - 1);
                                }
                                recyclerLayout.notifyItemRangeChanged(data.size() - 1, 1);
                            }
                        }
                    }
                });
    }

    private void sort() {
        sortSideBar.setVisibility(sortPosition == 0 ? View.VISIBLE : View.GONE);
        switch (sortPosition) {
            case 0:
                Collections.sort(data, new PinyinComparator());
                break;
            case 1:
                Collections.sort(data, (o1, o2) -> Long.compare(o1.getAppSize(), o2.getAppSize()));
                break;
            case 2:
                Collections.sort(data, (o1, o2) -> Long.compare(o2.getLastUpdateTime(), o1.getLastUpdateTime()));
                break;
            case 3:
                Collections.sort(data, new PackageStateComparator());
                break;
            default:
                break;
        }
        recyclerLayout.notifyDataSetChanged();
    }

    private void showFilterDialog(ExpandIcon expandIconView) {
        expandIconView.switchState();
        new RecyclerPartShadowDialogFragment()
                .addItems("???????????????", "?????????")
                .setSelectedItem(filterPosition)
                .setOnItemClickListener((view, title, position) -> {
                    filterPosition = position;
                    tvFilter.setText(title);
                    data.clear();
                    switch (position) {
                        case 0:
                            data.addAll(tempData);
                            break;
                        case 1:
                            for (InstalledAppInfo appInfo : tempData) {
                                if (appInfo.isDamaged()) {
                                    data.add(appInfo);
                                }
                            }
                            break;
                    }
                    sort();
                })
                .setAttachView(headerLayout)
                .setOnDismissListener(expandIconView::switchState)
                .show(context);
    }

    private void showSortDialog(ImageView ivSort) {
        ivSort.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
        new RecyclerPartShadowDialogFragment()
                .addItems("???????????????", "???????????????", "???????????????", "???????????????")
                .setSelectedItem(sortPosition)
                .setOnItemClickListener((view, title, position) -> {
                    sortPosition = position;
                    sort();
                })
                .setAttachView(headerLayout)
                .setOnDismissListener(() -> SkinEngine.setTint(ivSort, R.attr.textColorMajor))
                .show(context);
    }

    public void onMenuClicked(View view, InstalledAppInfo updateInfo) {
        ZDialog.arrowMenu()
                .setOptionMenus(R.array.apk_actions)
                .setOrientation(LinearLayout.HORIZONTAL)
                .setOnItemClickListener((position, menu) -> {
                    switch (position) {
                        case 0:
                            PackageDetailDialogFragment.with(updateInfo).show(context);
                            break;
                        case 1:
                            ZToast.normal(updateInfo.getApkFilePath());
                            AppUtils.shareApk(context, updateInfo.getApkFilePath());
                            break;
                        case 2:
                            File file = new File(updateInfo.getApkFilePath());
                            if (file.exists() && file.delete()) {
                                ZToast.success("???????????????");
                            } else {
                                ZToast.warning("???????????????");
                            }
                            break;
                        case 3:
                            AppUtils.installApk(context, updateInfo.getApkFilePath());
                            break;
                        default:
                            ZToast.warning("???????????????");
                            break;
                    }
                })
                .setAttachView(view)
                .show(context);
    }
}
