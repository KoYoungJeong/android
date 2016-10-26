package com.tosslab.jandi.app.ui.search.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.TopicInfoDialog;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.search.file.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.filter.member.MemberFilterActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapterViewModel;
import com.tosslab.jandi.app.ui.search.main.dagger.DaggerSearchComponent;
import com.tosslab.jandi.app.ui.search.main.dagger.SearchModule;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.PricingPlanWarningViewController;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import rx.Observable;

/**
 * Created by tee on 16. 7. 20..
 */

public class SearchActivity extends BaseAppCompatActivity
        implements SearchPresenter.View {

    private static final int REQUEST_CODE_SPEECH = 0x01;
    private static final int REQUEST_CODE_ROOM_SELECTION = 0x02;
    private static final int REQUEST_CODE_MEMBER_SELECTION = 0x03;

    @InjectExtra
    @Nullable
    long selectedRoomId = -1l;
    @InjectExtra
    @Nullable
    long selectedOneToOneRoomMemberId = -1l;

    @Bind(R.id.lv_search_result)
    RecyclerView lvSearchResult;
    @Bind(R.id.tv_search_keyword)
    AutoCompleteTextView tvSearchKeyword;
    @Bind(R.id.progress_more_loading_message)
    ProgressBar progressMoreLoadingMessage;
    @Bind(R.id.layout_pricing_plan_warning)
    ViewGroup layoutPricingPlanWarning;

    @Inject
    SearchPresenter searchPresenter;
    @Inject
    SearchAdapterViewModel searchAdapterViewModel;

    private boolean isRoomItemFold = false;
    private boolean flagFirstSearch = true;
    private SearchQueryAdapter searchQueryAdapter;
    private AlertDialog deleteConfirmDialog;
    private AlertDialog chooseRoomDialog;

    private boolean isSelectDirectMessageRoom = false;
    private long selectedMemberId = -1l;

    private android.view.inputmethod.InputMethodManager inputMethodManager;

    private boolean isOnlyMessageMode = false;

    private AnalyticsValue.Screen screenMode = AnalyticsValue.Screen.UniversalSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_integrated_search);

        ButterKnife.bind(this);

        SearchAdapter adapter = new SearchAdapter();

        setListView(adapter);

        DaggerSearchComponent.builder()
                .searchModule(new SearchModule(this, adapter))
                .build()
                .inject(this);

        Dart.inject(this);

        initAdapterViewModel();

        searchPresenter.sendSearchHistory();

        initDropdownOldQuery();

        setHistoryListeners();

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (selectedRoomId != -1l) {
            isOnlyMessageMode = true;
            searchPresenter.onSetOnlyMessageMode(isOnlyMessageMode);
            searchPresenter.onRoomChanged(selectedRoomId, -1);
            tvSearchKeyword.setHint(
                    JandiApplication.getContext().getString(R.string.jandi_message_search));
        } else {
            searchPresenter.onAccessTypeChanged("joined");
        }

        if (isOnlyMessageMode) {
            screenMode = AnalyticsValue.Screen.MsgSearch;
        } else {
            screenMode = AnalyticsValue.Screen.UniversalSearch;
        }

        AnalyticsUtil.sendScreenName(screenMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!flagFirstSearch) {
            Observable.just(1)
                    .delay(100, TimeUnit.MILLISECONDS)
                    .subscribe(i -> {
                        hideKeyboard();
                    });

        }
    }

    private void setHistoryListeners() {
        searchAdapterViewModel.setOnDeleteAllHistory(() -> showDeleteConfirmDialog());

        searchAdapterViewModel.setOnSelectHistoryListener(keyword -> {
            tvSearchKeyword.setText(keyword);
            tvSearchKeyword.setSelection(keyword.length());
            onSearch();
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.TapRecentKeywords);
        });

        searchAdapterViewModel.setOnDeleteHistoryListener(keyword -> {
            searchPresenter.onDeleteaHistoryItemByKeyword(keyword);
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.DeleteRecentKeyword);
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
    }

    @OnTextChanged(R.id.tv_search_keyword)
    void searchKeywordChanged(CharSequence text) {
        searchPresenter.onSearchKeywordChanged(text.toString());
    }

    @OnEditorAction(R.id.tv_search_keyword)
    boolean onSearchAction(TextView view, int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchQueryAdapter.clear();
            searchQueryAdapter.notifyDataSetChanged();
            tvSearchKeyword.dismissDropDown();
            onSearch();
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.GoSearchResult);
            return true;
        }
        return false;
    }

    private void onSearch() {
        String searchKeyword = tvSearchKeyword.getText().toString().trim();
        if (searchKeyword.length() >= 2) {
            if (flagFirstSearch) {
                setStickyHeaderAdapter();
                removeHistoryListeners();
                searchPresenter.onInitPricingInfo();
                flagFirstSearch = false;
            }
            searchPresenter.sendSearchQuery(searchKeyword, isOnlyMessageMode);
            tvSearchKeyword.dismissDropDown();
            hideKeyboard();
        } else {
            ColoredToast.show(R.string.jandi_search_available_length_of_keyword);
        }
    }

    private void setListView(SearchAdapter adapter) {
        adapter.setHasStableIds(true);
        lvSearchResult.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lvSearchResult.setLayoutManager(layoutManager);
        lvSearchResult.setItemAnimator(null);
    }

    private void initAdapterViewModel() {
        searchAdapterViewModel.setOnCheckChangeListener(isChecked -> {
            onCheckUnjoinTopic(isChecked);
            if (isChecked) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.IncludeNotJoinedTopics, AnalyticsValue.Label.On);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.IncludeNotJoinedTopics, AnalyticsValue.Label.Off);
            }
        });

        searchAdapterViewModel.setOnClickTopicListener((topicId, isJoined) -> {
            searchPresenter.onLaunchTopicRoom(topicId, isJoined);
            if (isJoined) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.ChooseJoinedTopic);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.ChooseUnJoinedTopic);
            }
        });

        searchAdapterViewModel.setOnClickMessageListener(searchMessageData -> {
            if (searchMessageData.getFeedbackType() != null
                    && searchMessageData.getFeedbackType().equals("poll")) {
                moveToPollActivity(searchMessageData.getPoll().getId());
            } else {
                searchPresenter.onMoveToMessageFromSearch(searchMessageData);
            }
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.TapMsgSearchResult);
        });

        searchAdapterViewModel.setOnClickMemberSelectionButtonListener(() -> {
            MemberFilterActivity.startForResult(this, -1, REQUEST_CODE_MEMBER_SELECTION);
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.ChooseMemberFilter);
        });

        searchAdapterViewModel.setOnClickRoomSelectionButtonListener(() -> showChooseRoomDialog());
        LinearLayoutManager layoutManager = (LinearLayoutManager) lvSearchResult.getLayoutManager();

        searchAdapterViewModel.setOnClickOneToOneRoomListener(memberId -> {
            moveDirectMessage(memberId);
            AnalyticsUtil.sendEvent(screenMode,
                    AnalyticsValue.Action.ChooseDm);
        });

        // SCROLL
        lvSearchResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        searchPresenter.sendMoreResults();
                    }
                }
            }
        });
    }

    private void setStickyHeaderAdapter() {
        SearchAdapter adapter = (SearchAdapter) searchAdapterViewModel;
        StickyRecyclerHeadersDecoration decoration = new StickyRecyclerHeadersDecoration(adapter);
        StickyRecyclerHeadersTouchListener touchListener =
                new StickyRecyclerHeadersTouchListener(lvSearchResult, decoration);
        touchListener.setOnHeaderClickListener((header, position, headerId) -> {
            if (headerId == 2) {
                isRoomItemFold = !isRoomItemFold;
                adapter.onClickHeader(headerId, isRoomItemFold);
            }
            if (isRoomItemFold) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.CollapseRoomList, AnalyticsValue.Label.On);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.CollapseRoomList, AnalyticsValue.Label.Off);
            }
        });
        lvSearchResult.addOnItemTouchListener(touchListener);
        lvSearchResult.addItemDecoration(decoration);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                decoration.invalidateHeaders();
            }
        });
    }

    @Override
    public void refreshSearchedAll() {
        searchAdapterViewModel.refreshSearchedAll();
    }

    @Override
    public void refreshSearchedOnlyMessage() {
        searchAdapterViewModel.refreshSearchOnlyMessage();
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
        dialog.setOnJoinClickListener((topicEntityId) -> {
            searchPresenter.onJoinTopic(topicEntityId, type, -1);
        });
    }


    @OnClick(R.id.iv_search_mic)
    void onVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
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
        if (requestCode == REQUEST_CODE_SPEECH) {
            if (resultCode != RESULT_OK) {
                return;
            }

            List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (voiceSearchResults != null && !voiceSearchResults.isEmpty()) {
                String searchText = voiceSearchResults.get(0);
                tvSearchKeyword.setText(searchText);
                tvSearchKeyword.setSelection(searchText.length());
            }
        } else if (requestCode == REQUEST_CODE_ROOM_SELECTION) {
            if (resultCode != RESULT_OK) {
                return;
            }

            isSelectDirectMessageRoom = !data.getBooleanExtra(RoomFilterActivity.KEY_IS_TOPIC, false);
            selectedRoomId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1l);
            selectedOneToOneRoomMemberId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_MEMBER_ID, -1l);

            searchPresenter.onRoomChanged(selectedRoomId, selectedOneToOneRoomMemberId);

        } else if (requestCode == REQUEST_CODE_MEMBER_SELECTION) {
            if (resultCode != RESULT_OK) {
                return;
            }

            selectedMemberId = data.getLongExtra(MemberFilterActivity.KEY_FILTERED_MEMBER_ID, -1l);
            searchPresenter.onWriterChanged(selectedMemberId);
        }
    }

    void showDeleteConfirmDialog() {
        if (deleteConfirmDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,
                    R.style.JandiTheme_AlertDialog_FixWidth_300);
            builder.setTitle(JandiApplication.getContext().getString(R.string.jandi_title_delete));
            builder.setMessage(
                    JandiApplication.getContext().getString(R.string.jandi_ask_delete_all_history));

            builder.setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                searchPresenter.onDeleteaAllHistoryItem();
                AnalyticsUtil.sendEvent(screenMode,
                        AnalyticsValue.Action.DeleteAllKeywords);
            });

            builder.setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
            });

            deleteConfirmDialog = builder.create();
        }

        deleteConfirmDialog.show();
    }

    public void showChooseRoomDialog() {
        if (chooseRoomDialog == null) {
            View view = LayoutInflater.from(this).inflate((R.layout.fragment_choose_room_popup), null);
            TextView tvAllRoomButton = (TextView) view.findViewById(R.id.tv_all_room_button);
            TextView tvJoinedRoomButton = (TextView) view.findViewById(R.id.tv_joined_room_button);
            TextView tvChooseRoomButton = (TextView) view.findViewById(R.id.tv_choose_room_Button);
            tvAllRoomButton.setOnClickListener(v -> {
                selectedRoomId = -1l;
                searchPresenter.onAccessTypeChanged("accessible");
                chooseRoomDialog.dismiss();
                AnalyticsUtil.sendEvent(screenMode,
                        AnalyticsValue.Action.ChooseRoomFilter, AnalyticsValue.Label.AllRoom);
            });
            tvJoinedRoomButton.setOnClickListener(v -> {
                selectedRoomId = -1l;
                searchPresenter.onAccessTypeChanged("joined");
                chooseRoomDialog.dismiss();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.ChooseRoomFilter, AnalyticsValue.Label.JoinedRoom);
            });
            tvChooseRoomButton.setOnClickListener(v -> {
                if (isSelectDirectMessageRoom) {
                    RoomFilterActivity.startForResultWithDirectMessageId(
                            this, -1, REQUEST_CODE_ROOM_SELECTION);
                } else {
                    RoomFilterActivity.startForResultWithTopicId(
                            this, -1, REQUEST_CODE_ROOM_SELECTION);
                }
                chooseRoomDialog.dismiss();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UniversalSearch,
                        AnalyticsValue.Action.ChooseRoomFilter, AnalyticsValue.Label.SelectRoom);
            });
            chooseRoomDialog = new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                    .setView(view)
                    .create();
        }
        chooseRoomDialog.show();
    }

    @Override
    protected void onDestroy() {
        searchPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void setSearchHints(List<String> keywords) {
        searchQueryAdapter.clear();
        searchQueryAdapter.addAll(keywords);
    }

    private void moveDirectMessage(long memberId) {
        MessageListV2Activity_.intent(this)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(memberId)
                .roomId(-1)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @Override
    public void showJoinRoomDialog(TopicRoom topicRoom, long linkId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        builder.setMessage(R.string.jandi_ask_join_topic)
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    int type = topicRoom.isPublicTopic() ?
                            JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
                    searchPresenter.onJoinTopic(topicRoom.getId(), type, linkId);
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null);
        builder.show();
    }

    @Override
    public void setPricingLimitView(Boolean isLimited) {
        if (isLimited) {
            layoutPricingPlanWarning.setVisibility(View.VISIBLE);
            PricingPlanWarningViewController pricingPlanWarningViewController =
                    PricingPlanWarningViewController.newInstance(this,
                            layoutPricingPlanWarning,
                            PricingPlanWarningViewController.TYPE_MSG_SEARCH
                    );
            pricingPlanWarningViewController.showRemoveButton(() -> {
                layoutPricingPlanWarning.setVisibility(View.GONE);
            });
        } else {
            layoutPricingPlanWarning.setVisibility(View.GONE);
        }
    }
}