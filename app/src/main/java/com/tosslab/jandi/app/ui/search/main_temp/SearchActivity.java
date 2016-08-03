package com.tosslab.jandi.app.ui.search.main_temp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.eowise.recyclerview.stickyheaders.DrawOrder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.view.TopicInfoDialog;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterViewModel;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchStickyHeaderAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.DaggerSearchComponent;
import com.tosslab.jandi.app.ui.search.main_temp.dagger.SearchModule;
import com.tosslab.jandi.app.ui.search.main_temp.presenter.SearchPresenter;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 7. 20..
 */

public class SearchActivity extends BaseAppCompatActivity
        implements SearchPresenter.View, SearchAdapter.OnRequestMoreMessage {

    private static final int SPEECH_REQUEST_CODE = 0x01;

    @Bind(R.id.lv_search_result)
    RecyclerView lvSearchResult;

    @Bind(R.id.tv_search_keyword)
    AutoCompleteTextView tvSearchKeyword;

    @Bind(R.id.progress_more_loading_message)
    ProgressBar progressMoreLoadingMessage;

    @Inject
    SearchPresenter searchPresenter;

    private boolean isRoomItemFold = false;
    private boolean isMessageItemFold = false;

    private SearchAdapterViewModel searchAdapterViewModel;

    private boolean flagFirstSearch = true;
    private SearchQueryAdapter searchQueryAdapter;

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

        searchPresenter.sendSearchHistory();

        tvSearchKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (flagFirstSearch) {
                    setStickyHeaderAdapter();
                    removeHistoryListeners();
                    flagFirstSearch = false;
                }
                searchPresenter.sendSearchQuery(
                        tvSearchKeyword.getText().toString());
                tvSearchKeyword.dismissDropDown();
                return true;
            }
            return false;
        });

        initDropdownOldQuery();

        setHistoryListeners();
    }

    private void setHistoryListeners() {
        searchAdapterViewModel.setOnDeleteAllHistory(() -> {
            searchPresenter.onDeleteaAllHistoryItem();
        });

        searchAdapterViewModel.setOnSelectHistoryListener(keyword -> {
            tvSearchKeyword.setText(keyword);
            tvSearchKeyword.setSelection(keyword.length());
            tvSearchKeyword.dismissDropDown();
        });

        searchAdapterViewModel.setOnDeleteHistoryListener(keyword -> {
            searchPresenter.onDeleteaHistoryItemByKeyword(keyword);
        });
    }

    private void removeHistoryListeners() {
        searchAdapterViewModel.setOnDeleteAllHistory(null);

        searchAdapterViewModel.setOnSelectHistoryListener(null);

        searchAdapterViewModel.setOnDeleteHistoryListener(null);
    }


    private void initDropdownOldQuery() {
        searchQueryAdapter = new SearchQueryAdapter(SearchActivity.this);
        tvSearchKeyword.setAdapter(searchQueryAdapter);

        tvSearchKeyword.setOnItemClickListener((parent, view, position, id) -> {
            String searchedQuery = searchQueryAdapter.getItem(position).toString();
            tvSearchKeyword.setText(searchedQuery);
            tvSearchKeyword.setSelection(searchedQuery.length());
        });

        tvSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQueryAdapter.clear();
                searchQueryAdapter.addAll(searchPresenter.getOldQueryList(s.toString()));
                searchQueryAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setAdapter() {
        SearchAdapter adapter = new SearchAdapter();
        adapter.setHasStableIds(true);
        lvSearchResult.setAdapter(adapter);
        lvSearchResult.setLayoutManager(new LinearLayoutManager(this));
        lvSearchResult.setItemAnimator(null);

        searchAdapterViewModel = adapter;
        searchPresenter.setSearchAdapterDataModel(adapter);
        searchAdapterViewModel.setOnCheckChangeListener(isChecked -> onCheckUnjoinTopic(isChecked));

        searchAdapterViewModel.setOnRequestMoreMessage(this);

        searchAdapterViewModel.setOnClickTopicListener((topicId, isJoined) -> {
            searchPresenter.onLaunchTopicRoom(topicId, isJoined);
        });

        searchAdapterViewModel.setOnClickMessageListener(searchMessageData -> {
            if (searchMessageData.getFeedbackType() != null) {
                if (searchMessageData.getFeedbackType().equals("poll")) {
                    moveToPollActivity(searchMessageData.getPoll().getId());
                } else if (searchMessageData.getFeedbackType().equals("file")) {
                    moveToFileActivity(searchMessageData.getMessageId(), searchMessageData.getFile().getId());
                }
            } else {
                moveToMessageActivityFromSearch(searchMessageData.getRoomId(),
                        searchMessageData.getType(),
                        searchMessageData.getLinkId());
            }
        });
    }

    private void setStickyHeaderAdapter() {
        SearchAdapter adapter = (SearchAdapter) searchAdapterViewModel;
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
                    } else if (headerId == 2) {
                        isRoomItemFold = !isRoomItemFold;
                        searchStickyHeaderAdapter.setRoomItemFold(isRoomItemFold);
                        adapter.onClickHeader(headerId, isRoomItemFold);
                    }
                })
                .build();

        lvSearchResult.addItemDecoration(stickyHeadersItemDecoration);
    }

    @Override
    public void refreshSearchedAll() {
        searchAdapterViewModel.refreshSearchedAll();
    }

    @Override
    public void refreshHistory() {
        searchAdapterViewModel.refreshHistory();
    }

    public void onCheckUnjoinTopic(boolean isChecked) {
        searchPresenter.setChangeIsShowUnjoinedTopic(isChecked);
    }

    @Override
    public void showMoreProgressBar() {
        progressMoreLoadingMessage.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        progressMoreLoadingMessage.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void dismissMoreProgressBar() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_bottom);

        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                progressMoreLoadingMessage.setVisibility(View.GONE);
            }
        });

        progressMoreLoadingMessage.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void onRequestMoreMessage() {
        searchPresenter.sendMoreResults();
    }

    @Override
    public void moveToMessageActivity(long entityId, int entityType) {
        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(entityId)
                .entityType(entityType)
                .start();
    }

    @Override
    public void moveToPollActivity(long pollId) {
        PollDetailActivity.start(this, pollId);
    }

    @Override
    public void moveToFileActivity(long messageId, long fileId) {
        FileDetailActivity_.intent(this)
                .selectMessageId(messageId)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
    }

    @Override
    public void moveToMessageActivityFromSearch(long entityId, int entityType, long linkId) {
        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .entityId(entityId)
                .entityType(entityType)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .start();
    }

    @Override
    public void showTopicInfoDialog(TopicRoom topicRoom) {
        TopicInfoDialog dialog = TopicInfoDialog.instantiate(topicRoom);
        int type = topicRoom.isPublicTopic() ?
                JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
        dialog.show(getSupportFragmentManager(), "dialog");
        dialog.setOnJoinClickListener((topicEntityId) ->
                searchPresenter.onJoinTopic(topicEntityId, type));
    }

    @OnClick(R.id.iv_search_mic)
    void onVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start the activity, the intent will be populated with the speech text
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.iv_search_backkey)
    void onPressBackKey() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                return;
            }

            List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (voiceSearchResults != null && !voiceSearchResults.isEmpty()) {
                String searchText = voiceSearchResults.get(0);
                tvSearchKeyword.setText(searchText);
                tvSearchKeyword.setSelection(searchText.length());
                searchPresenter.sendSearchQuery(
                        tvSearchKeyword.getText().toString());
                tvSearchKeyword.dismissDropDown();

            }
        }
    }

}