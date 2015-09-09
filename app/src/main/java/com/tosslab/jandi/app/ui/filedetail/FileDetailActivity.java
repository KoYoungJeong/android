package com.tosslab.jandi.app.ui.filedetail;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMensionEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.filedetail.fileinfo.FileHeadManager;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListFragment;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.ui.sticker.StickerViewModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

import static org.androidannotations.annotations.UiThread.Propagation;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@OptionsMenu(R.menu.file_detail_activity_menu)
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAnalyticsActivity implements FileDetailPresenter.View {

    private static final StickerInfo NULL_STICKER = new StickerInfo();
    @Extra
    int fileId;

    @Extra
    int selectMessageId = -1;

    @Extra
    int roomId = -1;

    @Bean
    FileDetailPresenter fileDetailPresenter;
    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @Bean
    FileHeadManager fileHeadManager;
    @ViewById(R.id.lv_file_detail_comments)
    ListView lvFileDetailComments;
    @ViewById(R.id.vg_file_detail_input_comment)
    RelativeLayout vgCommentLayout;
    @ViewById(R.id.et_message)
    EditText etComment;
    @ViewById(R.id.btn_send_message)
    Button btnSend;
    @ViewById(R.id.vg_file_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.iv_file_detail_preview_sticker_image)
    ImageView ivStickerPreview;
    @ViewById(R.id.rv_list_search_members)
    RecyclerView rvListSearchMembers;

    @Bean
    StickerViewModel stickerViewModel;
    @Bean
    KeyboardHeightModel keyboardHeightModel;
    @SystemService
    InputMethodManager inputMethodManager;
    @SystemService
    ClipboardManager clipboardManager;

    private EntityManager entityManager;
    private boolean isMyFile;
    private boolean isDeleted = true;
    private boolean isForeground;
    private boolean isFromDeleteAction = false;

    private ProgressWheel progressWheel;
    private ProgressDialog progressDialog;
    private StickerInfo stickerInfo = NULL_STICKER;

    @AfterViews
    public void initForm() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.FILE_DETAIL)
                        .build());

        GoogleAnalyticsUtil.sendScreenName("FILE_DETAIL");

        setUpActionBar();

        addFileDetailViewAsListviewHeader();

        fileHeadManager.setRoomId(roomId);

        progressWheel = new ProgressWheel(this);

        entityManager = EntityManager.getInstance();

        fileDetailPresenter.setView(this);

        stickerViewModel.setOnStickerClick(new StickerViewModel.OnStickerClick() {
            @Override
            public void onStickerClick(int groupId, String stickerId) {
                StickerInfo oldSticker = stickerInfo;
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                showStickerPreview();

                if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId()
                        || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
                    loadSticker(stickerInfo);
                }
                setSendButtonSelected(true);
            }
        });

        if (NetworkCheckUtil.isConnected()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, selectMessageId);
        } else if (!fileDetailPresenter.onLoadFromCache(fileId, selectMessageId)) {
            showCheckNetworkDialog();
        }


        JandiPreference.setKeyboardHeight(FileDetailActivity.this, 0);
        addStarredButtonExecution();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    private void addFileDetailViewAsListviewHeader() {
        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = fileHeadManager.getHeaderView();

        lvFileDetailComments.addHeaderView(header);
        lvFileDetailComments.setAdapter(fileDetailCommentListAdapter);
    }

    private void addStarredButtonExecution() {

        fileHeadManager.getStarredButton().setOnClickListener(v -> {
            boolean starred = v.isSelected();
            updateFileStarred(!starred);
            fileDetailPresenter.changeStarredFileMessageState(fileId, !starred);

        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (isDeleted) {
            return true;
        }

        if (isMyFile) {
            getMenuInflater().inflate(R.menu.file_detail_activity_my_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.file_detail_activity_menu, menu);
        }

        return true;
    }

    @ItemLongClick(R.id.lv_file_detail_comments)
    void onCommentLongClick(ResMessages.OriginalMessage item) {
        fileDetailPresenter.onLongClickComment(item);
    }

    @Click(R.id.iv_file_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        FileDetailActivity.this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        setSendButtonSelected(false);
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        fileDetailPresenter.deleteComment(fileId, event.messageType, event.messageId, event.feedbackId);

    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }
        copyToClipboard(event.contentString);
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        fileDetailPresenter.checkSharedEntity(event.getId(), fileId);
    }

    private void setUpActionBar() {
        // Set up the action bar.
        Toolbar toolbar = ((Toolbar) findViewById(R.id.layout_search_bar));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_file_detail_download:
                download();
                return true;
            case R.id.action_file_detail_share:
                clickShareButton();
                return true;
            case R.id.action_file_detail_unshare:
                clickUnShareButton();
                return true;
            case R.id.action_file_detail_delete:
                showDeleteFileDialog(fileId);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        trackGaFileDetail(entityManager);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        fileDetailPresenter.removeClipboardListenerforMention();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        dismissProgress();
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @AfterTextChange(R.id.et_message)
    void onCommentTextChange(Editable editable) {
        int inputLength = editable.length();
        setSendButtonSelected(inputLength > 0);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void finishOnMainThread() {
        finish();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void loadSuccess(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                            boolean isSendAction, int selectMessageId) {

        drawFileDetail(fileMessage, commentMessages, isSendAction);

        if (selectMessageId > 0) {
            int position = fileDetailCommentListAdapter.findMessagePosition(selectMessageId);
            if (position >= 0) {
                // 헤더를 포함하기 때문에 +1 한다.
                lvFileDetailComments.smoothScrollToPosition(position + 1);
            }

            fileDetailCommentListAdapter.setSelectMessage(selectMessageId);
            fileDetailCommentListAdapter.notifyDataSetChanged();

        }

        // XXX what mean Mention VM?
        fileDetailPresenter.refreshMentionVM(this, fileMessage, rvListSearchMembers, etComment, lvFileDetailComments);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(FileDetailActivity.this, (dialog, which) -> finish());
    }

    /**
     * *********************************************************
     * 파일 공유
     * **********************************************************
     */
    void clickShareButton() {
        fileDetailPresenter.onClickShare(fileId);
    }

    @Override
    public void initShareListDialog(List<FormattedEntity> unSharedEntities) {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_invite_to_topic, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_shared);
        dialog.setView(view);
        final AlertDialog cdpSelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        EditText et = (EditText) view.findViewById(R.id.et_cdp_search);

        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, unSharedEntities);

        PublishSubject<String> publishSubject = PublishSubject.create();
        Subscription subscribe = publishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .flatMap(s -> {
                    String searchText = s.toLowerCase();

                    return Observable.from(unSharedEntities)
                            .filter(formattedEntity -> formattedEntity.getName().toLowerCase()
                                    .contains(searchText))
                            .collect((Func0<ArrayList<FormattedEntity>>) ArrayList::new, ArrayList::add);

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::setEntities);

        cdpSelectDialog.setOnDismissListener(dialog1 -> subscribe.unsubscribe());

        publishSubject.onNext("");

        et.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                publishSubject.onNext(s.toString());
            }
        });

        // 현재 이 파일을 share 하지 않는 entity를 추출
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                if (cdpSelectDialog != null) {
                    cdpSelectDialog.dismiss();
                }

                fileDetailPresenter.shareMessage(fileId, adapter.getItem(i).getEntity().id);
            }
        });
    }

    @UiThread
    @Override
    public void showMoveDialog(int entityIdToBeShared) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FileDetailActivity.this);
        builder.setMessage(getString(R.string.jandi_move_entity_after_share))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EntityManager entityManager = EntityManager.getInstance();
                        FormattedEntity entity = entityManager.getEntityById(entityIdToBeShared);

                        moveToMessageListActivity(entityIdToBeShared, entity.type,
                                entity.isUser() ? -1 : entityIdToBeShared, entity.isStarred);
                    }
                })
                .create()
                .show();
    }

    @UiThread
    @Override
    public void onShareMessageSucceed(int entityIdToBeShared, ResMessages.FileMessage fileMessage) {
        ColoredToast.show(this, getString(R.string.jandi_share_succeed, getSupportActionBar().getTitle()));
        trackSharingFile(entityManager,
                entityManager.getEntityById(entityIdToBeShared).type,
                fileMessage);
        clearAdapter();
        fileDetailPresenter.getFileDetail(fileId, false, true, -1);
    }

    /**
     * *********************************************************
     * 파일 공유 해제
     * **********************************************************
     */
    void clickUnShareButton() {
        fileDetailPresenter.onClickUnShare(fileId);
    }

    @Override
    public void initUnShareListDialog(List<FormattedEntity> sharedEntities) {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_unshared);
        dialog.setView(view);
        final AlertDialog entitySelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, sharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (entitySelectDialog != null) {
                    entitySelectDialog.dismiss();
                }
                fileDetailPresenter.unShareMessage(fileId, sharedEntities.get(i).getEntity().id);
            }
        });
    }

    @UiThread
    @Override
    public void onUnShareMessageSucceed(int entityIdToBeUnshared, ResMessages.FileMessage fileMessage) {
        ColoredToast.show(this, getString(R.string.jandi_unshare_succeed, getSupportActionBar().getTitle()));
        trackUnsharingFile(entityManager,
                entityManager.getEntityById(entityIdToBeUnshared).type,
                fileMessage);
        clearAdapter();
        fileDetailPresenter.getFileDetail(fileId, false, true, -1);
    }

    public void onEvent(SocketMessageStarEvent event) {
        int messageId = event.getMessageId();
        boolean starred = event.isStarred();

        LogUtil.e("isStarred", event.isStarred() + "");

        if (messageId == fileId) {
            fileHeadManager.updateStarred(starred);
        }
    }

    public void onEvent(ConfirmDeleteFileEvent event) {
        if (!isForeground) {
            return;
        }
        isFromDeleteAction = true;
        fileDetailPresenter.deleteFile(event.getFileId(), roomId);
    }

    public void onEvent(DeleteFileEvent event) {
        LogUtil.d("FileDetailActivity", "finishing ? " + isFinishing());
        if (isFinishing()) {
            return;
        }

        LogUtil.d("FileDetailActivity", "isFromDeleteAction ? " + isFromDeleteAction);
        if (isFromDeleteAction) {
            return;
        }

        if (fileId == event.getId()) {
            LogUtil.e("FileDetailActivity", "DeleteFileEvent");
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(ShareFileEvent event) {
        if (fileId == event.getId()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (fileId == event.getFileId()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(MoveSharedEntityEvent event) {
        if (!isForeground) {
            return;
        }

        int entityId = event.getEntityId();

        EntityManager entityManager = EntityManager.getInstance();

        FormattedEntity entity = entityManager.getEntityById(entityId);

        int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC
                : entity.isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC
                : JandiConstants.TYPE_DIRECT_MESSAGE;

        int teamId = entityManager.getTeamId();
        boolean isStarred = entity.isStarred;
        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            if (entity.isPublicTopic() && entity.isJoined
                    || entity.isPrivateGroup()) {

                moveToMessageListActivity(entityId, entityType, entityId, isStarred);
            } else {
                fileDetailPresenter.joinAndMove(entity);
            }
        } else {
            moveToMessageListActivity(entityId, entityType, entityId, isStarred);
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void moveToMessageListActivity(int entityId, int entityType, int roomId, boolean isStarred) {
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(EntityManager.getInstance().getTeamId())
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .isFavorite(isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @UiThread
    @Override
    public void onDeleteFileSucceed(boolean isOk) {
        if (isOk) {
            CharSequence title = getSupportActionBar().getTitle();
            if (!TextUtils.isEmpty(title)) {
                ColoredToast.show(this, getString(R.string.jandi_delete_succeed, title));
            } else {
                ColoredToast.show(this, getString(R.string.jandi_delete_succeed, ""));
            }

            Intent data = new Intent();
            data.putExtra(MessageListFragment.EXTRA_FILE_DELETE, true);
            data.putExtra(MessageListFragment.EXTRA_FILE_ID, fileId);
            setResult(RESULT_OK, data);
            finish();
        } else {
            ColoredToast.showError(this, getString(R.string.err_delete_file));
        }
        isFromDeleteAction = false;
    }

    /**
     * *********************************************************
     * 댓글 작성 관련
     * **********************************************************
     */
    @Click(R.id.btn_send_message)
    void sendComment() {
        CharSequence text = etComment.getText();
        String comment = TextUtils.isEmpty(text) ? "" : text.toString().trim();
        hideSoftKeyboard();

        ResultMentionsVO mentions = fileDetailPresenter.getMentionInfo();

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            dismissStickerPreview();
            StickerRepository.getRepository()
                    .upsertRecentSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());

            fileDetailPresenter.sendCommentWithSticker(
                    fileId, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(),
                    mentions.getMessage(), mentions.getMentions());
            stickerInfo = NULL_STICKER;
        } else {
            fileDetailPresenter.sendComment(fileId, mentions.getMessage(), mentions.getMentions());
        }

        etComment.setText("");
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void hideSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showKeyboard() {
        inputMethodManager.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @Click(R.id.btn_message_sticker)
    void onStickerClick(View view) {
        boolean selected = view.isSelected();

        if (selected) {
            stickerViewModel.dismissStickerSelector();
        } else {
            int keyboardHeight =
                    JandiPreference.getKeyboardHeight(FileDetailActivity.this.getApplicationContext());
            if (keyboardHeight > 0) {
                hideSoftKeyboard();
                stickerViewModel.showStickerSelector(keyboardHeight);
                if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
                    keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                        if (isShow) {
                            stickerViewModel.dismissStickerSelector();
                        }
                    });
                }
            } else {
                initKeyboardHeight();
            }
        }
    }

    private void initKeyboardHeight() {
        keyboardHeightModel.setOnKeyboardHeightCaptureListener(() -> {
            onStickerClick(findViewById(R.id.btn_message_sticker));
            keyboardHeightModel.setOnKeyboardHeightCaptureListener(null);
        });
        etComment.requestFocus();
        showKeyboard();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showStickerPreview() {
        vgStickerPreview.setVisibility(View.VISIBLE);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void loadSticker(StickerInfo stickerInfo) {
        StickerManager.getInstance()
                .loadStickerDefaultOption(
                        ivStickerPreview, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    /**
     * *********************************************************
     * 파일 연결 관련
     * **********************************************************
     */
    public void download() {
        initProgressDialog();
        fileDetailPresenter.onClickDownload(progressDialog, fileId);
    }

    @UiThread
    @Override
    public void startGoogleOrDropboxFileActivity(String fileUrl) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
    }

    private void initProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
        }

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showDownloadProgressDialog(String fileName) {
        initProgressDialog();
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();
    }

    @UiThread
    @Override
    public void dismissDownloadProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing() || !isForeground) {
            return;
        }
        progressDialog.dismiss();
    }

    public void onEventMainThread(FileDownloadStartEvent fileDownloadStartEvent) {
        if (!isForeground) {
            return;
        }

        showDownloadProgressDialog(fileDownloadStartEvent.getFileName());

        fileDetailPresenter.downloadFile(fileDownloadStartEvent.getUrl(),
                fileDownloadStartEvent.getFileName(),
                fileDownloadStartEvent.getFileType(),
                fileDownloadStartEvent.getExt(),
                progressDialog, fileId, true);
    }

    @UiThread
    @Override
    public void onDownloadFileSucceed(File file, String fileType, ResMessages.FileMessage fileMessage,
                                      boolean execute) {
        trackDownloadingFile(entityManager, fileMessage);

        try {
            if (execute) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), getFileType(file, fileType));
                startActivity(intent);
            }
            ColoredToast.show(FileDetailActivity.this, getString(R.string.jandi_file_downloaded_into, file.getPath()));
        } catch (ActivityNotFoundException e) {
            String rawString = getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(this, formatString);
        } catch (SecurityException e) {
            String rawString = getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(this, formatString);
        }
    }

    private String getFileType(File file, String fileType) {

        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(
                    fileName.substring(idx + 1, fileName.length()).toLowerCase());
        } else {
            return mimeTypeMap.getExtensionFromMimeType(fileType.toLowerCase());
        }
    }

    /**
     * 사용자 프로필 보기
     */
    public void onEvent(RequestUserInfoEvent event) {
        if (!isForeground) {
            return;
        }
        int userEntityId = event.userId;
        fileDetailPresenter.getProfile(userEntityId);
    }

    @UiThread
    @Override
    public void onGetProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {

        if (!isForeground) {
            return;
        }

        EntityManager entityManager = EntityManager.getInstance();
        moveToMessageListActivity(
                event.userId, JandiConstants.TYPE_DIRECT_MESSAGE, -1,
                entityManager.getEntityById(event.userId).isStarred);
    }

    @UiThread
    @Override
    public void showUserInfoDialog(FormattedEntity user) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        UserInfoDialogFragment_.builder()
                .entityId(user.getId()).build()
                .show(getSupportFragmentManager(), "dialog");
    }

    public void copyToClipboard(String contentString) {
        ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void showDeleteFileDialog(int fileId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(getString(R.string.jandi_file_delete_message))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_delete, (dialog, which) ->
                        EventBus.getDefault().post(new ConfirmDeleteFileEvent(fileId)))
                .create().show();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void drawFileWriterState(boolean isEnabled) {
        fileHeadManager.drawFileWriterState(isEnabled);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void drawFileDetail(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                               boolean isSendAction) {

        fileHeadManager.setFileInfo(fileMessage);

        if (TextUtils.equals(fileMessage.status, "archived")) {
            vgCommentLayout.setVisibility(View.GONE);

            getSupportActionBar().setTitle(R.string.jandi_deleted_file);
        }

        isDeleted = TextUtils.equals(fileMessage.status, "archived");

        isMyFile = fileMessage.writerId == EntityManager.getInstance().getMe().getId();

        invalidateOptionsMenu();

        fileDetailCommentListAdapter.clear();
        fileDetailCommentListAdapter.updateFileComments(commentMessages);
        fileDetailCommentListAdapter.notifyDataSetChanged();

        if (isSendAction) {
            lvFileDetailComments.setSelection(fileDetailCommentListAdapter.getCount());
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showManipulateMessageDialogFragment(ResMessages.OriginalMessage item, boolean isMine) {
        DialogFragment newFragment;
        if (item instanceof ResMessages.CommentMessage) {
            newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(
                    (ResMessages.CommentMessage) item, isMine);
        } else {
            if (!isMine) {
                return;
            }
            newFragment = ManipulateMessageDialogFragment.newInstanceByStickerCommentMessage(
                    (ResMessages.CommentStickerMessage) item, isMine);
        }
        newFragment.show(getSupportFragmentManager(), "dioalog");
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void setSendButtonSelected(boolean selected) {
        btnSend.setSelected(selected);
    }

    @UiThread
    @Override
    public void showProgress() {
        if (progressWheel == null || progressWheel.isShowing()) {
            return;
        }

        progressWheel.show();
    }

    @UiThread
    @Override
    public void dismissProgress() {
        if (progressWheel == null || !progressWheel.isShowing()) {
            return;
        }
        progressWheel.dismiss();
    }

    @UiThread
    @Override
    public void clearAdapter() {
        fileDetailCommentListAdapter.clear();
    }

    @UiThread
    @Override
    public void showToast(String message) {
        ColoredToast.show(this, message);
    }

    @UiThread
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(this, message);
    }

    @Override
    public void onBackPressed() {
        if (!stickerViewModel.isShowStickerSelector()) {
            super.onBackPressed();
        } else {
            stickerViewModel.dismissStickerSelector();
        }
    }

    public void onEvent(SelectedMemberInfoForMensionEvent event) {
        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        MentionControlViewModel mentionControlViewModel = fileDetailPresenter.getMentionControlViewModel();
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    public void onEvent(MessageStarredEvent event) {

        if (!isForeground) {
            return;
        }

        int messageId = event.getMessageId();

        switch (event.getAction()) {
            case STARRED:
                fileDetailPresenter.registStarredComment(messageId);
                break;
            case UNSTARRED:
                fileDetailPresenter.unregistStarredComment(messageId);
                break;
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void modifyStarredInfo(int messageId, boolean isStarred) {
        int position = fileDetailCommentListAdapter.searchIndexOfMessages(messageId);
        fileDetailCommentListAdapter.modifyStarredStateByPosition(position, isStarred);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void updateFileStarred(boolean starred) {
        fileHeadManager.updateStarred(starred);
    }

}
