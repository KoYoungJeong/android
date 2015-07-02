package com.tosslab.jandi.app.ui.message.v2;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog_;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListPresenter {
    @ViewById(R.id.list_messages)
    RecyclerView messageListView;

    @ViewById(R.id.btn_send_message)
    Button sendButton;

    @ViewById(R.id.et_message)
    EditText messageEditText;

    @RootContext
    AppCompatActivity activity;

    @SystemService
    ClipboardManager clipboardManager;

    @SystemService
    InputMethodManager inputMethodManager;

    @ViewById(R.id.layout_messages_preview_last_item)
    View previewLayout;

    @ViewById(R.id.img_message_preview_user_profile)
    ImageView previewProfileView;

    @ViewById(R.id.txt_message_preview_user_name)
    TextView previewNameView;

    @ViewById(R.id.txt_message_preview_content)
    TextView previewContent;

    @ViewById(R.id.ll_messages)
    View sendLayout;

    @ViewById(R.id.ll_messages_go_to_latest)
    View moveRealChatView;

    @ViewById(R.id.ll_messages_disable_alert)
    View disabledUser;

    @ViewById(R.id.layout_messages_empty)
    LinearLayout emptyMessageView;

    @ViewById(R.id.layout_messages_loading)
    View loadingMessageView;

    @ViewById(R.id.img_go_to_latest)
    View arrowGoToLatestView;

    @ViewById(R.id.progress_go_to_latest)
    View progressGoToLatestView;

    @ViewById(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;

    @ViewById(R.id.iv_messages_preview_sticker_image)
    ImageView imgStickerPreview;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    private MessageListAdapter messageListAdapter;
    private ProgressWheel progressWheel;
    private String tempMessage;
    private boolean isDisabled;
    private boolean sendLayoutVisible;
    private boolean gotoLatestLayoutVisible;
    private EntityManager mEntityManager;
    private String invitationUrl;
    private String teamName;

    @AfterInject
    void initObject() {
        messageListAdapter = new MessageListAdapter(activity);
        messageListAdapter.setHasStableIds(true);
        messageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                int itemCountWithoutEvent = getItemCountWithoutEvent();
                if (itemCountWithoutEvent > 0) {
                    emptyMessageView.setVisibility(View.GONE);
                } else {

                    if (loadingMessageView.getVisibility() != View.VISIBLE) {
                        emptyMessageView.setVisibility(View.VISIBLE);
                    } else {
                        emptyMessageView.setVisibility(View.GONE);
                    }
                }
            }
        });

        mEntityManager = EntityManager.getInstance(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @AfterViews
    void initViews() {

        messageListView.setAdapter(messageListAdapter);
        messageListView.setItemAnimator(null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        messageListView.setLayoutManager(layoutManager);

        MessageListHeaderAdapter messageListHeaderAdapter = new MessageListHeaderAdapter(activity, messageListAdapter);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(messageListAdapter)
                .setRecyclerView(messageListView)
                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
                .build();
        messageListView.addItemDecoration(stickyHeadersItemDecoration);


        setSendEditText(tempMessage);

        if (isDisabled) {
            sendLayoutVisibleGone();
            disabledUser.setVisibility(View.VISIBLE);
            setPreviewVisibleGone();
        }

        if (sendLayoutVisible) {
            sendLayoutVisibleGone();
        }

        if (gotoLatestLayoutVisible) {
            setGotoLatestLayoutVisible();
        }
    }

    public void sendLayoutVisibleGone() {
        sendLayoutVisible = true;
        if (sendLayout != null) {
            sendLayout.setVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void addAll(int position, List<ResMessages.Link> messages) {
        messageListAdapter.addAll(position, messages);
        messageListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void setEnableSendButton(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    public int getLastItemPosition() {
        return messageListAdapter.getCount();
    }

    @UiThread
    public void moveLastPage() {
        if (messageListView != null) {
            messageListView.getLayoutManager().scrollToPosition(messageListAdapter.getCount() - 1);
        }
    }

    public void setOldLoadingComplete() {
        messageListAdapter.setOldLoadingComplete();
    }

    public void setOldNoMoreLoading() {
        messageListAdapter.setOldNoMoreLoading();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessage(int linkId, int firstVisibleItemTop) {
        int itemPosition = messageListAdapter.getItemPositionByMessageId(linkId);
        ((LinearLayoutManager) messageListView.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessageById(int id, int firstVisibleItemTop) {
        int itemPosition = messageListAdapter.getItemPositionById(id);
        ((LinearLayoutManager) messageListView.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    public int getFirstVisibleItemLinkId() {
        if (messageListAdapter.getCount() > 0) {
            int firstVisibleItemPosition = ((LinearLayoutManager) messageListView.getLayoutManager()).findFirstVisibleItemPosition();
            if (firstVisibleItemPosition >= 0) {
                return messageListAdapter.getItem(firstVisibleItemPosition).messageId;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public int getFirstVisibleItemTop() {
        View childAt = messageListView.getLayoutManager().getChildAt(0);
        if (childAt != null) {
            return childAt.getTop();
        } else {
            return 0;
        }
    }

    @UiThread
    public void showNoMoreMessage() {
        ColoredToast.showWarning(activity, activity.getString(R.string.warn_no_more_messages));
    }

    public void moveFileDetailActivity(Fragment fragment, int messageId) {
        FileDetailActivity_
                .intent(fragment)
                .fileId(messageId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    public ResMessages.Link getItem(int position) {
        return messageListAdapter.getItem(position);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
    }

    public void exceedMaxFileSizeError() {

        ColoredToast.showError(activity, activity.getString(R.string.jandi_file_size_large_error));
    }

    public String getSendEditText() {
        return messageEditText.getText().toString();
    }


    public void setSendEditText(String text) {
        tempMessage = text;
        if (messageEditText != null) {
            messageEditText.setText(tempMessage);
        }
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(activity, message);
    }

    public void showMessageMenuDialog(boolean myMessage, ResMessages.TextMessage textMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage
                (textMessage, myMessage);
        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
    }

    public void showStickerMessageMenuDialog(
            boolean myMessage, ResMessages.StickerMessage StickerMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByStickerMessage
                (StickerMessage, myMessage);
        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
    }

    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage
                (commentMessage, false);
        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
    }

    public ProgressDialog getUploadProgress(ConfirmFileUploadEvent event) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(activity.getString(R.string.jandi_file_uploading) + " " + event.realFilePath);
        progressDialog.show();

        return progressDialog;

    }

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(activity, message);
    }

    @UiThread
    public void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    @UiThread
    public void clearMessages() {
        messageListAdapter.clear();

    }

    public List<ResMessages.Link> getLastItemsWithoutDummy() {
        int count = messageListAdapter.getCount();
        int lastIdx = Math.max(count - 20, 0);
        List<ResMessages.Link> lastItems = new ArrayList<ResMessages.Link>();
        for (int idx = count - 1; idx >= lastIdx; --idx) {
            if (messageListAdapter.getItem(idx) instanceof DummyMessageLink) {
                continue;
            }
            lastItems.add(messageListAdapter.getItem(idx));
        }

        return lastItems;
    }

    @UiThread
    public void finish() {
        activity.finish();
    }

    public void copyToClipboard(String contentString) {
        final ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void changeToArchive(int messageId) {
        int position = messageListAdapter.indexByMessageId(messageId);
        String archivedStatus = "archived";
        if (position > 0) {
            ResMessages.Link item = messageListAdapter.getItem(position);
            item.message.status = archivedStatus;
            item.message.createTime = new Date();
        }

        List<Integer> commentIndexes = messageListAdapter.indexByFeedbackId(messageId);

        for (Integer commentIndex : commentIndexes) {
            ResMessages.Link item = messageListAdapter.getItem(commentIndex);
            item.feedback.status = archivedStatus;
            item.feedback.createTime = new Date();
        }

        if (position > 0 || commentIndexes.size() > 0) {

            messageListAdapter.notifyDataSetChanged();
        }

    }

    public void updateMessageIdAtSendingMessage(long localId, int messageId) {
        if (!hasMessage(messageId)) {
            messageListAdapter.updateMessageId(localId, messageId);
        } else {
            deleteLocalMessage(localId);
        }
    }

    private void deleteLocalMessage(long localId) {
        deleteDummyMessageAtList(localId);
    }

    private boolean hasMessage(int messageId) {
        int idx = messageListAdapter.indexByMessageId(messageId);
        return idx >= 0;
    }

    public void insertSendingMessage(long localId, String message, String name, String userLargeProfileUrl) {
        DummyMessageLink dummyMessageLink = new DummyMessageLink(localId, message, SendingState.Sending);
        dummyMessageLink.message.writerId = EntityManager.getInstance(activity).getMe().getId();
        dummyMessageLink.message.createTime = new Date();
        dummyMessageLink.message.updateTime = new Date();

        messageListAdapter.addDummyMessage(dummyMessageLink);
        messageListAdapter.notifyDataSetChanged();

        messageListView.getLayoutManager().scrollToPosition(messageListAdapter.getItemCount() - 1);
    }

    public void updateDummyMessageState(long localId, SendingState state) {
        messageListAdapter.updateDummyMessageState(localId, state);
        justRefresh();
    }

    @UiThread
    public void addDummyMessages(List<ResMessages.Link> dummyMessages) {
        for (ResMessages.Link dummyMessage : dummyMessages) {
            messageListAdapter.addDummyMessage(((DummyMessageLink) dummyMessage));
        }
        messageListAdapter.notifyDataSetChanged();
    }

    public void showDummyMessageDialog(long localId) {
        DummyMessageDialog_.builder()
                .localId(localId)
                .build()
                .show(activity.getFragmentManager(), "dialog");
    }

    public DummyMessageLink getDummyMessage(long localId) {
        int position = messageListAdapter.getDummeMessagePositionByLocalId(localId);
        return ((DummyMessageLink) messageListAdapter.getItem(position));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void deleteDummyMessageAtList(long localId) {
        int position = messageListAdapter.getDummeMessagePositionByLocalId(localId);
        messageListAdapter.remove(position);
        messageListAdapter.notifyDataSetChanged();
    }

    private boolean isVisibleLastItem() {
        return ((LinearLayoutManager) messageListView.getLayoutManager()).findFirstVisibleItemPosition() == messageListAdapter.getCount() - 1;
    }

    @UiThread
    public void showPreviewIfNotLastItem() {

        if (isVisibleLastItem()) {
            return;
        }

        ResMessages.Link item = messageListAdapter.getItem(messageListAdapter.getCount() - 1);

        if (TextUtils.equals(item.status, "event")) {
            return;
        }

        FormattedEntity entityById = EntityManager.getInstance(activity).getEntityById(item.message.writerId);
        previewNameView.setText(entityById.getName());

        String url = entityById.getUser().u_photoThumbnailUrl != null && !(TextUtils.isEmpty(entityById.getUser().u_photoThumbnailUrl.smallThumbnailUrl)) ? entityById.getUser().u_photoThumbnailUrl.smallThumbnailUrl : entityById.getUser().u_photoUrl;
        Ion.with(previewProfileView)
                .transform(new IonCircleTransform())
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + url);

        if (item.message instanceof ResMessages.FileMessage) {
            previewContent.setText(((ResMessages.FileMessage) item.message).content.title);
        } else if (item.message instanceof ResMessages.CommentMessage) {
            previewContent.setText(((ResMessages.CommentMessage) item.message).content.body);
        } else if (item.message instanceof ResMessages.TextMessage) {
            previewContent.setText(((ResMessages.TextMessage) item.message).content.body);
        }

        previewLayout.setVisibility(View.VISIBLE);
    }

    public void setPreviewVisibleGone() {
        if (previewLayout != null) {
            previewLayout.setVisibility(View.GONE);
        }
    }

    public void disableChat() {
        isDisabled = true;
    }

    @UiThread
    public void showMessageLoading() {
        loadingMessageView.setVisibility(View.VISIBLE);
        emptyMessageView.setVisibility(View.GONE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissLoadingView() {
        loadingMessageView.setVisibility(View.GONE);
    }

    public void setMarker(int lastMarker) {
        if (messageListAdapter != null) {
            messageListAdapter.setMarker(lastMarker);
        }
    }


    public void setMoreNewFromAdapter(boolean isMoreNew) {
        messageListAdapter.setMoreFromNew(isMoreNew);
    }

    public void setNewLoadingComplete() {
        messageListAdapter.setNewLoadingComplete();
    }

    public void setNewNoMoreLoading() {
        messageListAdapter.setNewNoMoreLoading();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void addAndMove(List<ResMessages.Link> records) {
        int firstVisibleItemLinkId = getFirstVisibleItemLinkId();
        int firstVisibleItemTop = getFirstVisibleItemTop();
        int lastItemPosition = getLastItemPosition();

        if (lastItemPosition > 0) {
            messageListView.getLayoutManager().scrollToPosition(lastItemPosition - 1);
        }
        addAll(lastItemPosition, records);
        if (firstVisibleItemLinkId > 0) {
            moveToMessage(firstVisibleItemLinkId, firstVisibleItemTop);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setGotoLatestLayoutVisible() {
        gotoLatestLayoutVisible = true;
        if (moveRealChatView != null) {
            moveRealChatView.setVisibility(View.VISIBLE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setGotoLatestLayoutVisibleGone() {
        gotoLatestLayoutVisible = false;
        if (moveRealChatView != null) {
            moveRealChatView.setVisibility(View.GONE);
        }
    }


    public void setOnItemClickListener(MessageListAdapter.OnItemClickListener onItemClickListener) {
        messageListAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void setOnItemLongClickListener(MessageListAdapter.OnItemLongClickListener onItemLongClickListener) {
        messageListAdapter.setOnItemLongClickListener(onItemLongClickListener);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setGotoLatestLayoutShowProgress() {
        arrowGoToLatestView.setVisibility(View.GONE);
        progressGoToLatestView.setVisibility(View.VISIBLE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showEmptyView() {
        emptyMessageView.setVisibility(View.VISIBLE);
    }

    public int getLastVisibleItemPosition() {
        return ((LinearLayoutManager) messageListView.getLayoutManager()).findLastVisibleItemPosition();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void justRefresh() {
        messageListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void setMarkerInfo(int teamId, int roomId) {
        messageListAdapter.setTeamId(teamId);
        messageListAdapter.setRoomId(roomId);
        messageListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void insertMessageEmptyLayout() {

        if (emptyMessageView == null) {
            return;
        }
        emptyMessageView.removeAllViews();

        LayoutInflater.from(activity).inflate(R.layout.view_message_list_empty, emptyMessageView, true);
    }

    @UiThread
    public void insertTeamMemberEmptyLayout() {

        if (emptyMessageView == null) {
            return;
        }
        emptyMessageView.removeAllViews();
        View view = LayoutInflater.from(activity).inflate(R.layout.view_team_member_empty, emptyMessageView, true);
        View.OnClickListener onClickListener = v -> invitationDialogExecutor.execute();
        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(onClickListener);
        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(onClickListener);
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(activity).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    @UiThread
    public void insertTopicMemberEmptyLayout() {

        if (emptyMessageView == null) {
            return;
        }

        emptyMessageView.removeAllViews();
        View view = LayoutInflater.from(activity).inflate(R.layout.view_topic_member_empty, emptyMessageView, true);
        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));
        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));

    }

    public int getItemCountWithoutEvent() {

        int itemCount = messageListAdapter.getItemCount();
        for (int idx = itemCount - 1; idx >= 0; --idx) {
            if (messageListAdapter.getItemViewType(idx) == BodyViewHolder.Type.Event.ordinal()) {
                itemCount--;
            }
        }

        return itemCount;
    }

    @UiThread
    public void dismissEmptyView() {
        emptyMessageView.setVisibility(View.GONE);
    }

    @UiThread
    public void checkItemCountIfException() {
        boolean hasItem = getFirstVisibleItemLinkId() > 0;
        dismissLoadingView();
        if (!hasItem) {
            showEmptyView();
        } else {
            dismissEmptyView();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void clearEmptyMessageLayout() {
        if (emptyMessageView != null) {
            emptyMessageView.removeAllViews();
        }
    }

    public int getItemCount() {
        return messageListAdapter.getItemCount();
    }

    public int getRoomId() {
        return messageListAdapter.getRoomId();
    }

    public EditText getSendEditTextView() {
        return messageEditText;
    }

    public void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
    }

    public void showKeyboard() {
        inputMethodManager.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void showStickerPreview(StickerInfo stickerInfo) {

        vgStickerPreview.setVisibility(View.VISIBLE);
    }

    public void loadSticker(StickerInfo stickerInfo) {
        StickerManager.getInstance().loadStickerDefaultOption(imgStickerPreview, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
    }

    public void dismissStickerPreview() {

        vgStickerPreview.setVisibility(View.GONE);

    }
}
