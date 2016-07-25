package com.tosslab.jandi.app.ui.search.main_temp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.eowise.recyclerview.stickyheaders.DrawOrder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchStickyHeaderAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.DaggerSearchComponent;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.SearchModule;
import com.tosslab.jandi.app.ui.search.main_temp.presenter.SearchPresenter;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 20..
 */

public class SearchActivity extends BaseAppCompatActivity implements SearchPresenter.View {

    @Bind(R.id.lv_search_result)
    RecyclerView lvSearchResult;

    @Inject
    SearchPresenter searchPresenter;

    private SearchAdapter adapter;

    private boolean isRoomItemFold = false;
    private boolean isMessageItemFold = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integrated_search);
        ButterKnife.bind(this);
        setAdapter();
        DaggerSearchComponent.builder()
                .searchModule(new SearchModule(this))
                .build()
                .inject(this);

//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_HEADER));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_ROOM_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_HEADER));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
//        adapter.addRow(MultiItemRecyclerAdapter.Row.create(null, SearchAdapter.ITEM_TYPE_MESSAGE_ITEM));
    }

    private void setAdapter() {
        adapter = new SearchAdapter();
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
                        adapter.onClickHeader(headerId, isMessageItemFold);
                    } else {
                        isRoomItemFold = !isRoomItemFold;
                        adapter.onClickHeader(headerId, isRoomItemFold);
                    }

                })
                .build();
        lvSearchResult.addItemDecoration(stickyHeadersItemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}