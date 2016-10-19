package com.tosslab.jandi.app.ui.poll.detail;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.entities.MentionableMembersRefreshEvent;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.PollDataChangedEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.local.orm.domain.ReadyCommentForPoll;
import com.tosslab.jandi.app.local.orm.repositories.ReadyCommentForPollRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel_;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.poll.detail.adapter.PollDetailAdapter;
import com.tosslab.jandi.app.ui.poll.detail.adapter.view.PollDetailDataView;
import com.tosslab.jandi.app.ui.poll.detail.component.DaggerPollDetailComponent;
import com.tosslab.jandi.app.ui.poll.detail.module.PollDetailModule;
import com.tosslab.jandi.app.ui.poll.detail.presenter.PollDetailPresenter;
import com.tosslab.jandi.app.ui.poll.participants.PollParticipantsActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;
import com.tosslab.jandi.app.views.controller.SoftInputAreaController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailActivity extends BaseAppCompatActivity implements PollDetailPresenter.View {

    public static final String KEY_POLL_ID = "pollId";

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @Inject
    PollDetailPresenter pollDetailPresenter;
    @Inject
    PollDetailDataView pollDetailDataView;

    @Bind(R.id.toolbar_poll_detail)
    Toolbar toolbar;
    @Bind(R.id.vg_poll_detail_input_comment)
    ViewGroup vgInputWrapper;
    @Bind(R.id.et_message)
    BackPressCatchEditText etComment;
    @Bind(R.id.btn_send_message)
    View btnSend;
    @Bind(R.id.vg_poll_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @Bind(R.id.vg_poll_detail_soft_input_area)
    ViewGroup vgSoftInputArea;
    @Bind(R.id.iv_poll_detail_preview_sticker_image)
    ImageView ivStickerPreview;
    @Bind(R.id.vg_poll_detail_soft_input_detector)
    SoftInputDetectLinearLayout vgSoftInputDetector;
    @Bind(R.id.btn_show_mention)
    View ivMention;
    @Bind(R.id.btn_poll_detail_action)
    ImageView btnAction;
    @Bind(R.id.lv_poll_detail)
    RecyclerView lvPollDetail;

    private ProgressWheel progressWheel;
    private StickerViewModel stickerViewModel;
    private ClipboardManager clipboardManager;
    private InputMethodManager inputMethodManager;
    private SoftInputAreaController softInputAreaController;
    private MentionControlViewModel mentionControlViewModel;

    private long pollId;
    private StickerInfo stickerInfo = NULL_STICKER;
    private boolean shouldRetrievePollDetail = false;
    private boolean shouldPrepareOptionsMenu;
    private String pollStatus;

    public static void start(Activity activity, long pollId) {
        Intent intent = new Intent(activity, PollDetailActivity.class);
        intent.putExtra(KEY_POLL_ID, pollId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_detail);

        PollDetailAdapter pollDetailAdapter = new PollDetailAdapter();
        injectComponent(pollDetailAdapter);

        ButterKnife.bind(this);

        initPollDetailViews(pollDetailAdapter);

        initObjects(savedInstanceState);

        setUpActionBar();

        initSoftInputAreaController();

        initCommentEditText();

        initProgressWheel();

        initStickers();

        initPollDetails();
    }

    private void injectComponent(PollDetailAdapter pollDetailAdapter) {
        DaggerPollDetailComponent.builder()
                .pollDetailModule(new PollDetailModule(this, pollDetailAdapter))
                .build()
                .inject(this);
    }

    private void initPollDetails() {
        if (pollId != -1) {
            pollDetailPresenter.onInitializePollDetail(pollId);
        }
    }

    private void initObjects(Bundle savedInstanceState) {
        stickerViewModel = StickerViewModel_.getInstance_(getBaseContext());
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        pollId = savedInstanceState != null
                ? savedInstanceState.getLong(KEY_POLL_ID, -1)
                : getIntent().getLongExtra(KEY_POLL_ID, -1);
    }

    private void initPollDetailViews(PollDetailAdapter adapter) {
        lvPollDetail.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        lvPollDetail.setAdapter(adapter);

        adapter.setOnCommentClickListener(comment -> hideKeyboard());
        adapter.setOnCommentLongClickListener(this::onCommentLongClick);
        adapter.setOnPollParticipantsClickListener(poll -> {
            pollDetailPresenter.onRequestShowPollParticipants(poll);
            sendAnalyticsEvent(AnalyticsValue.Action.ViewPollParticipant);
        });
        adapter.setOnPollItemParticipantsClickListener((poll, item) -> {
            pollDetailPresenter.onRequestShowPollItemParticipants(poll, item);
            sendAnalyticsEvent(AnalyticsValue.Action.ViewChoiceParticipant);
        });
        adapter.setOnPollVoteClickListener((pollId, votedItemSeqs) -> {
            pollDetailPresenter.onPollVoteAction(pollId, votedItemSeqs);
            sendAnalyticsEvent(AnalyticsValue.Action.SubmitVote);
        });
        adapter.setOnPollStarClickListener(poll -> {
            pollDetailPresenter.onChangePollStarredState(poll);

            boolean futureStarred = !poll.isStarred();
            AnalyticsValue.Label label = futureStarred ? AnalyticsValue.Label.Off : AnalyticsValue.Label.On;

            sendAnalyticsEvent(AnalyticsValue.Action.StarVote, label);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_POLL_ID, pollId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        if (shouldRetrievePollDetail) {
            pollDetailPresenter.reInitializePollDetail(pollId);
        }

        shouldRetrievePollDetail = true;
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);

        dismissStickerPreview();

        ReadyCommentForPollRepository.getRepository()
                .upsertReadyComment(
                        new ReadyCommentForPoll(pollId, etComment.getText().toString()));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        pollDetailPresenter.clearAllEventQueue();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1)
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    pollDetailDataView.notifyDataSetChanged();

                    if (softInputAreaController != null) {
                        softInputAreaController.onConfigurationChanged();
                    }

                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }

                });
    }

    @Override
    public void onBackPressed() {
        if (softInputAreaController != null && softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputArea(true, true);
        } else {
            super.onBackPressed();
        }
    }

    public void onEventMainThread(PollDataChangedEvent event) {
        pollDetailDataView.notifyDataSetChanged();
    }

    public void onEvent(SocketPollCommentCreatedEvent event) {
        Poll poll = event.getData() != null
                ? event.getData().getPoll() : null;
        if (poll == null || poll.getId() != pollId) {
            return;
        }

        ResMessages.Link linkComment = event.getData() != null
                ? event.getData().getLinkComment() : null;
        pollDetailPresenter.onCommentCreated(linkComment);
    }

    public void onEvent(SocketPollCommentDeletedEvent event) {
        Poll poll = event.getData() != null
                ? event.getData().getPoll() : null;
        if (poll == null || poll.getId() != pollId) {
            return;
        }

        ResMessages.Link linkComment = event.getData() != null
                ? event.getData().getLinkComment() : null;
        pollDetailPresenter.onCommentDeleted(linkComment);
    }

    public void onEvent(SocketPollEvent event) {
        if (event.getPoll() == null
                || event.getPoll().getId() != pollId) {
            return;
        }

        if (SocketPollEvent.Type.DELETED == event.getType()
                || SocketPollEvent.Type.FINISHED == event.getType()) {
            pollDetailPresenter.reInitializePollDetail(pollId);
        }
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (isFinishing()) {
            return;
        }
        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    public void onEvent(MessageStarredEvent event) {
        if (isFinishing()) {
            return;
        }

        long messageId = event.getMessageId();

        AnalyticsValue.Label label = AnalyticsValue.Label.On;
        switch (event.getAction()) {
            case STARRED:
                pollDetailPresenter.onChangeCommentStarredState(messageId, true);
                label = AnalyticsValue.Label.On;
                break;
            case UNSTARRED:
                pollDetailPresenter.onChangeCommentStarredState(messageId, false);
                label = AnalyticsValue.Label.Off;
                break;
        }
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Star, label);
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (isFinishing()) {
            return;
        }

        copyToClipboard(event.contentString);

        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Copy);
    }

    public void onEvent(TopicDeleteEvent event) {
        if (isFinishing()) {
            return;
        }

        pollDetailPresenter.onTopicDeleted(event.getId());
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (isFinishing()) {
            return;
        }

        pollDetailPresenter.onDeleteComment(event.messageType, event.messageId, event.feedbackId);
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Delete);
    }

    public void onEvent(ShowProfileEvent event) {
        long userEntityId = event.userId;

        MemberProfileActivity_.intent(this)
                .memberId(userEntityId)
                .start();

        AnalyticsValue.Action action = event.isFromComment
                ? AnalyticsValue.Action.ViewProfile_FromComment : AnalyticsValue.Action.ViewVoteCreator;

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PollDetail, action);
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);

        getSupportActionBar().setTitle(R.string.jandi_poll);
    }

    private void initCommentEditText() {
        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });
    }

    @OnFocusChange(R.id.et_message)
    void onEditTextFocusChange(boolean focus) {
        if (focus) {
            sendAnalyticsEvent(AnalyticsValue.Action.MessageInputField);
        }
    }

    @OnTextChanged(R.id.et_message)
    void onCommentTextChange(Editable editable) {
        setCommentSendButtonEnabled();
    }

    private void initStickers() {
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            if (oldSticker.getStickerGroupId() == groupId
                    && oldSticker.getStickerId().equals(stickerId)) {
                sendComment();
            } else {
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                vgStickerPreview.setVisibility(View.VISIBLE);
                StickerManager.getInstance()
                        .loadStickerDefaultOption(ivStickerPreview,
                                stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
                setCommentSendButtonEnabled();
                sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
            }
        });
        stickerViewModel.setType(StickerViewModel.TYPE_POLL_DETAIL);
    }

    private void initSoftInputAreaController() {
        softInputAreaController = new SoftInputAreaController(
                stickerViewModel, null,
                vgSoftInputDetector, vgSoftInputArea, null, btnAction,
                etComment);

        softInputAreaController.setOnStickerButtonClickListener(() -> {
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker);
        });

        softInputAreaController.init();
        softInputAreaController.setOnSoftInputAreaShowingListener((isShowing, softInputHeight) -> {
            View lastChild = lvPollDetail.getChildAt(lvPollDetail.getChildCount() - 1);
            int lastPosition = lvPollDetail.getAdapter().getItemCount() - 1;
            int childAdapterPosition = lvPollDetail.getChildAdapterPosition(lastChild);
            if (lastChild != null
                    && childAdapterPosition == lastPosition) {
                if (isShowing) {
                    lvPollDetail.post(() -> lvPollDetail.smoothScrollBy(0, softInputHeight));
                }

            }
        });
    }

    private void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    private void setCommentSendButtonEnabled() {
        boolean hasText = !TextUtils.isEmpty(etComment.getText())
                && TextUtils.getTrimmedLength(etComment.getText()) > 0;
        boolean enabled = vgStickerPreview.getVisibility() == View.VISIBLE || hasText;
        btnSend.setEnabled(enabled);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    private void initMentionControlViewModel(long pollId, List<Long> sharedTopicIds) {
        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(this,
                    etComment,
                    sharedTopicIds,
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);

            ReadyCommentForPoll readyComment = ReadyCommentForPollRepository.getRepository()
                    .getReadyComment(pollId);
            mentionControlViewModel.setUpMention(readyComment.getText());
            mentionControlViewModel.setOnMentionShowingListener(isShowing -> {
                if (mentionControlViewModel.hasMentionMember()) {
                    ivMention.setVisibility(isShowing ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            mentionControlViewModel.refreshMembers(sharedTopicIds);
        }

        removeClipboardListenerForMention();
        registerClipboardListenerForMention();
    }

    public void onEvent(MentionableMembersRefreshEvent event) {
        if (isFinishing() || mentionControlViewModel == null) {
            return;
        }

        runOnUiThread(() -> setMentionButtonVisibility(mentionControlViewModel.hasMentionMember()));
    }

    void setMentionButtonVisibility(boolean show) {
        ivMention.setVisibility(show
                ? View.VISIBLE : View.GONE);
    }

    public void registerClipboardListenerForMention() {
        runOnUiThread(() -> {
            if (mentionControlViewModel != null) {
                mentionControlViewModel.registClipboardListener();
            }
        });
    }

    public void removeClipboardListenerForMention() {
        runOnUiThread(() -> {
            if (mentionControlViewModel != null) {
                mentionControlViewModel.removeClipboardListener();
            }
        });
    }

    public boolean onCommentLongClick(ResMessages.OriginalMessage comment) {
        showChooseDialogIfNeed(comment);
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap);
        return true;
    }

    private void showChooseDialogIfNeed(ResMessages.OriginalMessage comment) {
        if (comment == null) {
            return;
        }

        shouldRetrievePollDetail = false;
        long myId = TeamInfoLoader.getInstance().getMyId();
        User me = TeamInfoLoader.getInstance().getUser(myId);

        boolean isMine = me != null
                && (me.getId() == comment.writerId || me.isTeamOwner());

        if (comment instanceof ResMessages.CommentMessage) {
            ManipulateMessageDialogFragment.newInstanceByCommentMessage(
                    (ResMessages.CommentMessage) comment, isMine)
                    .show(getSupportFragmentManager(), "choose_dialog");
        } else {
            if (!isMine) {
                return;
            }

            ManipulateMessageDialogFragment.newInstanceByStickerCommentMessage(
                    (ResMessages.CommentStickerMessage) comment, true)
                    .show(getSupportFragmentManager(), "choose_dialog");
        }
    }

    public void onEvent(MoveSharedEntityEvent event) {
        long entityId = event.getEntityId();

        moveToSharedEntity(entityId);

        sendAnalyticsEvent(AnalyticsValue.Action.TopicName);
    }

    private void moveToSharedEntity(long entityId) {
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        int entityType;
        boolean isStarred = false;
        boolean isUser = false;
        boolean isBot = false;

        if (teamInfoLoader.isTopic(entityId)) {
            if (teamInfoLoader.isPublicTopic(entityId)) {
                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            }
            isStarred = teamInfoLoader.isStarred(entityId);
        } else if (teamInfoLoader.isUser(entityId)) {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            isStarred = teamInfoLoader.isStarredUser(entityId);
            isUser = true;
            if (teamInfoLoader.isJandiBot(entityId)) {
                isBot = true;
            }
        } else {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        }

        if (isUser || isBot) {
            moveToMessageListActivity(entityId, entityType, -1, isStarred);
            return;
        } else {
            TopicRoom topic = teamInfoLoader.getTopic(entityId);
            if (topic.isJoined()) {

                moveToMessageListActivity(entityId, entityType, entityId, isStarred);
            } else {
                pollDetailPresenter.joinAndMove(topic);
            }
        }
    }

    @OnClick(R.id.iv_poll_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        setCommentSendButtonEnabled();
        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_cancel);
    }

    @OnClick(R.id.btn_send_message)
    void sendComment() {
        hideKeyboard();

        ResultMentionsVO mentionInfo = getMentionInfo();
        String message = mentionInfo.getMessage().trim();
        List<MentionObject> mentions = mentionInfo.getMentions();
        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            dismissStickerPreview();
            long stickerGroupId = stickerInfo.getStickerGroupId();
            String stickerId = stickerInfo.getStickerId();

            StickerRepository.getRepository().upsertRecentSticker(stickerGroupId, stickerId);

            pollDetailPresenter.onSendCommentWithSticker(
                    pollId, stickerGroupId, stickerId, message, mentions);

            stickerInfo = NULL_STICKER;
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Send);
        } else {
            pollDetailPresenter.onSendComment(pollId, message, mentions);
            sendAnalyticsEvent(AnalyticsValue.Action.Send);
        }

        etComment.setText("");
    }

    private ResultMentionsVO getMentionInfo() {
        if (mentionControlViewModel != null) {
            return mentionControlViewModel.getMentionInfoObject();
        } else {
            return new ResultMentionsVO("", new ArrayList<>());
        }
    }

    @OnClick(R.id.btn_show_mention)
    void onMentionClick() {
        etComment.requestFocus();

        boolean needSpace = needSpace(etComment.getSelectionStart(), etComment.getText().toString());
        int keyEvent = KeyEvent.KEYCODE_AT;

        BaseInputConnection inputConnection = new BaseInputConnection(etComment, true);
        if (needSpace) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        }
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));

        if (softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputAreaAndShowSoftInput();
        } else {
            softInputAreaController.showSoftInput();
        }
    }

    private boolean needSpace(int cursorPosition, String message) {
        int selectionStart = cursorPosition;
        if (selectionStart > 0) {
            CharSequence charSequence = message.substring(selectionStart - 1, selectionStart);
            return !TextUtils.isEmpty(charSequence.toString().trim());
        }
        return false;
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PollDetail, action);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, AnalyticsValue.Label label) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PollDetail, action, label);
    }

    @Override
    public void showKeyboard() {
        runOnUiThread(() -> inputMethodManager.showSoftInput(getCurrentFocus(), 0));
    }

    @Override
    public void hideKeyboard() {
        runOnUiThread(() -> {
            if (inputMethodManager.isAcceptingText()) {
                inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void copyToClipboard(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        runOnUiThread(() -> {
            ClipData clipData = ClipData.newPlainText(null, text);
            clipboardManager.setPrimaryClip(clipData);
        });
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void initPollDetailExtras(Poll poll) {
        if (poll == null) {
            return;
        }

        initOptionsMenu(poll);

        if ("deleted".equals(poll.getStatus())) {
            vgInputWrapper.setVisibility(View.GONE);
            return;
        }

        initMentionControlViewModel(poll.getId(), Arrays.asList(poll.getTopicId()));
    }

    private void initOptionsMenu(Poll poll) {
        long creatorId = poll.getCreatorId();
        long myId = TeamInfoLoader.getInstance().getMyId();

        if ("deleted".equals(poll.getStatus())) {
            shouldPrepareOptionsMenu = false;
        } else {
            shouldPrepareOptionsMenu = creatorId == myId
                    || TeamInfoLoader.getInstance().getUser(myId).isTeamOwner();
        }

        pollStatus = poll.getStatus();

        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (shouldPrepareOptionsMenu) {
            getMenuInflater().inflate(R.menu.poll_detail, menu);
            if ("finished".equals(pollStatus)) {
                menu.findItem(R.id.action_poll_detail_finish).setVisible(false);
            }
            return true;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_poll_detail_finish) {
            onPollFinishAction();
            return true;
        } else if (item.getItemId() == R.id.action_poll_detail_delete) {
            onPollDeleteAction();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onPollDeleteAction() {
        shouldRetrievePollDetail = false;
        AlertUtil.showDialog(this, R.string.jandi_action_poll_delete,
                R.string.jandi_ask_poll_delete,
                R.string.jandi_confirm, (dialog, which) -> {
                    sendAnalyticsEvent(AnalyticsValue.Action.DeletePoll);
                    pollDetailPresenter.onPollDeleteAction(pollId);
                },
                -1, null,
                R.string.jandi_cancel, null,
                true);
    }

    private void onPollFinishAction() {
        shouldRetrievePollDetail = false;
        AlertUtil.showDialog(this, R.string.jandi_action_poll_finish,
                R.string.jandi_ask_poll_finish,
                R.string.jandi_confirm, (dialog, which) -> {
                    sendAnalyticsEvent(AnalyticsValue.Action.ClosePoll);
                    pollDetailPresenter.onPollFinishAction(pollId);
                },
                -1, null,
                R.string.jandi_cancel, null,
                true);
    }

    @Override
    public void showCheckNetworkDialog(final boolean shouldFinishWhenConfirm) {
        runOnUiThread(() -> {
            shouldRetrievePollDetail = false;
            DialogInterface.OnClickListener confirmListener = null;
            if (shouldFinishWhenConfirm) {
                confirmListener = (dialog, which) -> finish();
            }

            AlertUtil.showCheckNetworkDialog(this, confirmListener);
        });
    }

    @Override
    public void showUnExpectedErrorToast() {
        ColoredToast.showError(R.string.jandi_err_unexpected);
    }

    @Override
    public void scrollToLastComment() {
        int itemCount = lvPollDetail.getAdapter().getItemCount();
        if (itemCount <= 0) {
            return;
        }

        lvPollDetail.smoothScrollToPosition(itemCount - 1);
    }

    @Override
    public void notifyDataSetChanged() {
        pollDetailDataView.notifyDataSetChanged();
    }

    @Override
    public void moveToMessageListActivity(long entityId, int entityType, long roomId,
                                          boolean isStarred) {
        runOnUiThread(() -> {
            MessageListV2Activity_.intent(PollDetailActivity.this)
                    .teamId(TeamInfoLoader.getInstance().getTeamId())
                    .entityId(entityId)
                    .entityType(entityType)
                    .roomId(roomId)
                    .isFromPush(false)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .start();
        });
    }

    @Override
    public void showCommentStarredSuccessToast() {
        ColoredToast.show(R.string.jandi_message_starred);
    }

    @Override
    public void showCommentUnStarredSuccessToast() {
        ColoredToast.show(R.string.jandi_unpinned_message);
    }

    @Override
    public void showCommentDeleteErrorToast() {
        ColoredToast.showError(R.string.err_entity_delete);
    }

    @Override
    public void showEmptyParticipantsToast() {
        SuperToast.cancelAllSuperToasts();
        ColoredToast.showError(R.string.jandi_empty_participants);
    }

    @Override
    public void showPollIsAnonymousToast() {
        SuperToast.cancelAllSuperToasts();
        ColoredToast.showError(R.string.jandi_poll_is_anonymous);
    }

    @Override
    public void showPollItemParticipants(long pollId, Poll.Item item) {
        PollParticipantsActivity.start(this, pollId, item);
    }

    @Override
    public void showPollDeleteSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_poll_deleted));
    }

    @Override
    public void showInvalidPollToast() {
        ColoredToast.showError(R.string.jandi_starmention_no_longer_in_topic);
    }

    @Override
    public void showPollParticipants(long pollId) {
        PollParticipantsActivity.startForAllParticipants(this, pollId);
    }

}
