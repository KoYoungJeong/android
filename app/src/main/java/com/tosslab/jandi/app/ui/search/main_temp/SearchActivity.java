package com.tosslab.jandi.app.ui.search.main_temp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import com.eowise.recyclerview.stickyheaders.DrawOrder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterViewModel;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchStickyHeaderAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.DaggerSearchComponent;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.SearchModule;
import com.tosslab.jandi.app.ui.search.main_temp.presenter.SearchPresenter;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 20..
 */

public class SearchActivity extends BaseAppCompatActivity
        implements SearchPresenter.View, RoomHeaderViewHolder.OnCheckChangeListener {

    @Bind(R.id.lv_search_result)
    RecyclerView lvSearchResult;

    @Bind(R.id.tv_search_keyword)
    AutoCompleteTextView tvSearchKeyword;

    @Inject
    SearchPresenter searchPresenter;

    private boolean isRoomItemFold = false;
    private boolean isMessageItemFold = false;

    private boolean isShowUnjoinTopic = false;

    private SearchAdapterViewModel searchAdapterViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integrated_search);
        DaggerSearchComponent.builder()
                .searchModule(new SearchModule(this))
                .build()
                .inject(this);
        ButterKnife.bind(this);

        setAdapter();

        tvSearchKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchPresenter.sendSearchQuery(
                        tvSearchKeyword.getText().toString(), isShowUnjoinTopic);
                return true;
            }
            return false;
        });
    }

    private void setAdapter() {
        SearchAdapter adapter = new SearchAdapter();
        adapter.setHasStableIds(true);
        lvSearchResult.setAdapter(adapter);
        lvSearchResult.setLayoutManager(new LinearLayoutManager(this));
        lvSearchResult.setItemAnimator(null);

        SearchStickyHeaderAdapter searchStickyHeaderAdapter = new SearchStickyHeaderAdapter(adapter);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(adapter)
                .setRecyclerView(lvSearchResult)
                .setSticky(true)
                .setDrawOrder(DrawOrder.OverItems)
                .setStickyHeadersAdapter(searchStickyHeaderAdapter, false)
                .setOnHeaderClickListener((header, headerId) -> {
                    if (headerId == 1) {
                        isMessageItemFold = !isMessageItemFold;
                        searchStickyHeaderAdapter.setMessageItemFold(isMessageItemFold);
                        adapter.onClickHeader(headerId, isMessageItemFold);
                    } else {
                        isRoomItemFold = !isRoomItemFold;
                        searchStickyHeaderAdapter.setRoomItemFold(isRoomItemFold);
                        adapter.onClickHeader(headerId, isRoomItemFold);
                    }
                })
                .build();

        lvSearchResult.addItemDecoration(stickyHeadersItemDecoration);

        searchAdapterViewModel = adapter;
        searchPresenter.setSearchAdapterDataModel(adapter);
        searchAdapterViewModel.setOnCheckChangeListener(this);
    }

    @Override
    public void refreshAll() {
        searchAdapterViewModel.refreshAll();
    }

    @Override
    public void onCheckUnjoinTopic(boolean isChecked) {
        isShowUnjoinTopic = isChecked;
        searchPresenter.sendSearchQueryOnlyTopicRoom(
                tvSearchKeyword.getText().toString(), isShowUnjoinTopic);
    }

}