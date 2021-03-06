package com.tosslab.jandi.app.ui.search.main;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.TopicInfoDialog;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.search.file.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.filter.member.MemberFilterActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapterViewModel;
import com.tosslab.jandi.app.ui.search.main.dagger.DaggerSearchComponent;
import com.tosslab.jandi.app.ui.search.main.dagger.SearchModule;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
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

    @Bind(R.id.iv_search_mic)
    ImageView ivSearchMic;

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

    private String beforeText = "";

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

        EventBus.getDefault().register(this);
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
        if (beforeText.isEmpty() && !TextUtils.isEmpty(text)) {
            setMicToClearImage();
        } else if (!beforeText.isEmpty() && TextUtils.isEmpty(text)) {
            setClearToMicImage();
        }
        searchPresenter.onSearchKeywordChanged(text.toString());
        beforeText = text.toString();
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
//                searchPresenter.onInitPricingInfo();
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
            searchPresenter.setChangeIsShowUnjoinedTopic(isChecked);
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

        searchAdapterViewModel.setOnClickRoomSelectionButtonListener(() -> searchPresenter.onRoomSelect());
        LinearLayoutManager layoutManager = (LinearLayoutManager) lvSearchResult.getLayoutManager();

        searchAdapterViewModel.setOnClickOneToOneRoomListener(memberId -> {
            searchPresenter.onOneToOneRoomClick(memberId);
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
        startActivity(Henson.with(this)
                .gotoMessageListV2Activity()
                .entityId(entityId)
                .entityType(entityType)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    public void moveToPollActivity(long pollId) {
        PollDetailActivity.start(this, pollId);
    }

    @Override
    public void moveToMessageActivityFromSearch(long roomId, long entityId, int entityType, long linkId) {
        startActivity(Henson.with(this)
                .gotoMessageListV2Activity()
                .roomId(roomId)
                .entityId(entityId)
                .entityType(entityType)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

        if (TextUtils.isEmpty(tvSearchKeyword.getText())) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            tvSearchKeyword.setText("");
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

    @Override
    public void showChooseRoomDialog(boolean showAllRoom) {
        if (chooseRoomDialog == null) {
            View view = LayoutInflater.from(this).inflate((R.layout.fragment_choose_room_popup), null);
            TextView tvAllRoomButton = (TextView) view.findViewById(R.id.tv_all_room_button);
            TextView tvJoinedRoomButton = (TextView) view.findViewById(R.id.tv_joined_room_button);
            TextView tvChooseRoomButton = (TextView) view.findViewById(R.id.tv_choose_room_Button);
            if (showAllRoom) {
                tvAllRoomButton.setVisibility(View.VISIBLE);
                tvAllRoomButton.setOnClickListener(v -> {
                    selectedRoomId = -1l;
                    searchPresenter.onAccessTypeChanged("accessible");
                    chooseRoomDialog.dismiss();
                    AnalyticsUtil.sendEvent(screenMode,
                            AnalyticsValue.Action.ChooseRoomFilter, AnalyticsValue.Label.AllRoom);
                });
            } else {
                tvAllRoomButton.setVisibility(View.GONE);
            }
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
    public void showShouldOpenedUser() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.topic_search_1on1_unable)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        searchPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
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

    @Override
    public void moveDirectMessage(long memberId) {
        startActivity(Henson.with(this)
                .gotoMessageListV2Activity()
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(memberId)
                .roomId(-1)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

    public void setMicToClearImage() {
        ivSearchMic.setImageResource(R.drawable.account_icon_close);
    }

    public void setClearToMicImage() {
        ivSearchMic.setImageResource(R.drawable.account_icon_mic);
    }

    @Override
    public void setPricingLimitView(Boolean isLimited) {
//        if (isLimited) {
//            layoutPricingPlanWarning.setVisibility(View.VISIBLE);
//            PricingPlanWarningViewController pricingPlanWarningViewController =
//                    PricingPlanWarningViewController.with(this, layoutPricingPlanWarning)
//                            .addViewRemoveButton(() -> {
//                                layoutPricingPlanWarning.setVisibility(View.GONE);
//                            });
//            if (isOnlyMessageMode) {
//                pricingPlanWarningViewController.bind(PricingPlanWarningViewController.TYPE_MSG_MESSAGE_SEARCH);
//            } else {
//                pricingPlanWarningViewController.bind(PricingPlanWarningViewController.TYPE_MSG_UNIV_SEARCH);
//            }
//        } else {
//            layoutPricingPlanWarning.setVisibility(View.GONE);
//        }
    }

    @Override
    public void showUsageLimitDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.pricingplan_restrictions_view_message_alert_title)
                .setMessage(R.string.pricingplan_restrictions_view_message_alert_body)
                .setNegativeButton(this.getText(R.string.intercom_close), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.pricingplan_restrictions_fileupload_popup_seedetail,
                        (dialog, which) -> {
                            movePricePlan(this);
                        }).show();
    }

    private void movePricePlan(Context context) {
        if (context != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            String lang = locale.getLanguage();
            String url = "https://www.jandi.com/landing/kr/pricing";

            if (TextUtils.equals(lang, "en")) {
                url = "www.jandi.com/landing/en/pricing";
            } else if (TextUtils.equals(lang, "ja")) {
                url = "www.jandi.com/landing/jp/pricing";
            } else if (TextUtils.equals(lang, "ko")) {
                url = "www.jandi.com/landing/kr/pricing";
            } else if (TextUtils.equals(lang, "zh-cn")) {
                url = "www.jandi.com/landing/zh-cn/pricing";
            } else if (TextUtils.equals(lang, "zh-tw")) {
                url = "www.jandi.com/landing/zh-tw/pricing";
            }

            ApplicationUtil.startWebBrowser(context, url);
        }
    }

    public void onEvent(ShareFileEvent event) {
        long fileId = event.getId();
        searchPresenter.addFileSharedEntity(fileId, event.getShareEntities());
    }

    public void onEvent(UnshareFileEvent event) {
        long fileId = event.getFileId();
        long roomId = event.getRoomId();
        searchPresenter.removeFileSharedEntity(fileId, roomId);
    }
}