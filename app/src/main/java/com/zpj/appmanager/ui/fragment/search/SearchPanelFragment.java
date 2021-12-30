package com.zpj.appmanager.ui.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zpj.appmanager.R;
import com.zpj.appmanager.ui.fragment.base.SkinFragment;
import com.zpj.appmanager.ui.widget.flowlayout.FlowLayout;
import com.zpj.bus.ZBus;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.recyclerview.EasyRecyclerView;
import com.zpj.appmanager.database.SearchHistoryManager;
import com.zpj.appmanager.model.GuessAppInfo;
import com.zpj.appmanager.model.QuickAppInfo;
import com.zpj.appmanager.model.SearchHistory;
import com.zpj.appmanager.utils.EventBus;
import com.zpj.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchPanelFragment extends SkinFragment {

    private final List<GuessAppInfo> appInfoList = new ArrayList<>();
    private final List<QuickAppInfo> quickAppInfoList = new ArrayList<>();

    private LinearLayout llRecommend;
    private LinearLayout llQuick;

    private EasyRecyclerView<GuessAppInfo> rvGuess;
    private EasyRecyclerView<QuickAppInfo> rvQuick;

    private FlowLayout hotSearch;
    private RelativeLayout rlHistoryBar;
    private FlowLayout searchHistory;
    private FlowLayout.OnItemClickListener onItemClickListener;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_panel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.onSearchEvent(this, new ZBus.SingleConsumer<String>() {
            @Override
            public void onAccept(String keyword) {
                SearchHistory history = SearchHistoryManager.getSearchHistoryByText(keyword);
                if (history == null) {
                    history = new SearchHistory();
                    history.setText(keyword);
                }
                history.setTime(System.currentTimeMillis());
                history.save();
                searchHistory.remove(history.getText());
                searchHistory.addItem(0, history.getText());
                searchHistory.setVisibility(searchHistory.count() == 0 ? View.GONE : View.VISIBLE);
                rlHistoryBar.setVisibility(searchHistory.count() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        int dp8 = ScreenUtils.dp2pxInt(context, 8);

        llRecommend = findViewById(R.id.ll_recommend);
        llQuick = findViewById(R.id.ll_quick);

        hotSearch = view.findViewById(R.id.hot_search);
        hotSearch.setSpace(dp8);
        hotSearch.setOnItemClickListener(onItemClickListener);
        rlHistoryBar = findViewById(R.id.rl_history_bar);
        searchHistory = view.findViewById(R.id.search_history);
        searchHistory.setSpace(dp8);
        searchHistory.setOnItemClickListener(onItemClickListener);
        TextView tvClearHistory = findViewById(R.id.tv_clear_history);
        tvClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZDialog.alert()
                        .setTitle("清空记录？")
                        .setContent("您将清空您的历史搜索记录，确认清空？")
                        .setPositiveButton((fragment, which) -> {
                            SearchHistoryManager.deleteAllLocalSearchHistory();
                            searchHistory.clear();
                            searchHistory.setVisibility(View.GONE);
                            rlHistoryBar.setVisibility(searchHistory.count() == 0 ? View.GONE : View.VISIBLE);
                        })
                        .show(context);
            }
        });

        rvGuess = new EasyRecyclerView<>(findViewById(R.id.rv_guess));

        rvQuick = new EasyRecyclerView<>(findViewById(R.id.rv_quick));
        rvQuick.setData(quickAppInfoList)
                .setItemRes(R.layout.item_app_linear)
                .onBindViewHolder((holder, list, position, payloads) -> {
                    QuickAppInfo info = list.get(position);
//                    holder.setVisible(R.id.iv_icon, false);
                    ImageView ivIcon = holder.getView(R.id.iv_icon);
                    ViewGroup.LayoutParams params = ivIcon.getLayoutParams();
                    int size = ScreenUtils.dp2pxInt(context, 36);
                    params.height = size;
                    params.width = size;
                    ivIcon.setImageResource(R.drawable.ic_apk);
                    holder.setVisible(R.id.tv_info, false);
                    ivIcon.setColorFilter(getResources().getColor(R.color.colorPrimary));

                    TextView tvTitle = holder.getView(R.id.tv_title);
                    tvTitle.setMaxLines(1);
                    tvTitle.setTextSize(14);
                    tvTitle.setText(info.getAppTitle());
                    holder.setText(R.id.tv_desc, info.getAppPackage());

                })
                .build();

    }

    public void init() {
        getGuessApp();
        getHotSearch();
        getSearchHistory();
    }

    public void setOnItemClickListener(FlowLayout.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void getHotSearch() {

    }

    private void getGuessApp() {

    }

    private void getSearchHistory() {
        postOnEnterAnimationEnd(() -> {
            List<String> list = new ArrayList<>();
            for (SearchHistory history : SearchHistoryManager.getAllSearchHistory()) {
                list.add(history.getText());
            }
            searchHistory.setItems(list);
            searchHistory.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            rlHistoryBar.setVisibility(searchHistory.count() == 0 ? View.GONE : View.VISIBLE);
        });
    }

}
