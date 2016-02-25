//package com.tosslab.jandi.app.ui.message.v2;
//
//import android.content.ClipData;
//import android.content.ClipboardManager;
//import android.graphics.Color;
//import android.net.Uri;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.SpannableStringBuilder;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.facebook.drawee.drawable.ScalingUtils;
//import com.facebook.drawee.generic.RoundingParams;
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.tosslab.jandi.app.JandiConstants;
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
//import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
//import com.tosslab.jandi.app.lists.FormattedEntity;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.markdown.MarkdownLookUp;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
//import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
//import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
//import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
//import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
//import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
//import com.tosslab.jandi.app.ui.message.to.StickerInfo;
//import com.tosslab.jandi.app.ui.message.v2.adapter.MessageAdapter;
//import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
//import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListSearchAdapter;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
//import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog_;
//import com.tosslab.jandi.app.ui.offline.OfflineLayer;
//import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
//import com.tosslab.jandi.app.utils.AlertUtil;
//import com.tosslab.jandi.app.utils.ColoredToast;
//import com.tosslab.jandi.app.utils.ProgressWheel;
//import com.tosslab.jandi.app.utils.image.ImageUtil;
//import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
//import com.tosslab.jandi.app.utils.transform.TransformConfig;
//import com.tosslab.jandi.app.views.spannable.JandiURLSpan;
//
//import org.androidannotations.annotations.AfterInject;
//import org.androidannotations.annotations.AfterViews;
//import org.androidannotations.annotations.Bean;
//import org.androidannotations.annotations.EBean;
//import org.androidannotations.annotations.RootContext;
//import org.androidannotations.annotations.SystemService;
//import org.androidannotations.annotations.UiThread;
//import org.androidannotations.annotations.ViewById;
//
//import java.util.Date;
//import java.util.List;
//
//import de.greenrobot.event.EventBus;
//import rx.Observable;
//
///**
// * Created by Steve SeongUg Jung on 15. 1. 20..
// */
//@EBean
//public class MessageListPresenter {
//    @ViewById(R.id.lv_messages)
//    RecyclerView lvMessages;
//
//    @ViewById(R.id.btn_send_message)
//    View sendButton;
//
//    @ViewById(R.id.et_message)
//    EditText etMessage;
//
//    @RootContext
//    AppCompatActivity activity;
//
//    @SystemService
//    ClipboardManager clipboardManager;
//
//    @SystemService
//    InputMethodManager inputMethodManager;
//
//    @ViewById(R.id.vg_messages_preview_last_item)
//    View vgPreview;
//
//    @ViewById(R.id.iv_message_preview_user_profile)
//    SimpleDraweeView ivPreviewProfile;
//
//    @ViewById(R.id.tv_message_preview_user_name)
//    TextView tvPreviewUserName;
//
//    @ViewById(R.id.tv_message_preview_content)
//    TextView tvPreviewContent;
//
//    @ViewById(R.id.vg_messages_input)
//    View vgMessageInput;
//
//    @ViewById(R.id.vg_messages_go_to_latest)
//    View vgMoveToLatest;
//
//    @ViewById(R.id.vg_messages_disable_alert)
//    View vDisabledUser;
//
//    @ViewById(R.id.layout_messages_empty)
//    LinearLayout layoutEmpty;
//
//    @ViewById(R.id.layout_messages_loading)
//    View vgProgressForMessageList;
//
//    @ViewById(R.id.img_go_to_latest)
//    View vMoveToLatest;
//
//    @ViewById(R.id.progress_go_to_latest)
//    View progressGoToLatestView;
//
//    @ViewById(R.id.vg_messages_preview_sticker)
//    ViewGroup vgStickerPreview;
//
//    @ViewById(R.id.iv_messages_preview_sticker_image)
//    SimpleDraweeView ivSticker;
//
//    @ViewById(R.id.vg_message_offline)
//    View vgOffline;
//
//    @ViewById(R.id.progress_message)
//    View oldProgressBar;
//
//    @Bean
//    InvitationDialogExecutor invitationDialogExecutor;
//
//    @Bean
//    TeamDomainInfoModel teamDomainInfoModel;
//
//    @Bean
//    KeyboardHeightModel keyboardHeightModel;
//
//    private ProgressWheel progressWheelForAction;
//    private String tempMessage;
//    private boolean isDisabled;
//    private boolean sendLayoutVisible;
//    private boolean gotoLatestLayoutVisible;
//    private OfflineLayer offlineLayer;
//    private View.OnTouchListener listTouchListener;
//
//    private MessageAdapter messageAdapter;
//
//    @AfterInject
//    void initObject() {
//        progressWheelForAction = new ProgressWheel(activity);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void setEmptyViewIfNeed() {
//        int originItemCount = getItemCount();
//        int itemCountWithoutEvent = getItemCountWithoutEvent();
//        int eventCount = originItemCount - itemCountWithoutEvent;
//        if (itemCountWithoutEvent > 0 || eventCount > 1) {
//            // create 이벤트외에 다른 이벤트가 생성된 경우
//            layoutEmpty.setVisibility(View.GONE);
//        } else {
//            // 아예 메세지가 없거나 create 이벤트 외에는 생성된 이벤트가 없는 경우
//            layoutEmpty.setVisibility(View.VISIBLE);
//        }
//    }

//    @AfterViews
//    void initViews() {

// 프로그레스를 좀 더 부드럽게 보여주기 위해서 애니메이션
//        vgProgressForMessageList.setAlpha(0f);
//        vgProgressForMessageList.animate()
//                .alpha(1.0f)
//                .setDuration(150);
//
//        lvMessages.setAdapter(messageAdapter);
//        lvMessages.setItemAnimator(null);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
//        layoutManager.setStackFromEnd(true);
//        layoutManager.setSmoothScrollbarEnabled(true);
//        lvMessages.setLayoutManager(layoutManager);
//
//        MessageListHeaderAdapter messageListHeaderAdapter = new MessageListHeaderAdapter(activity, messageAdapter);
//
//        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
//                .setAdapter(messageAdapter)
//                .setRecyclerView(lvMessages)
//                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
//                .build();
//
//        lvMessages.addItemDecoration(stickyHeadersItemDecoration);

//        setListTouchListener(listTouchListener);

//        if (isDisabled) {
//            setDisableUser();
//        }

//        if (sendLayoutVisible) {
//            sendLayoutVisibleGone();
//        }
//
//        if (gotoLatestLayoutVisible) {
//            setGotoLatestLayoutVisible();
//        }

//        offlineLayer = new OfflineLayer(vgOffline);
//
//        if (!NetworkCheckUtil.isConnected()) {
//            offlineLayer.showOfflineView();
//        }
//
//    }

//    public void sendLayoutVisibleGone() {
//        sendLayoutVisible = true;
//        if (vgMessageInput != null) {
//            vgMessageInput.setVisibility(View.GONE);
//        }
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void addAll(int position, List<ResMessages.Link> messages) {
//        messageAdapter.addAll(position, messages);
//        messageAdapter.notifyDataSetChanged();
//    }

//    @UiThread
//    public void showProgressWheel() {
//        if (progressWheelForAction != null && progressWheelForAction.isShowing()) {
//            progressWheelForAction.dismiss();
//        }
//
//        if (progressWheelForAction != null) {
//            progressWheelForAction.show();
//        }
//    }

//    @UiThread
//    public void dismissProgressWheel() {
//        if (progressWheelForAction != null && progressWheelForAction.isShowing()) {
//            progressWheelForAction.dismiss();
//        }
//    }

//    public void setEnableSendButton(boolean enabled) {
//        sendButton.setEnabled(enabled);
//    }
//
//    public int getLastItemPosition() {
//        return messageAdapter.getItemCount();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void moveLastPage() {
//        if (lvMessages != null) {
//            lvMessages.getLayoutManager().scrollToPosition(messageAdapter.getItemCount() - 1);
//        }
//    }

//    public void setOldLoadingComplete() {
//        messageAdapter.setOldLoadingComplete();
//    }
//
//    public void setOldNoMoreLoading() {
//        messageAdapter.setOldNoMoreLoading();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void moveToMessage(long messageId, int firstVisibleItemTop) {
//        int itemPosition = messageAdapter.indexByMessageId(messageId);
//        ((LinearLayoutManager) lvMessages.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
//    }
//
//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void moveToMessageById(long linkId, int firstVisibleItemTop) {
//        int itemPosition = messageAdapter.indexOfLinkId(linkId);
//        ((LinearLayoutManager) lvMessages.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
//    }

//    public long getFirstVisibleItemLinkId() {
//        if (messageAdapter.getItemCount() > 0) {
//            int firstVisibleItemPosition = ((LinearLayoutManager) lvMessages.getLayoutManager()).findFirstVisibleItemPosition();
//            if (firstVisibleItemPosition >= 0) {
//                return messageAdapter.getItem(firstVisibleItemPosition).messageId;
//            } else {
//                return -1;
//            }
//        } else {
//            return -1;
//        }
//    }
//
//    public int getFirstVisibleItemTop() {
//        View childAt = lvMessages.getLayoutManager().getChildAt(0);
//        if (childAt != null) {
//            return childAt.getTop();
//        } else {
//            return 0;
//        }
//    }

//    @UiThread
//    public void showNoMoreMessage() {
//        ColoredToast.showWarning(activity.getString(R.string.warn_no_more_messages));
//    }

//    public void moveFileDetailActivity(Fragment fragment, long messageId, long roomId, long selectMessageId) {
//        FileDetailActivity_
//                .intent(fragment)
//                .fileId(messageId)
//                .roomId(roomId)
//                .selectMessageId(selectMessageId)
//                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
//        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
//    }

//    public ResMessages.Link getItem(int position) {
//        return messageAdapter.getItem(position);
//    }

//    public String getSendEditText() {
//        return etMessage.getText().toString();
//    }
//
//    public void setSendEditText(String text) {
//        tempMessage = text;
//        if (etMessage != null) {
//            etMessage.setText(tempMessage);
//        }
//    }

//    @UiThread
//    public void showFailToast(String message) {
//        ColoredToast.showError(message);
//    }

//    public void showMessageMenuDialog(boolean isDirectMessage, boolean myMessage,
//                                      ResMessages.TextMessage textMessage) {
//        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage(
//                textMessage, myMessage, isDirectMessage);
//        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
//    }
//
//    public void showStickerMessageMenuDialog(
//            boolean myMessage, ResMessages.StickerMessage StickerMessage) {
//        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByStickerMessage
//                (StickerMessage, myMessage);
//        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
//    }
//
//    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
//        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage
//                (commentMessage, false);
//        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
//    }

//    @UiThread
//    public void showSuccessToast(String message) {
//        ColoredToast.show(message);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void clearMessages() {
//        messageAdapter.clear();
//    }

//    public ResMessages.Link getLastItemWithoutDummy() {
//        int count = messageAdapter.getItemCount();
//        for (int idx = count - 1; idx >= 0; --idx) {
//            if (messageAdapter.getItem(idx) instanceof DummyMessageLink) {
//                continue;
//            }
//            return messageAdapter.getItem(idx);
//        }
//
//        return null;
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void finish() {
//        activity.finish();
//    }

//    public void copyToClipboard(String contentString) {
//        final ClipData clipData = ClipData.newPlainText("", contentString);
//        clipboardManager.setPrimaryClip(clipData);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void changeToArchive(long messageId) {
//        int position = messageAdapter.indexByMessageId(messageId);
//        String archivedStatus = "archived";
//        if (position > 0) {
//            ResMessages.Link item = messageAdapter.getItem(position);
//            item.message.status = archivedStatus;
//            item.message.createTime = new Date();
//
//        }
//
//        List<Integer> commentIndexes = messageAdapter.indexByFeedbackId(messageId);
//
//        for (Integer commentIndex : commentIndexes) {
//            ResMessages.Link item = messageAdapter.getItem(commentIndex);
//            item.feedback.status = archivedStatus;
//            item.feedback.createTime = new Date();
//        }
//
//        if (position >= 0 || commentIndexes.size() > 0) {
//
//            messageAdapter.notifyItemRangeChanged(0, messageAdapter.getItemCount());
//        }
//    }

//    public void updateLinkPreviewMessage(ResMessages.TextMessage message) {
//        long messageId = message.id;
//        int index = messageAdapter.indexByMessageId(messageId);
//        if (index < 0) {
//            return;
//        }
//
//        ResMessages.Link link = getItem(index);
//        if (!(link.message instanceof ResMessages.TextMessage)) {
//            return;
//        }
//        link.message = message;
//    }

//    public void showDummyMessageDialog(long localId) {
//        DummyMessageDialog_.builder()
//                .localId(localId)
//                .build()
//                .show(activity.getFragmentManager(), "dialog");
//    }

//    public DummyMessageLink getDummyMessage(long localId) {
//        int position = messageAdapter.getDummeMessagePositionByLocalId(localId);
//        return ((DummyMessageLink) messageAdapter.getItem(position));
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void refreshAll() {
//        messageAdapter.notifyDataSetChanged();
//    }

//    private boolean isVisibleLastItem() {
//        return ((LinearLayoutManager) lvMessages.getLayoutManager())
//                .findFirstVisibleItemPosition() == messageAdapter.getItemCount() - 1;
//    }

//    @UiThread
//    public void showPreviewIfNotLastItem() {
//
//        if (isVisibleLastItem()) {
//            return;
//        }
//
//        ResMessages.Link item = messageAdapter.getItem(messageAdapter.getItemCount() - 1);
//
//        if (TextUtils.equals(item.status, "event")) {
//            return;
//        }
//
//        FormattedEntity entity = EntityManager.getInstance().getEntityById(item.message.writerId);
//        tvPreviewUserName.setText(entity.getName());
//
//        String url = ImageUtil.getImageFileUrl(entity.getUserSmallProfileUrl());
//        Uri uri = Uri.parse(url);
//        if (!EntityManager.getInstance().isBot(entity.getId())) {
//            ImageUtil.loadProfileImage(ivPreviewProfile, uri, R.drawable.profile_img);
//        } else {
//            RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
//                    TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);
//
//            ImageLoader.newBuilder()
//                    .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
//                    .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
//                    .backgroundColor(Color.WHITE)
//                    .roundingParams(circleRoundingParams)
//                    .load(uri)
//                    .into(ivPreviewProfile);
//
//        }
//
//        String message;
//        if (item.message instanceof ResMessages.FileMessage) {
//            message = ((ResMessages.FileMessage) item.message).content.title;
//        } else if (item.message instanceof ResMessages.CommentMessage) {
//            message = ((ResMessages.CommentMessage) item.message).content.body;
//        } else if (item.message instanceof ResMessages.TextMessage) {
//            message = ((ResMessages.TextMessage) item.message).content.body;
//        } else if (item.message instanceof ResMessages.StickerMessage || item.message instanceof
//                ResMessages.CommentStickerMessage) {
//            message = String.format("(%s)", activity.getString(R.string.jandi_coach_mark_stickers));
//        } else {
//            message = "";
//        }
//        SpannableStringBuilder builder = new SpannableStringBuilder(TextUtils.isEmpty(message) ? "" : message);
//
//        MarkdownLookUp.text(builder)
//                .plainText(true)
//                .lookUp(tvPreviewContent.getContext());
//        new MarkdownViewModel(builder, true).execute();
//
//        builder.removeSpan(JandiURLSpan.class);
//        tvPreviewContent.setText(builder);
//
//        vgPreview.setVisibility(View.VISIBLE);
//    }

//    public void setPreviewVisibleGone() {
//        if (vgPreview != null) {
//            vgPreview.setVisibility(View.GONE);
//        }
//    }

//    public void disableChat() {
//        isDisabled = true;
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void dismissLoadingView() {
//        vgProgressForMessageList.animate()
//                .alpha(0f)
//                .setDuration(250);
//    }

//    public void setMarker(int lastMarker) {
//        if (messageAdapter != null) {
//            messageAdapter.setMarker(lastMarker);
//        }
//    }

//    public void setMoreNewFromAdapter(boolean isMoreNew) {
//        messageAdapter.setMoreFromNew(isMoreNew);
//    }

//    public void setNewLoadingComplete() {
//        messageAdapter.setNewLoadingComplete();
//    }
//
//    public void setNewNoMoreLoading() {
//        messageAdapter.setNewNoMoreLoading();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void setGotoLatestLayoutVisible() {
//        gotoLatestLayoutVisible = true;
//        if (vgMoveToLatest != null) {
//            vgMoveToLatest.setVisibility(View.VISIBLE);
//        }
//    }

//    public void setOnItemClickListener(MessageAdapter.OnItemClickListener onItemClickListener) {
//        messageAdapter.setOnItemClickListener(onItemClickListener);
//    }
//
//    public void setOnItemLongClickListener(MessageAdapter.OnItemLongClickListener onItemLongClickListener) {
//        messageAdapter.setOnItemLongClickListener(onItemLongClickListener);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void showEmptyView() {
//        layoutEmpty.setVisibility(View.VISIBLE);
//    }

//    public int getLastVisibleItemPosition() {
//        return ((LinearLayoutManager) lvMessages.getLayoutManager()).findLastVisibleItemPosition();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void justRefresh() {
//        int itemCount = messageAdapter.getItemCount();
//        if (itemCount > 0) {
//            messageAdapter.notifyItemRangeChanged(0, itemCount);
//        }
//    }

//    @UiThread
//    public void setMarkerInfo(long teamId, long roomId) {
//        messageAdapter.setTeamId(teamId);
//        messageAdapter.setRoomId(roomId);
//        messageAdapter.notifyDataSetChanged();
//    }

//    @UiThread
//    public void insertMessageEmptyLayout() {
//
//        if (layoutEmpty == null) {
//            return;
//        }
//        layoutEmpty.removeAllViews();
//
//        LayoutInflater.from(activity).inflate(R.layout.view_message_list_empty, layoutEmpty, true);
//    }

//    @UiThread
//    public void insertTeamMemberEmptyLayout() {
//
//        if (layoutEmpty == null) {
//            return;
//        }
//        layoutEmpty.removeAllViews();
//        View view = LayoutInflater.from(activity).inflate(R.layout.view_team_member_empty, layoutEmpty, true);
//        View.OnClickListener onClickListener = v -> {
//            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_TOPIC_CHAT);
//            invitationDialogExecutor.execute();
//        };
//        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(onClickListener);
//        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(onClickListener);
//    }

//    private String getOwnerName() {
//        List<FormattedEntity> users = EntityManager.getInstance().getFormattedUsers();
//        FormattedEntity tempDefaultEntity = new FormattedEntity();
//        FormattedEntity owner = Observable.from(users)
//                .filter(formattedEntity -> formattedEntity.isTeamOwner())
//                .firstOrDefault(tempDefaultEntity)
//                .toBlocking()
//                .first();
//        return owner.getUser().name;
//    }

//    @UiThread
//    public void insertTopicMemberEmptyLayout() {
//
//        if (layoutEmpty == null) {
//            return;
//        }
//
//        layoutEmpty.removeAllViews();
//        View view = LayoutInflater.from(activity).inflate(R.layout.view_topic_member_empty, layoutEmpty, true);
//        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));
//        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));
//
//    }

//    public int getItemCountWithoutEvent() {
//
//        int itemCount = messageAdapter.getItemCount();
//        for (int idx = itemCount - 1; idx >= 0; --idx) {
//            if (messageAdapter.getItemViewType(idx) == BodyViewHolder.Type.Event.ordinal()) {
//                itemCount--;
//            }
//        }
//
//        return itemCount;
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void clearEmptyMessageLayout() {
//        if (layoutEmpty != null) {
//            layoutEmpty.removeAllViews();
//        }
//    }

//    public int getItemCount() {
//        return messageAdapter.getItemCount();
//    }
//
//    public int getItemCountWithoutDummy() {
//        return messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount();
//    }

//    public long getRoomId() {
//        return messageAdapter.getRoomId();
//    }
//
//    public void hideKeyboard() {
//        if (inputMethodManager.isAcceptingText()) {
//            inputMethodManager.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
//        }
//    }

//    public void showStickerPreview() {
//        vgStickerPreview.setVisibility(View.VISIBLE);
//    }
//
//    public void loadSticker(StickerInfo stickerInfo) {
//        StickerManager.LoadOptions loadOption = new StickerManager.LoadOptions();
//        loadOption.scaleType = ScalingUtils.ScaleType.CENTER_CROP;
//        StickerManager.getInstance().loadSticker(ivSticker, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), loadOption);
//    }
//
//    public void dismissStickerPreview() {
//        vgStickerPreview.setVisibility(View.GONE);
//    }
//
//    public void setEntityInfo(long entityId) {
//        messageAdapter.setEntityId(entityId);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void modifyStarredInfo(long messageId, boolean isStarred) {
//        int position = messageAdapter.indexByMessageId(messageId);
//        messageAdapter.modifyStarredStateByPosition(position, isStarred);
//    }

//    public void setLastReadLinkId(long lastReadLinkId) {
//        messageAdapter.setLastReadLinkId(lastReadLinkId);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void moveLastReadLink() {
//        long lastReadLinkId = messageAdapter.getLastReadLinkId();
//
//        if (lastReadLinkId <= 0) {
//            return;
//        }
//
//        int position = messageAdapter.indexOfLinkId(lastReadLinkId);
//
//        if (position > 0) {
//            int measuredHeight = lvMessages.getMeasuredHeight() / 2;
//            if (measuredHeight <= 0) {
//                measuredHeight = (int) TypedValue
//                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                                100f,
//                                activity.getResources().getDisplayMetrics());
//            }
//            ((LinearLayoutManager) lvMessages.getLayoutManager())
//                    .scrollToPositionWithOffset(Math.min(messageAdapter.getItemCount() - 1, position + 1), measuredHeight);
//        } else if (position < 0) {
//            lvMessages.getLayoutManager().scrollToPosition(messageAdapter.getItemCount() - 1);
//        }
//
//    }

//    @UiThread
//    public void setUpOldMessage(List<ResMessages.Link> linkList,
//                                int currentItemCount, boolean isFirstMessage) {
//        if (currentItemCount == 0) {
//            // 첫 로드라면...
//            clearMessages();
//
//            addAll(0, linkList);
//
//            moveLastPage();
//
//            dismissLoadingView();
//
//        } else {
//
//            long latestVisibleLinkId = getFirstVisibleItemLinkId();
//            int firstVisibleItemTop = getFirstVisibleItemTop();
//
//            addAll(0, linkList);
//
//            moveToMessage(latestVisibleLinkId, firstVisibleItemTop);
//        }
//
//        if (!isFirstMessage) {
//            setOldLoadingComplete();
//        } else {
//            setOldNoMoreLoading();
//        }
//    }

//    @UiThread
//    public void setUpNewMessage(List<ResMessages.Link> linkList, long myId, boolean firstLoad) {
//
//        int lastPosition = linkList.size() - 1;
//        if (lastPosition < 0) {
//            return;
//        }
//
//        int visibleLastItemPosition = getLastVisibleItemPositionFromListView();
//        int lastItemPosition = getLastItemPosition();
//
//        addAll(lastItemPosition, linkList);
//
//        ResMessages.Link lastUpdatedMessage = linkList.get(lastPosition);
//        if (!firstLoad
//                && visibleLastItemPosition >= 0
//                && visibleLastItemPosition < lastItemPosition - 1
//                && lastUpdatedMessage.fromEntity != myId) {
//            if (!TextUtils.equals(lastUpdatedMessage.status, "archived")) {
//                showPreviewIfNotLastItem();
//            }
//        } else {
//            long messageId = lastUpdatedMessage.messageId;
//            if (firstLoad) {
//                moveLastReadLink();
//                setUpLastReadLink(myId);
//
//                justRefresh();
//            } else if (messageId <= 0) {
//                if (lastUpdatedMessage.fromEntity != myId) {
//                    moveToMessageById(lastUpdatedMessage.id, 0);
//                }
//            } else {
//                moveToMessage(messageId, 0);
//            }
//        }
//
//    }

//    private void setUpLastReadLink(long myId) {
//        long lastReadLinkId = messageAdapter.getLastReadLinkId();
//        int indexOfLinkId = messageAdapter.indexOfLinkId(lastReadLinkId);
//
//        if (indexOfLinkId < 0) {
//            return;
//        }
//
//
//        if (indexOfLinkId >= messageAdapter.getItemCount() - 1) {
//            // 라스트 링크가 마지막 아이템인경우
//            messageAdapter.setLastReadLinkId(-1);
//        } else {
//            ResMessages.Link item = messageAdapter.getItem(indexOfLinkId + 1);
//            if (item instanceof DummyMessageLink) {
//                // 마지막 아이템은 아니지만 다음 아이템이 더미인경우 마지막 아이템으로 간주
//                messageAdapter.setLastReadLinkId(-1);
//            }
//        }
//
//    }

//    public void updateMessageStarred(int messageId, boolean starred) {
//        int itemCount = messageAdapter.getItemCount();
//        for (int idx = 0; idx > itemCount; ++idx) {
//            ResMessages.OriginalMessage message = messageAdapter.getItem(idx).message;
//            if (message.id == messageId) {
//                message.isStarred = starred;
//                break;
//            }
//        }
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void deleteLinkByMessageId(long messageId) {
//        int position = messageAdapter.indexByMessageId(messageId);
//        messageAdapter.remove(position);
//        messageAdapter.notifyDataSetChanged();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void dismissOfflineLayer() {
//        offlineLayer.dismissOfflineView();
//    }
//
//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void showOfflineLayer() {
//        offlineLayer.showOfflineView();
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void showGrayToast(String message) {
//        ColoredToast.showGray(message);
//    }
//
//    @UiThread
//    public void showLeavedMemberDialog(long entityId) {
//        String name = EntityManager.getInstance().getEntityNameById(entityId);
//        String msg = activity.getString(R.string.jandi_no_long_team_member, name);
//
//        AlertUtil.showConfirmDialog(activity, msg, null, false);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void setDisableUser() {
//        sendLayoutVisibleGone();
//        vDisabledUser.setVisibility(View.VISIBLE);
//        setPreviewVisibleGone();
//    }

//    public void setListTouchListener(View.OnTouchListener touchListener) {
//        if (lvMessages != null) {
//            lvMessages.setOnTouchListener(touchListener);
//        } else if (touchListener != null) {
//            this.listTouchListener = touchListener;
//        }
//    }
//
//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void setUpLastReadLinkIdIfPosition() {
//        // 마커가 마지막아이템을 가르키고 있을때만 position = -1 처리
//        long lastReadLinkId = messageAdapter.getLastReadLinkId();
//        int markerPosition = messageAdapter.indexOfLinkId(lastReadLinkId);
//        if (markerPosition == messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount() - 1) {
//            messageAdapter.setLastReadLinkId(-1);
//        }
//    }

//    public void initAdapter(boolean isFromSearch) {
//
//        if (isFromSearch) {
//            messageAdapter = new MessageListSearchAdapter(activity);
//        } else {
//            messageAdapter = new MainMessageListAdapter(activity);
//        }
//
//    }

//    public void setFirstCursorLinkId(long firstCursorLinkId) {
//        if (messageAdapter instanceof MainMessageListAdapter) {
//            ((MainMessageListAdapter) messageAdapter).setFirstCursorLinkId(firstCursorLinkId);
//        }
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void showOldLoadProgress() {
//
//        if (oldProgressBar.getVisibility() != View.GONE) {
//            return;
//        }
//
//        oldProgressBar.setVisibility(View.VISIBLE);
//
//        Animation inAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, -1f,
//                Animation.RELATIVE_TO_SELF, 0f);
//        inAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
//        inAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//        inAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());
//
//        oldProgressBar.startAnimation(inAnim);
//    }

//    @UiThread(propagation = UiThread.Propagation.REUSE)
//    public void dismissOldLoadProgress() {
//
//        if (oldProgressBar.getVisibility() != View.VISIBLE) {
//            return;
//        }
//
//        Animation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, -1f);
//        outAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
//        outAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//        outAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());
//
//        outAnim.setAnimationListener(new SimpleEndAnimationListener() {
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                oldProgressBar.setVisibility(View.GONE);
//            }
//        });
//        oldProgressBar.startAnimation(outAnim);
//    }
//}