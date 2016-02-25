package com.tosslab.jandi.app.ui.message.v2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog_;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.KeyboardAreaController;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.imeissue.EditableAccomodatingLatinIMETypeNullIssues;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.transform.TransformConfig;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 16. 2. 16..
 */

@EFragment(R.layout.fragment_message_list)
public class MessageListV2Fragment extends Fragment implements MessageListV2Activity
        .OnBackPressedListener, MessageListV2Activity.OnKeyPressListener, MessageListV2Presenter.View {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_WINDOW_PERMISSION = 102;

    // EASTER EGG SNOW
    public static boolean SNOWING_EASTEREGG_STARTED = false;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    @FragmentArg
    int entityType;
    @FragmentArg
    long entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    boolean isFromPush = false;
    @FragmentArg
    long teamId;
    @FragmentArg
    long lastReadLinkId = -1;
    @FragmentArg
    long roomId;
    @FragmentArg
    long firstCursorLinkId = -1;

    @ViewById(R.id.lv_messages)
    RecyclerView lvMessages;
    @ViewById(R.id.btn_send_message)
    View sendButton;
    @ViewById(R.id.et_message)
    BackPressCatchEditText etMessage;
    @ViewById(R.id.vg_messages_preview_last_item)
    View vgPreview;
    @ViewById(R.id.iv_message_preview_user_profile)
    SimpleDraweeView ivPreviewProfile;
    @ViewById(R.id.tv_message_preview_user_name)
    TextView tvPreviewUserName;
    @ViewById(R.id.tv_message_preview_content)
    TextView tvPreviewContent;
    @ViewById(R.id.vg_messages_input)
    View vgMessageInput;
    @ViewById(R.id.vg_messages_go_to_latest)
    View vgMoveToLatest;
    @ViewById(R.id.vg_messages_disable_alert)
    View vDisabledUser;
    @ViewById(R.id.layout_messages_empty)
    LinearLayout layoutEmpty;
    @ViewById(R.id.layout_messages_loading)
    View vgProgressForMessageList;
    @ViewById(R.id.img_go_to_latest)
    View vMoveToLatest;
    @ViewById(R.id.progress_go_to_latest)
    View progressGoToLatestView;
    @ViewById(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.iv_messages_preview_sticker_image)
    SimpleDraweeView ivSticker;
    @ViewById(R.id.vg_message_offline)
    View vgOffline;
    @ViewById(R.id.progress_message)
    View oldProgressBar;
    @ViewById(R.id.lv_messages)
    RecyclerView messageListView;
    @ViewById(R.id.btn_message_action_button_1)
    ImageView btnActionButton1;
    @ViewById(R.id.btn_message_action_button_2)
    ImageView btnActionButton2;
    @ViewById(R.id.btn_show_mention)
    ImageView btnShowMention;
    @ViewById(R.id.vg_option_space)
    ViewGroup vgOptionSpace;
    @ViewById(R.id.vg_easteregg_snow)
    FrameLayout vgEasterEggSnow;

    @Bean
    MessageListV2Presenter messageListPresenter;

    private MessageAdapter messageAdapter;

    private OfflineLayer offlineLayer;

    private boolean isForeground = true;

    private ProgressWheel progressWheel;

    private File photoFileByCamera;

    ////////////////////////////////////////// Life cycle //////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            photoFileByCamera
                    = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }
        messageListPresenter.setView(this);
        messageListPresenter.onEventBusRegister();
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        PushMonitor.getInstance().register(roomId);

        messageListPresenter.removeNotificationSameEntityId();
        messageListPresenter.initDownloadState();

        messageListPresenter.refreshNewMessage();
        EventBus.getDefault().post(new MainSelectTopicEvent(roomId));

        messageListPresenter.initMentionControlViewModel(getActivity(), etMessage);
        if (NetworkCheckUtil.isConnected()) {
            dismissOfflineLayer();
        } else {
            showOfflineLayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isForeground = false;
        PushMonitor.getInstance().unregister(roomId);
        if (roomId > 0) {
            messageListPresenter.saveTempWritingMessage(etMessage.getText().toString());
        }
        messageListPresenter.removeMentionClipboardListener();
        messageListPresenter.hideUploadMenuAction();
        messageListPresenter.hideKeyboardAction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageListPresenter.onDestroy();
        messageListPresenter.onEventBusUnregister();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// Initial views////////////////////////////////////////////////
    @AfterInject
    void initObject() {
        SendMessageRepository.getRepository().deleteAllOfCompletedMessages();
        messageListPresenter.setFirstCursorLinkId(firstCursorLinkId);
        SNOWING_EASTEREGG_STARTED = false;
    }

    @AfterViews
    void initViews() {
        trackSprinklr();
        setUpActionbar();
        setHasOptionsMenu(true);
        initAdapter();
        setListView();
        showLoadingView();
        messageListPresenter.setMessageAdapter(messageAdapter);
        messageListPresenter.setFragment(this);

        offlineLayer = new OfflineLayer(vgOffline);

        if (!NetworkCheckUtil.isConnected()) {
            offlineLayer.showOfflineView();
        }

        progressWheel = new ProgressWheel(getActivity());

        messageListPresenter.initMessageList(teamId, roomId, entityId, entityType, lastReadLinkId);

        messageListPresenter.getAnnouncement();

        messageListPresenter.setKeyboardArea(btnActionButton1, btnActionButton2, etMessage, vgOptionSpace);

        initKeyboardEvent();

        setListItemClickListener();

        setListViewScrollAction();

        setUpListTouchListener();

        setEditTextListeners();

        setEditTextTouchEvent();

        messageListPresenter.initEditTextMessage();

        messageListPresenter.initUserDisabled();

        messageListPresenter.initAnnouncementListeners();

        messageListPresenter.sendAnalyticsName();

        showCoachMark();
    }

    public void initAdapter() {
        messageAdapter = new MainMessageListAdapter(getContext());
        messageAdapter.setEntityId(entityId);
    }

    private void setListItemClickListener() {
        // 아이템 클릭 했을 때의 액션
        messageAdapter.setOnItemClickListener((adapter, position) -> {
            // hide all
            messageListPresenter.messageItemClick(messageListPresenter.getItem(position), position);
        });

        // 아이템 롱클릭했을때 액션
        messageAdapter.setOnItemLongClickListener((adapter, position) -> {
            messageListPresenter.messageItemLongClick(messageListPresenter.getItem(position));
            return true;
        });

    }

    private void setListViewScrollAction() {
        // 스크롤 했을 때 동작
        messageListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                    setPreviewVisible(false);
                }
            }
        });
    }

    private void setUpActionbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            activity.setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        }

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(EntityManager.getInstance().getEntityNameById(entityId));
    }

    public void setListView() {
        MessageListHeaderAdapter messageListHeaderAdapter =
                new MessageListHeaderAdapter(getContext(), messageAdapter);
        lvMessages.setAdapter(messageAdapter);
        lvMessages.setItemAnimator(null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        lvMessages.setLayoutManager(layoutManager);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(messageAdapter)
                .setRecyclerView(lvMessages)
                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
                .build();

        lvMessages.addItemDecoration(stickyHeadersItemDecoration);
    }

    private void trackSprinklr() {
        int screenView = messageListPresenter.getScreenViewMode(entityType);

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, screenView)
                        .build());
    }

    private void initKeyboardEvent() {
        etMessage.setOnKeyListener((v, keyCode, event) -> {
            LogUtil.d("In etMessage KeyCode : " + keyCode);
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && getResources().getConfiguration().keyboard != Configuration
                    .KEYBOARD_NOKEYS) {

                if (!event.isShiftPressed()) {
                    onSendClick();
                    return true;
                } else {
                    return false;
                }
            }

            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                //We only look at ACTION_DOWN in this code, assuming that ACTION_UP is redundant.
                // If not, adjust accordingly.
            } else if (event.getUnicodeChar() ==
                    (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)) {
                //We are ignoring this character, and we want everyone else to ignore it, too, so
                // we return true indicating that we have handled it (by ignoring it).
                return true;
            }
            return false;
        });
    }

    private void setUpListTouchListener() {
        if (lvMessages != null) {
            lvMessages.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    messageListPresenter.hideAllKeyboardArea();
                }
                return false;
            });
        }
    }

    private void setEditTextListeners() {
        etMessage.setOnBackPressListener(() -> {
            if (messageListPresenter.isKeyboardOpened()) {
                //키보드가 열려져 있고 그 위에 스티커가 있는 상태에서 둘다 제거 할때 속도를 맞추기 위해 딜레이를 줌
                Observable.just(1)
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
                            messageListPresenter.hideStickerMenuAction();
                            messageListPresenter.hideUploadMenuAction();
                        });
            }
            return false;
        });

        TextCutter.with(etMessage)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });
    }

    private void setEditTextTouchEvent() {
        etMessage.setOnClickListener(v -> {
            messageListPresenter.hideStickerMenuAction();
            messageListPresenter.hideUploadMenuAction();
        });
    }

    private void showCoachMark() {
        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(
                entityType == JandiConstants.TYPE_DIRECT_MESSAGE, getActivity());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Click(R.id.vg_messages_preview_last_item)
    void onPreviewClick() {
        setPreviewVisible(false);
        moveLastPage();
    }

    @Click(R.id.btn_send_message)
    void onSendClick() {
        messageListPresenter.sendMessage(etMessage.getText().toString());
    }

    @Click(R.id.btn_show_mention)
    void onMentionClick() {
        etMessage.requestFocus();
        messageListPresenter.writeMentionCharacter(etMessage);
    }

    @Click(R.id.iv_messages_preview_sticker_close)
    void onStickerPreviewClose() {
        messageListPresenter.stickerPreviewClose(TextUtils.isEmpty(etMessage.getText()));
    }

    @Click(R.id.et_message)
    void onMessageInputClick() {
        messageListPresenter.sendAnalyticsEvent(AnalyticsValue.Action.MessageInputField);
    }

    @TextChange(R.id.et_message)
    void onMessageEditChange(TextView tv, CharSequence text) {
        messageListPresenter.buttonEnableControl(text);
    }

    @EditorAction(R.id.et_message)
    boolean onMessageDoneClick(TextView tv, int actionId) {
        LogUtil.d(tv.toString() + " ::: " + actionId);

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            messageListPresenter.hideKeyboardAction();
        }

        return false;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissLoadingView() {
        vgProgressForMessageList.animate()
                .alpha(0f)
                .setDuration(250);
    }

    public void showLoadingView() {
        // 프로그레스를 좀 더 부드럽게 보여주기 위해서 애니메이션
        vgProgressForMessageList.setAlpha(0f);
        vgProgressForMessageList.animate()
                .alpha(1.0f)
                .setDuration(150);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 100ms - 100ms 간격으로 화면 갱신토록 함
        // 사유 1: 헤더가 사이즈 변경을 인식하는데 시간이 소요됨
        // 사유 2: 2번 하는 이유는 첫 100ms 에서 갱신안되는 단말을 위함...
        // 구형단말을 위한 배려 -_-v
        Observable.just(1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    justRefresh();
                    messageListPresenter.onConfigurationChanged();
                });

    }

    //TODO
    @Override
    public boolean onBackPressed() {
        if (messageListPresenter.getCurrentKeyButtonAction()
                == KeyboardAreaController.ButtonAction.STICKER) {
            messageListPresenter.hideStickerMenuAction();
            return true;
        }

        if (messageListPresenter.getCurrentKeyButtonAction()
                == KeyboardAreaController.ButtonAction.UPLOAD) {
            messageListPresenter.hideUploadMenuAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKey(int keyCode, KeyEvent event) {
        if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_POUND)
                || (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_PERIOD)
                || (keyCode >= KeyEvent.KEYCODE_GRAVE && keyCode <= KeyEvent.KEYCODE_AT)) {
            if (!etMessage.isFocused()) {
                etMessage.requestFocus();
                etMessage.setSelection(etMessage.getText().length());
                return true;
            }
        }
        return false;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOldLoadProgress() {
        if (oldProgressBar.getVisibility() != View.GONE) {
            return;
        }

        oldProgressBar.setVisibility(View.VISIBLE);

        Animation inAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f);
        inAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
        inAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        inAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());

        oldProgressBar.startAnimation(inAnim);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void justRefresh() {
        int itemCount = messageAdapter.getItemCount();
        if (itemCount > 0) {
            messageAdapter.notifyItemRangeChanged(0, itemCount);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshAll() {
        messageAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEmptyLayoutVisible(boolean visible) {
        if (visible) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOfflineLayer() {
        offlineLayer.dismissOfflineView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOfflineLayer() {
        offlineLayer.showOfflineView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearMessages() {
        messageAdapter.clear();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void scrollToPositionWithOffset(int itemPosition, int firstVisibleItemTop) {
        ((LinearLayoutManager) lvMessages.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLastPage() {
        if (lvMessages != null) {
            lvMessages.getLayoutManager().scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public int getFirstVisibleItemPositionFromListView() {
        return ((LinearLayoutManager) lvMessages.getLayoutManager()).findFirstVisibleItemPosition();
    }

    @Override
    public int getFirstVisibleItemTopFromListView() {
        View childAt = lvMessages.getLayoutManager().getChildAt(0);
        if (childAt != null) {
            return childAt.getTop();
        } else {
            return 0;
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showProgressWheel() {
        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissProgressWheel() {
        if (progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissOldLoadProgress() {
        if (oldProgressBar.getVisibility() != View.VISIBLE) {
            return;
        }

        Animation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f);
        outAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
        outAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        outAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());

        outAnim.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                oldProgressBar.setVisibility(View.GONE);
            }
        });
        oldProgressBar.startAnimation(outAnim);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void scrollToPosition(int itemPosition) {
        lvMessages.getLayoutManager().scrollToPosition(itemPosition);
    }

    @Override
    public int getListViewMeasuredHeight() {
        return lvMessages.getMeasuredHeight();
    }

    @Override
    public int getLastVisibleItemPositionFromListView() {
        return ((LinearLayoutManager) lvMessages.getLayoutManager()).findLastVisibleItemPosition();
    }

    @Override
    public boolean isVisibleLastItem() {
        return ((LinearLayoutManager) lvMessages.getLayoutManager())
                .findFirstVisibleItemPosition() == messageAdapter.getItemCount() - 1;
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setPreviewName(String name) {
        tvPreviewUserName.setText(name);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setPreviewContent(SpannableStringBuilder content) {
        tvPreviewContent.setText(content);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setPreviewVisible(boolean isVisible) {
        if (isVisible) {
            vgPreview.setVisibility(View.VISIBLE);
        } else {
            vgPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void showPreviewProfileImage(boolean isBot, Uri uri) {
        if (!isBot) {
            ImageUtil.loadProfileImage(ivPreviewProfile, uri, R.drawable.profile_img);
        } else {
            RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
                    TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);

            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .backgroundColor(Color.WHITE)
                    .roundingParams(circleRoundingParams)
                    .load(uri)
                    .into(ivPreviewProfile);

        }
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @UiThread
    public void showNoMoreMessage() {
        ColoredToast.showWarning(getActivity().getString(R.string.warn_no_more_messages));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
        getActivity().finish();
    }

    @Override
    public boolean isForeground() {
        return isForeground;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showMessageEmptyLayout() {
        if (layoutEmpty == null) {
            return;
        }
        layoutEmpty.removeAllViews();

        LayoutInflater.from(getActivity()).inflate(R.layout.view_message_list_empty, layoutEmpty, true);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showTeamMemberEmptyLayout() {
        if (layoutEmpty == null) {
            return;
        }
        layoutEmpty.removeAllViews();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_team_member_empty, layoutEmpty, true);
        View.OnClickListener onClickListener = v -> {
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_TOPIC_CHAT);
            invitationDialogExecutor.execute();
        };
        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(onClickListener);
        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(onClickListener);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearEmptyMessageLayout() {
        if (layoutEmpty != null) {
            layoutEmpty.removeAllViews();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showTopicMemberEmptyLayout() {

        if (layoutEmpty == null) {
            return;
        }

        layoutEmpty.removeAllViews();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_topic_member_empty, layoutEmpty, true);
        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));
        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setVisibleBtnMention(boolean visible) {
        if (visible) {
            btnShowMention.setVisibility(View.VISIBLE);
        } else {
            btnShowMention.setVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setVisibleStickerPreview(boolean visible) {
        if (visible) {
            vgStickerPreview.setVisibility(View.VISIBLE);
        } else {
            vgStickerPreview.setVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEnableSendButton(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setSendEditText(String text) {
        if (etMessage != null) {
            etMessage.setText(text);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showGrayToast(String message) {
        ColoredToast.showGray(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();

        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.message_list_menu_basic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = messageListPresenter.excuteMenuCommand(this, item);
        if (result) {
            return result;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void moveFileDetailActivity(long messageId, long roomId, long selectMessageId) {
        FileDetailActivity_
                .intent(this)
                .fileId(messageId)
                .roomId(roomId)
                .selectMessageId(selectMessageId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void showDummyMessageDialog(long localId) {
        DummyMessageDialog_.builder()
                .localId(localId)
                .build()
                .show(getActivity().getFragmentManager(), "dialog");
    }

    @Override
    public void showMessageMenuDialog(boolean isDirectMessage, boolean myMessage,
                                      ResMessages.TextMessage textMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage(
                textMessage, myMessage, isDirectMessage);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
    }

    @Override
    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage
                (commentMessage, false);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
    }


    @Override
    public void showStickerMessageMenuDialog(
            boolean myMessage, ResMessages.StickerMessage StickerMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByStickerMessage
                (StickerMessage, myMessage);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
    }

    @Override
    public void showStickerPreview(StickerInfo stickerInfo) {
        StickerManager.LoadOptions loadOption = new StickerManager.LoadOptions();
        loadOption.scaleType = ScalingUtils.ScaleType.CENTER_CROP;
        StickerManager.getInstance()
                .loadSticker(ivSticker, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), loadOption);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showLeavedMemberDialog() {
        String name = EntityManager.getInstance().getEntityNameById(entityId);
        String msg = getActivity().getString(R.string.jandi_no_long_team_member, name);

        AlertUtil.showConfirmDialog(getActivity(), msg, null, false);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setDisableUser() {
        sendLayoutVisibleGone();
        vDisabledUser.setVisibility(View.VISIBLE);
        setPreviewVisible(false);
    }

    private void sendLayoutVisibleGone() {
        if (vgMessageInput != null) {
            vgMessageInput.setVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setChangedTopicTitle(String changedEntityName) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(changedEntityName);
    }

    @Override
    public String getTopicTitle() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString();
    }

    @Override
    public void moveDirectMessageList(long userId) {
        EntityManager entityManager = EntityManager.getInstance();
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(userId)
                .isFavorite(entityManager.getEntityById(userId).isStarred)
                .isFromPush(isFromPush)
                .start();
    }

    @Override
    public void moveFileUploadPreviewActivity(List<String> filePaths) {
        FileUploadPreviewActivity_.intent(this)
                .singleUpload(true)
                .realFilePathList((ArrayList) filePaths)
                .selectedEntityIdToBeShared(entityId)
                .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
    }

    @Override
    public void moveMemberListActivity() {
        MembersListActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(entityId)
                .type(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC)
                .start();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshActionBar() {
        setUpActionbar();
        getActivity().invalidateOptionsMenu();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void closeDialogFragment() {
        android.app.Fragment dialogFragment = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null && dialogFragment instanceof android.app.DialogFragment) {
            ((android.app.DialogFragment) dialogFragment).dismiss();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        () -> Observable.just(1)
                                .delay(300, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    messageListPresenter.showUploadMenuAction();
                                }, Throwable::printStackTrace))
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        messageListPresenter.setSavedInstancePhotoFile(outState);
    }


    @OnActivityResult(TopicDetailActivity.REQUEST_DETAIL)
    void onTopicDetailResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        boolean leave = data.getBooleanExtra(TopicDetailActivity.EXTRA_LEAVE, false);

        if (leave) {
            getActivity().finish();
        }
    }

    @OnActivityResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH)
    void onFileDetailResult(Intent data) {
        if (data != null && data.getBooleanExtra(EXTRA_FILE_DELETE, false)) {
            int fileId = data.getIntExtra(EXTRA_FILE_ID, -1);
            if (fileId != -1) {
                messageListPresenter.FileUpdateStatusArchived(fileId);
            }
        } else {
            messageListPresenter.refreshNewMessage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        UnLockPassCodeManager.getInstance().setUnLocked(true);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case FilePickerViewModel.TYPE_UPLOAD_GALLERY:
                break;
            case FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO:
                String savedFilePath = null;
                if (photoFileByCamera != null) {
                    savedFilePath = photoFileByCamera.getPath();
                }
                messageListPresenter
                        .uploadFileTakePhotoByActivityResult(requestCode, intent, savedFilePath);
                break;
            case FilePickerViewModel.TYPE_UPLOAD_EXPLORER:
                messageListPresenter.uploadFileExplorerByActivityResult(requestCode, intent);
                break;
            case FileUploadPreviewActivity.REQUEST_CODE:
                messageListPresenter.requestUploadFileByActivityResult(intent);
                break;
            default:
                break;
        }
    }
}