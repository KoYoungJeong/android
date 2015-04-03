package com.tosslab.jandi.app.ui.message;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.DeleteTopicDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.lists.messages.MessageItemListAdapter;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.model.FileUploadUtil;
import com.tosslab.jandi.app.ui.message.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.model.RefreshRequestor;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
@Deprecated
@EActivity(R.layout.activity_message_list)
public class MessageListActivity extends BaseAnalyticsActivity {
    private final Logger log = Logger.getLogger(MessageListActivity.class);
    private final String DIALOG_TAG = "dialog";
    public EntityManager mEntityManager;
    @Extra
    int entityType;
    @Extra
    int entityId;
    @Extra
    boolean isFavorite = false;
    @Extra
    boolean isFromPush = false;
    @Extra
    int teamId;

    @Bean
    JandiEntityClient mJandiEntityClient;

    @Bean
    MessageManipulator messageManipulator;

    @ViewById(R.id.list_messages)
    ListView actualListView;
    @Bean
    MessageItemListAdapter messageItemListAdapter;

    @Bean
    MessageListModel messageListModel;

    @ViewById(R.id.et_message)
    EditText etMessage;
    @ViewById(R.id.btn_send_message)
    Button buttonSendMessage;
    private ChattingInfomations mChattingInformations;
    private Context mContext;
    private ProgressWheel mProgressWheel;
    // Update 관련
    private Timer mTimer;
    private MessageItemConverter mMessageItemConverter;
    private MessageState messageState;
    /**
     * *********************************************************
     * 사진 직접 찍어 올리기
     * **********************************************************
     */

    private Uri mImageUriFromCamera = null;
    private boolean isRefreshDisable = true;

    void initInformations() {
        mContext = getApplicationContext();
        mEntityManager = EntityManager.getInstance(mContext);

        if (isFromPush) {
            ResAccountInfo.UserTeam teamInfo = JandiAccountDatabaseManager.getInstance(mContext).getTeamInfo(teamId);
            if (teamInfo != null) {
                JandiAccountDatabaseManager.getInstance(mContext).updateSelectedTeam(teamId);
            }
        }

        messageState = new MessageState();

        mMessageItemConverter = new MessageItemConverter();

        mChattingInformations = new ChattingInfomations(mContext, entityId, entityType, isFromPush, isFavorite);
        messageManipulator.initEntity(
                mChattingInformations.entityType, mChattingInformations.entityId);

    }

    @AfterViews
    void initViews() {
        initInformations();
        initTempMessage();
        clearPushNotification(entityId);
//        BadgeUtils.clearBadge(mContext); // TODO BUG 현재 Activity 에서 홈버튼으로 돌아가면 아이콘에 뱃지가 0이 됨.
        initProgressWheel();

        setUpActionBar(mChattingInformations.entityName);
        setupScrollView();

        getMessages();
    }

    private void initTempMessage() {
        String tempMessage = messageListModel.getTempMessage(teamId, entityId);
        etMessage.setText(tempMessage);
        etMessage.setSelection(etMessage.getText().length());
    }

    private void showCachedMessage() {

        List<ResMessages.Link> cachedMessage = messageListModel.getCachedMessage(teamId, entityId);

        mMessageItemConverter.insertMessageItem(cachedMessage);
        messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
        refreshListAdapter();
        goToBottomOfListView();
    }

    private void setUpActionBar(String entityName) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        showActionBarTitle(entityName);
    }

    private void showActionBarTitle(String entityName) {
        getSupportActionBar().setTitle(entityName);
    }

    private void initProgressWheel() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

    private void clearPushNotification(int entityId) {
        // Notification 선택을 안하고 앱을 선택해서 실행시 Notification 제거
        if (entityId == JandiPreference.getChatIdFromPush(this)) {
            NotificationManager notificationManager;
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }
    }

    private void setupScrollView() {

        // Empty View를 가진 ListView 설정
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.view_message_list_empty, null);
        actualListView.setEmptyView(emptyView);
        actualListView.setAdapter(messageItemListAdapter);

        actualListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                messagesItemLongClicked(messageItemListAdapter.getItem(i));
                return true;
            }
        });
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                messagesItemClicked(messageItemListAdapter.getItem(i));
            }
        });

        // 스크롤이 맨 아래 근처에 위치하면 업데이트 내역이 생기면 바로 최하단으로 이동하고
        // 스크롤이 중간에서 위로 존재하면 최하단으로 이동하지 않는다.
        actualListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // not using this
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int thresholdOfStickingBottom = totalItemCount - firstVisibleItem - visibleItemCount;
                if (thresholdOfStickingBottom >= 5) {   // 이게 적절한 threshold 인지는 계속 하면서...
                    actualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                } else {
                    actualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                }
            }
        });
    }

    public void onEvent(RefreshOldMessageEvent event) {

        if (isRefreshDisable) {
            return;
        }

        reqeustMoreMessage();

    }

    @Background
    void reqeustMoreMessage() {
        pauseUpdateTimer();
        int count = messageItemListAdapter.getCount();
        String moreMessageResult = new RefreshRequestor(mContext, messageItemListAdapter, messageManipulator, messageState, mMessageItemConverter).getMoreMessageResult();
        refreshFinish(moreMessageResult, count);
        resumeUpdateTimer();

    }

    /**
     * 리스트 뷰의 최하단으로 이동한다.
     */
    @UiThread
    void goToBottomOfListView() {
        actualListView.setSelection(messageItemListAdapter.getCount() - 1);
    }

    @AfterTextChange(R.id.et_message)
    void messageTextChanged() {
        int inputLength = etMessage.getEditableText().length();
        buttonSendMessage.setSelected(inputLength > 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        resumeUpdateTimer();
    }

    // MessageListActivity 가 stack의 top 에 있을 때 다시 호출 되는 경우
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        initViews();
    }

    @Override
    public void onPause() {
        pauseUpdateTimer();
        setMarker();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        dismissProgressWheel();
        super.onStop();

        messageListModel.saveMessagesForCache(teamId, entityId, messageItemListAdapter);
        messageListModel.saveTempMessage(teamId, entityId, etMessage.getText().toString());
    }

    @Override
    public void finish() {
        // 현재 채팅방을 빠져 나오면 해당 chatId 를 초기화한다.
        JandiPreference.setActivatedChatId(this, JandiPreference.NOT_SET_YET);

        if (mChattingInformations.willBeFinishedFromPush) {
            // Push로부터 온 Activity는 하위 스택이 없으므로 MainTabActivity로 이동해야함.
            MainTabActivity_.intent(this)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .start();
            super.finish();
        } else {
            super.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int FAVORITE_MENU_ITEM = 0;

        getMenuInflater().inflate(R.menu.message_list_menu_basic, menu);
        MenuItem item = menu.getItem(FAVORITE_MENU_ITEM);
        if (mChattingInformations.isFavorite) {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav);
        } else {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav_off);
        }

        // DirectMessage의 경우 확장 메뉴가 없음.
        if (mChattingInformations.isDirectMessage() == false) {
            if (mChattingInformations.isMyEntity) {
                getMenuInflater().inflate(R.menu.manipulate_my_entity_menu, menu);
            } else {
                getMenuInflater().inflate(R.menu.manipulate_entity_menu, menu);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuCommand menuCommand = MenuCommandBuilder.init(MessageListActivity.this)
                .with(mJandiEntityClient)
                .with(mChattingInformations)
                .build(item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * *********************************************************
     * Timer Task
     * 주기적으로 message update 내역을 polling
     * **********************************************************
     */

    private void pauseUpdateTimer() {
        log.info("pauseUpdateTimer");
        if (mTimer != null)
            mTimer.cancel();
    }

    private void resumeUpdateTimer() {
        log.info("resumeUpdateTimer");
        TimerTask task = new UpdateTimerTask();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
        mTimer.schedule(task, 3000, 3000);  // 3초뒤, 3초마다
    }

    /**
     * *********************************************************
     * EntityManager 획득
     * Push 에서 바로 현재 Activity로 이동했다면 EntityManager를 호출한다.
     * **********************************************************
     */
    @Background
    public void getEntitiesInBackground() {
        try {
            // TODO Temp TeamId
            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(MessageListActivity.this).upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            JandiPreference.setBadgeCount(MessageListActivity.this, totalUnreadCount);
            BadgeUtils.setBadge(MessageListActivity.this, totalUnreadCount);
            getEntitiesSucceed(resLeftSideMenu);
        } catch (Exception e) {
            // TODO 에러 상황 나누기
            // TODO 네트웍 에러와 세션 만료.
            log.error("Get entities failed", e);
            getEntitiesFailed(getString(R.string.err_expired_session));
        }
    }

    @UiThread
    public void getEntitiesSucceed(ResLeftSideMenu resLeftSideMenu) {
        mEntityManager = EntityManager.getInstance(MessageListActivity.this);
        FormattedEntity entity = mEntityManager.getEntityById(mChattingInformations.entityId);
        if (entity == null) {
            getEntitiesFailed(getString(R.string.err_messages_invaild_entity));
            return;
        }
        mChattingInformations.loadExtraInfo();
        log.debug("entity name from push : " + mChattingInformations.entityName);
        showActionBarTitle(mChattingInformations.entityName);
        trackSigningInFromPush(mEntityManager);

        getMessages();
    }

    @UiThread
    public void getEntitiesFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
        returnToIntroStartActivity();
    }

    /**
     * Message List 획득
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     */

    @UiThread
    public void getMessages() {
        pauseUpdateTimer();

        showProgressWheel();

        // 현재의 entityId로 오는 푸시 메시지는 무시하기 위하여 entityId를 저장
        JandiPreference.setActivatedChatId(this, entityId);

        // 만약 push로부터 실행되었다면 Entity List를 우선 받는다.
        if (mEntityManager == null) {
            getEntitiesInBackground();
            return;
        }

        messageState.setFirstMessage(false);

        messageState.setFirstItemId(-1);

        trackGaMessageList(mEntityManager, mChattingInformations.entityType);
        getMessagesInBackground();
    }

    @UiThread
    void showProgressWheel() {
        dismissProgressWheel();
        if (mProgressWheel != null) {
            mProgressWheel.show();
        }
    }

    void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }


    @Deprecated
    @Background
    public void getMessagesInBackground() {
        try {

            if (messageState.getFirstItemId() == -1) {
                // 메세지를 가져온 기록이 없으면 캐싱된 데이터를 미리 보여줌
                showCachedMessage();
                dismissProgressWheel();
                log.debug("complete get cached data");
            }

            ResMessages restResMessages = messageManipulator.getMessages(messageState.getFirstItemId());
            log.debug("complete get server data");

            if (messageState.getFirstItemId() == -1) {

                clearMessageDatas();

                // 업데이트를 위해 가장 최신의 Link ID를 저장한다.
                messageState.setLastUpdateLinkId(restResMessages.lastLinkId);
                if (restResMessages.records == null || restResMessages.records.size() <= 0) {
                    showWarningEmpty();
                    return;
                }
                setMarker();
            }
            // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
//            messageState.setFirstMessage(restResMessages.isFirst);
//            // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
//            messageState.setFirstItemId(restResMessages.firstIdOfReceivedList);
//            log.debug("getMessagesInBackground : " + restResMessages.messageCount
//                    + " messages from " + messageState.getFirstItemId());
            getMessagesSucceed(restResMessages);
        } catch (JandiNetworkException e) {
            log.error("getMessagesInBackground : FAILED" + e.httpBody, e);
            log.error(e.getErrorInfo(), e);
            getMessagesFailed(getString(R.string.err_messages_get));
        } catch (Exception e) {
            getMessagesFailed(getString(R.string.err_messages_get));
        } finally {
            dismissProgressWheel();
        }
    }

    @UiThread
    void clearMessageDatas() {
        mMessageItemConverter.clear();
        messageItemListAdapter.clearAdapterWithoutUpdate();
    }

    @UiThread
    public void showWarningEmpty() {
        messageState.setLastUpdateLinkId(0);
        ColoredToast.showWarning(mContext, getString(R.string.warn_messages_empty));
        resumeUpdateTimer();
    }

    @UiThread
    public void getMessagesSucceed(ResMessages resMessages) {
        if (messageState.isFirstMessage()) {
            // 현재 entity에서 더 이상 가져올 메시지가 없다면 pull to refresh를 끈다.
            isRefreshDisable = true;
        } else {
            isRefreshDisable = false;
        }
        mMessageItemConverter.insertMessageItem(resMessages);
        messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
        refreshListAdapter();
        goToBottomOfListView();

        resumeUpdateTimer();
    }

    @UiThread
    public void getMessagesFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }

    @UiThread
    void refreshListAdapter() {
        messageItemListAdapter.notifyDataSetChanged();
    }

    @UiThread
    protected void refreshFinish(String errMessage, int lastMessageSize) {
        if (messageState.isFirstMessage()) {
            ColoredToast.showWarning(mContext, getString(R.string.warn_no_more_messages));
            isRefreshDisable = true;
        } else {
            isRefreshDisable = false;
        }

        if (errMessage == null) {
            // Success
            int index = messageItemListAdapter.getCount() - lastMessageSize + 1;
            log.debug("GetFutherMessagesTask : REFRESH at " + index);
            actualListView.setSelectionFromTop(index, 0);
            refreshListAdapter();
            // 리스트에 아이템이 추가되더라도 현재 위치를 고수하도록 이동한다.
            // +1 은 이전 메시지의 0번째 item은 실제 아이템이 아닌 날짜 경계선이기에 그 포지션을 뺀다.
            // +1 은 인덱스 0 - 사이즈는 1부터...
        } else {
            ColoredToast.showError(mContext, errMessage);
        }
    }

    /**
     * *********************************************************
     * Message List 업데이트
     * Message 리스트의 업데이트 획득 (from 서버)
     * **********************************************************
     */
    void getUpdateMessagesAndResumeUpdateTimer() {
        getUpdateMessagesInBackground(true);
    }

    void getUpdateMessagesWithoutResumingUpdateTimer() {
        getUpdateMessagesInBackground(false);
    }

    @Background
    public void getUpdateMessagesInBackground(boolean doWithResumingUpdateTimer) {
        try {
            if (messageState.getLastUpdateLinkId() >= 0) {
                ResUpdateMessages resUpdateMessages = messageManipulator.updateMessages(messageState.getLastUpdateLinkId());
                int nMessages = resUpdateMessages.updateInfo.messages.size();
                boolean isEmpty = true;
                log.info("getUpdateMessagesInBackground : " + nMessages
                        + " messages updated at ID, " + messageState.getLastUpdateLinkId());

                // 가장 최신의 LinkId를 업데이트한다.
                messageState.setLastUpdateLinkId(resUpdateMessages.lastLinkId);
                if (nMessages > 0) {
                    isEmpty = false;
                    // Update 된 메시지만 부분 삽입한다.
//                    mMessageItemConverter.updatedMessageItem(resUpdateMessages);
                    messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
                    setMarker();
                }
                getUpdateMessagesDone(isEmpty, doWithResumingUpdateTimer);
            } else {
                log.warn("getUpdateMessagesInBackground : LastUpdateLinkId = " + messageState.getLastUpdateLinkId());
            }
        } catch (JandiNetworkException e) {
            log.error("fail to get updated messages", e);
        } catch (Exception e) {
            log.error("fail to get updated messages", e);
        }

    }

    @UiThread
    public void getUpdateMessagesDone(boolean isEmpty, boolean doWithResumingUpdateTimer) {
        log.info("getUpdateMessagesDone : and resumeTimer? " + doWithResumingUpdateTimer);
        if (isEmpty) {
            // DO NOTHING
            return;
        }
        refreshListAdapter();
        if (doWithResumingUpdateTimer) {
            resumeUpdateTimer();
        }
    }

    /**
     * *********************************************************
     * Message 전송
     * **********************************************************
     */

    @Click(R.id.btn_send_message)
    void sendMessage() {
        if (buttonSendMessage.isSelected()) {
            pauseUpdateTimer();
            String message = etMessage.getText().toString();
            etMessage.setText("");
            if (message.length() > 0) {
                sendMessageInBackground(message);
            }
        }
    }

    @Background
    public void sendMessageInBackground(String message) {
        try {
            messageManipulator.sendMessage(message);
            log.debug("sendMessageInBackground : succeed");
            sendMessageSucceed();
        } catch (JandiNetworkException e) {
            log.error("sendMessageInBackground : FAILED", e);
            sendMessageFailed(R.string.err_messages_send);
        } catch (Exception e) {
            sendMessageFailed(R.string.err_messages_send);
        }
    }

    @UiThread
    public void sendMessageSucceed() {
        getUpdateMessagesAndResumeUpdateTimer();
    }

    @UiThread
    public void sendMessageFailed(int errMessageResId) {
        ColoredToast.showError(mContext, getString(errMessageResId));
        getUpdateMessagesAndResumeUpdateTimer();
    }


    /************************************************************
     * Message 제어
     ************************************************************/

    /**
     * Message Item의 Long Click 시, 수정/삭제 팝업 메뉴 활성화
     *
     * @param item
     */
    void messagesItemLongClicked(MessageItem item) {
        if (!item.isDateDivider) {
            checkPermissionForManipulatingMessage(item);
        }
    }

    void checkPermissionForManipulatingMessage(MessageItem item) {
        if (item.getContentType() == MessageItem.TYPE_IMAGE) {
            // 이미지 삭제 등의 액션은 나중에...
        } else if (item.getContentType() == MessageItem.TYPE_FILE) {
            // 파일 삭제 등의 액션은 나중에...
        } else {
            showDialog(item);
        }
    }

    @UiThread
    void showWarningCheckPermission(String message) {
        ColoredToast.showWarning(mContext, message);
    }

    void showDialog(MessageItem item) {
        android.support.v4.app.DialogFragment newFragment;
        if (mEntityManager.getMe().getUser().id == item.getUserId()) {
            // 내가 만든 메시지라면...
            newFragment = ManipulateMessageDialogFragment.newInstanceForMyMessage(item);
        } else {
            newFragment = ManipulateMessageDialogFragment.newInstance(item);
        }
        newFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    /**
     * *********************************************************
     * Message 복사
     * **********************************************************
     */
    public void onEvent(ConfirmCopyMessageEvent event) {
        final ClipboardManager clipboardManager
                = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipData clipData = ClipData.newPlainText("", event.contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    /**
     * *********************************************************
     * Message 삭제
     * **********************************************************
     */

    // 정말 삭제할 건지 다시 물어본다.
    public void onEvent(RequestDeleteMessageEvent event) {
        android.support.v4.app.DialogFragment newFragment = DeleteMessageDialogFragment.newInstance(event, false);
        newFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    // 삭제 확인
    public void onEvent(ConfirmDeleteMessageEvent event) {
        deleteMessage(event.messageType, event.messageId, event.feedbackId);
    }

    @UiThread
    void deleteMessage(int messageType, int messageId, int feedbackId) {
        pauseUpdateTimer();
        deleteMessageInBackground(messageType, messageId, feedbackId);
    }

    @Background
    void deleteMessageInBackground(int messageType, int messageId, int feedbackId) {
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageManipulator.deleteMessage(messageId);
                log.debug("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_COMMENT) {
                mJandiEntityClient.deleteMessageComment(messageId, feedbackId);
            }
            deleteMessageDone(true, null);
        } catch (JandiNetworkException e) {
            log.error("deleteMessageInBackground : FAILED", e);
            deleteMessageDone(false, getString(R.string.err_messages_delete));
        } catch (Exception e) {
            deleteMessageDone(false, getString(R.string.err_messages_delete));
        }
    }

    @UiThread
    void deleteMessageDone(boolean isOk, String message) {
        if (isOk) {
            getUpdateMessagesAndResumeUpdateTimer();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /**
     * *********************************************************
     * 파일 업로드
     * **********************************************************
     */
    @Click(R.id.btn_upload_file)
    void uploadFile() {
        android.support.v4.app.DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    public void onEvent(RequestFileUploadEvent event) {
        Intent intent;
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                log.info("RequestFileUploadEvent : from gallery");
                // Gallery
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                getPictureFromCamera();
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                log.info("RequestFileUploadEvent : from explorer");
                intent = new Intent(mContext, FileExplorerActivity.class);
                startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
                break;
            default:
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        log.debug("onActivityResult : " + requestCode + " / " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);


        String realFilePath;
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                if (resultCode == RESULT_OK) {
                    realFilePath = FileUploadUtil.getUploadFilePathFromActivityResult(mContext, requestCode, data, mImageUriFromCamera);
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_FILE_DETAIL_REFRESH:
                log.info("onActivityResult : Come from FileDetailActivity");
                // 파일 상세 Activity에서 넘어온 경우, 댓글이 달렸을 수도 있으니 바로 업데이트한다.
                getUpdateMessagesWithoutResumingUpdateTimer();
                break;
            default:
                break;
        }
    }

    // File Upload 대화상자 보여주기
    void showFileUploadDialog(String realFilePath) {
        // 업로드 파일 용량 체크
        final int MAX_FILE_SIZE = 100 * 1024 * 1024;    // 100MB
        File uploadFile = new File(realFilePath);
        if (uploadFile.exists() && uploadFile.length() > MAX_FILE_SIZE) {
            exceedMaxFileSizeError();
            return;
        }

        android.support.v4.app.DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath,
                mChattingInformations.entityId);
        newFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    // File Upload 확인 이벤트 획득
    public void onEvent(ConfirmFileUploadEvent event) {
        pauseUpdateTimer();

        FileUploadUtil.uploadStart(event, MessageListActivity.this, mChattingInformations, new FileUploadUtil.UploadCallback() {

            @Override
            public void onUploadFail() {
                uploadFileFailed();

            }

            @Override
            public void onUploadSuccess(JsonObject result) {
                uploadFileSucceed(result);

            }
        });
    }

    @UiThread
    void uploadFileSucceed(JsonObject result) {
        log.debug(result);
        trackUploadingFile(mEntityManager, mChattingInformations.entityType, result);
        ColoredToast.show(mContext, getString(R.string.jandi_file_upload_succeed));
        getUpdateMessagesAndResumeUpdateTimer();
    }

    @UiThread
    void uploadFileFailed() {
        ColoredToast.showError(mContext, getString(R.string.err_file_upload_failed));
        getUpdateMessagesAndResumeUpdateTimer();
    }

    @UiThread
    void exceedMaxFileSizeError() {
        ColoredToast.showError(mContext, getString(R.string.err_file_upload_failed));
        getUpdateMessagesAndResumeUpdateTimer();
    }

    // 카메라에서 가져오기
    public void getPictureFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageUriFromCamera = FileUploadUtil.createCacheFile(this);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUriFromCamera);
        startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    /**
     * *********************************************************
     * 파일 상세
     * **********************************************************
     */
    void messagesItemClicked(MessageItem item) {
        if (!item.isDateDivider) {
            switch (item.getContentType()) {
                case MessageItem.TYPE_STRING:
                    break;
                case MessageItem.TYPE_COMMENT:
                    moveToFileDetailActivity(item.getFeedbackId());
                    break;
                case MessageItem.TYPE_IMAGE:
                case MessageItem.TYPE_FILE:
                    moveToFileDetailActivity(item.getMessageId());
                    break;

            }
        }
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    /**
     * 수정 이벤트 획득 from EditTextDialogFragment
     */
    public void onEvent(ConfirmModifyTopicEvent event) {
        modifyEntityInBackground(event);
    }

    @Background
    void modifyEntityInBackground(ConfirmModifyTopicEvent event) {
        try {
            if (mChattingInformations.isPublicTopic()) {
                mJandiEntityClient.modifyChannelName(mChattingInformations.entityId, event.inputName);
            } else if (mChattingInformations.isPrivateTopic()) {
                mJandiEntityClient.modifyPrivateGroupName(mChattingInformations.entityId, event.inputName);
            }
            modifyEntitySucceed(event.inputName);
        } catch (JandiNetworkException e) {
            log.error("modify failed " + e.getErrorInfo(), e);
            if (e.errCode == JandiNetworkException.DUPLICATED_NAME) {
                modifyEntityFailed(getString(R.string.err_entity_duplicated_name));
            } else {
                modifyEntityFailed(getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            modifyEntityFailed(getString(R.string.err_entity_modify));
        }
    }

    @UiThread
    void modifyEntitySucceed(String changedEntityName) {
        trackChangingEntityName(mEntityManager, mChattingInformations.entityType);
        mChattingInformations.entityName = changedEntityName;
        getSupportActionBar().setTitle(changedEntityName);
    }

    @UiThread
    void modifyEntityFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
    }

    /**
     * *********************************************************
     * Topic 삭제
     * **********************************************************
     */
    @SupposeUiThread
    void requestToDeleteTopic() {
        android.support.v4.app.DialogFragment newFragment = DeleteTopicDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void onEvent(ConfirmDeleteTopicEvent event) {
        deleteTopicInBackground();
    }

    @Background
    void deleteTopicInBackground() {
        try {
            if (mChattingInformations.isPublicTopic()) {
                mJandiEntityClient.deleteChannel(mChattingInformations.entityId);
            } else if (mChattingInformations.isPrivateTopic()) {
                mJandiEntityClient.deletePrivateGroup(mChattingInformations.entityId);
            }
            deleteTopicSucceed();
        } catch (JandiNetworkException e) {
            deleteTopicFailed(getString(R.string.err_entity_delete));
        } catch (Exception e) {
            deleteTopicFailed(getString(R.string.err_entity_delete));
        }
    }

    @UiThread
    public void deleteTopicSucceed() {
        log.debug("delete success");
        trackDeletingEntity(mEntityManager, mChattingInformations.entityType);
        finish();
    }

    @UiThread
    public void deleteTopicFailed(String errMessage) {
        log.error("delete failed");
        ColoredToast.showError(mContext, errMessage);
    }

    /**
     * *********************************************************
     * Set Marker
     * **********************************************************
     */
    @Background
    public void setMarker() {
        try {
            if (messageState.getLastUpdateLinkId() > 0) {
                messageManipulator.setMarker(messageState.getLastUpdateLinkId());
            }
        } catch (JandiNetworkException e) {
            log.error("set marker failed", e);
        } catch (Exception e) {
            log.error("set marker failed", e);
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필 보기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    public void onEvent(RequestUserInfoEvent event) {
        int userEntityId = event.userId;
        getProfileInBackground(userEntityId);
    }

    @Background
    void getProfileInBackground(int userEntityId) {
        try {
            ResLeftSideMenu.User user = mJandiEntityClient.getUserProfile(userEntityId);
            getProfileSuccess(user);
        } catch (JandiNetworkException e) {
            log.error("get profile failed", e);
            getProfileFailed();
        } catch (Exception e) {
            log.error("get profile failed", e);
            getProfileFailed();
        }
    }

    @UiThread
    void getProfileSuccess(ResLeftSideMenu.User user) {
        showUserInfoDialog(new FormattedEntity(user));
    }

    @UiThread
    void getProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    private void showUserInfoDialog(FormattedEntity user) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        UserInfoDialogFragment_.builder().entityId(user.getId()).build().show(getSupportFragmentManager(), "dialog");
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        changeForDirectMessage(event.userId);
    }

    void changeForDirectMessage(final int userId) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListV2Activity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                        .entityId(userId)
                        .isFavorite(mEntityManager.getEntityById(userId).isStarred)
                        .isFromPush(isFromPush)
                        .start();
            }
        }, 250);
    }

    /**
     * Polling task
     * Timer는 OnResume, OnPause 의 생명주기와 함께함
     */
    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            getUpdateMessagesWithoutResumingUpdateTimer();
        }
    }
}
