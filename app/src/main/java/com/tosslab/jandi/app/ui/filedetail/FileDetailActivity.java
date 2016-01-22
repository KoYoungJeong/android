package com.tosslab.jandi.app.ui.filedetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.repositories.ReadyCommentRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.filedetail.adapter.FileDetailAdapter;
import com.tosslab.jandi.app.ui.filedetail.views.FileShareActivity;
import com.tosslab.jandi.app.ui.filedetail.views.FileShareActivity_;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.ViewSlider;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.BackPressCatchEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAppCompatActivity implements FileDetailPresenter.View {
    public static final String TAG = "FileDetail";

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_STORAGE_PERMISSION_EXPORT = 102;
    private static final int REQ_WINDOW_PERMISSION = 103;

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @Extra
    long roomId = -1;

    @Extra
    long fileId;

    @Extra
    long selectMessageId = -1;

    @Bean
    FileDetailPresenter fileDetailPresenter;

    @Bean
    StickerViewModel stickerViewModel;
    @Bean
    KeyboardHeightModel keyboardHeightModel;
    @SystemService
    InputMethodManager inputMethodManager;
    @ViewById(R.id.lv_file_detail)
    RecyclerView listView;
    @ViewById(R.id.toolbar_file_detail)
    Toolbar toolbar;
    @ViewById(R.id.vg_file_detail_input_comment)
    ViewGroup vgInputWrapper;
    @ViewById(R.id.et_message)
    BackPressCatchEditText etComment;
    @ViewById(R.id.btn_send_message)
    View btnSend;
    @ViewById(R.id.vg_file_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.iv_file_detail_preview_sticker_image)
    SimpleDraweeView ivStickerPreview;
    @ViewById(R.id.vg_option_space)
    ViewGroup vgOptionSpace;
    @ViewById(R.id.btn_show_mention)
    View ivMention;
    private MentionControlViewModel mentionControlViewModel;
    private FileDetailAdapter adapter;

    private ProgressWheel progressWheel;

    private boolean isMyFile;
    private boolean isDeletedFile;
    private boolean isExternalShared;

    private StickerInfo stickerInfo = NULL_STICKER;

    @AfterViews
    void initViews() {
        setUpActionBar();

        initFileInfoViews();

        initProgressWheel();

        initStickers();

        fileDetailPresenter.setView(this);
        fileDetailPresenter.init(fileId, true /* withProgress */);
    }

    private void initStickers() {
        stickerViewModel.setOptionSpace(vgOptionSpace);
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            stickerInfo = new StickerInfo();
            stickerInfo.setStickerGroupId(groupId);
            stickerInfo.setStickerId(stickerId);
            vgStickerPreview.setVisibility(View.VISIBLE);

            if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId()
                    || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {

                StickerManager.getInstance()
                        .loadStickerDefaultOption(ivStickerPreview,
                                stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
            }
            sendCommentSendButtonEnabled();

            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
        });

        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId) -> sendComment());

        stickerViewModel.setType(StickerViewModel.TYPE_FILE_DETAIL);

        stickerViewModel.setStickerButton(findViewById(R.id.btn_message_sticker));
    }

    private void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    private void sendCommentSendButtonEnabled() {
        boolean hasText = TextUtils.isEmpty(etComment.getText())
                && TextUtils.getTrimmedLength(etComment.getText()) > 0;
        boolean enabled = vgStickerPreview.getVisibility() == View.VISIBLE || hasText;
        btnSend.setEnabled(enabled);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    private void initFileInfoViews() {
        listView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        listView.setAdapter(adapter = new FileDetailAdapter(roomId));
        listView.addOnScrollListener(new ViewSlider(toolbar));
    }

    private void initMentionControlViewModel(long messageId, List<Long> sharedTopicIds) {
        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(this,
                    etComment,
                    sharedTopicIds,
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);

            ReadyComment readyComment = ReadyCommentRepository.getRepository().getReadyComment(messageId);
            mentionControlViewModel.setUpMention(readyComment.getText());
            mentionControlViewModel.setOnMentionShowingListener(isShowing -> {
                ivMention.setVisibility(isShowing ? View.GONE : View.VISIBLE);
            });
        } else {
            mentionControlViewModel.refreshMembers(sharedTopicIds);
        }

        boolean isEmpty = mentionControlViewModel.getAllSelectableMembers().size() == 0;
        ivMention.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        refreshClipboardListenerForMention();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshClipboardListenerForMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
            mentionControlViewModel.registClipboardListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (isDeletedFile) {
            return true;
        }

        if (isMyFile) {
            getMenuInflater().inflate(R.menu.file_detail_activity_my_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.file_detail_activity_menu, menu);
        }

        SubMenu subMenu = menu.findItem(R.id.menu_overflow).getSubMenu();
        if (isExternalShared) {
            MenuItem menuItem = subMenu.findItem(R.id.action_file_detail_enable_external_link);
            menuItem.setTitle(R.string.jandi_copy_link);
        } else {
            subMenu.removeItem(R.id.action_file_detail_disable_external_link);
        }

        return true;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void bindFileDetail(ResMessages.FileMessage fileMessage, List<Long> sharedTopicIds,
                               boolean isMyFile, boolean isDeletedFile,
                               boolean isImageFile, boolean isExternalShared) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileMessage.content.title);
        }

        this.isMyFile = isMyFile;
        this.isDeletedFile = isDeletedFile;
        this.isExternalShared = isExternalShared;

        int viewType = isImageFile
                ? FileDetailAdapter.VIEW_TYPE_IMAGE : FileDetailAdapter.VIEW_TYPE_FILE;

        adapter.setRow(0, new FileDetailAdapter.Row<>(fileMessage, viewType));
        adapter.notifyDataSetChanged();

        if (isDeletedFile) {
            vgInputWrapper.setVisibility(View.GONE);
        }

        initMentionControlViewModel(fileMessage.id, sharedTopicIds);

        supportInvalidateOptionsMenu();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void bindComments(List<ResMessages.OriginalMessage> fileComments) {
        List<FileDetailAdapter.Row<?>> rows = new ArrayList<>();
        rows.add(new FileDetailAdapter.Row<>(null, FileDetailAdapter.VIEW_TYPE_COMMENT_DIVIDER));
        Observable.from(fileComments)
                .subscribe(commentMessage -> {
                    boolean isSticker = !(commentMessage instanceof ResMessages.CommentMessage);
                    int viewType = isSticker
                            ? FileDetailAdapter.VIEW_TYPE_STICKER
                            : FileDetailAdapter.VIEW_TYPE_COMMENT;
                    rows.add(new FileDetailAdapter.Row<>(commentMessage, viewType));
                });

        adapter.addRows(rows);
        adapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showUnexpectedErrorToast() {
        ColoredToast.showError(getString(R.string.jandi_err_unexpected));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showKeyboard() {
        inputMethodManager.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void hideKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    public void onEvent(MoveSharedEntityEvent event) {
        ColoredToast.show("Hello World!");
    }


    @OptionsItem(android.R.id.home)
    @Override
    public void finish() {
        super.finish();
    }

    @OptionsItem(R.id.action_file_detail_download)
    void download() {

    }

    @OptionsItem(R.id.action_file_detail_share)
    void share() {
        FileShareActivity_.intent(this)
                .extra("fileId", fileId)
                .startForResult(FileShareActivity.REQUEST_CODE_SHARE);
    }

    @Click(R.id.btn_send_message)
    void sendComment() {
        hideKeyboard();

        stickerViewModel.dismissStickerSelector(true);

        ResultMentionsVO mentionInfo = getMentionInfo();
        String message = mentionInfo.getMessage().trim();
        List<MentionObject> mentions = mentionInfo.getMentions();
        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            dismissStickerPreview();
            long stickerGroupId = stickerInfo.getStickerGroupId();
            String stickerId = stickerInfo.getStickerId();

            StickerRepository.getRepository().upsertRecentSticker(stickerGroupId, stickerId);

            fileDetailPresenter.sendCommentWithSticker(
                    fileId, stickerGroupId, stickerId, message, mentions);

            stickerInfo = NULL_STICKER;
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Send);
        } else {
            fileDetailPresenter.sendComment(fileId, message, mentions);
            sendAnalyticsEvent(AnalyticsValue.Action.Send);
        }

        etComment.setText("");
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action sticker_send) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, sticker_send);
    }

    private ResultMentionsVO getMentionInfo() {
        if (mentionControlViewModel != null) {
            return mentionControlViewModel.getMentionInfoObject();
        } else {
            return new ResultMentionsVO("", new ArrayList<>());
        }
    }

    @OnActivityResult(FileShareActivity.REQUEST_CODE_SHARE)
    void onShareResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || data.hasExtra(FileShareActivity.KEY_ENTITY_ID)) {
            return;
        }

        int entityId = data.getIntExtra(FileShareActivity.KEY_ENTITY_ID, -1);
        ResMessages.FileMessage fileMessage = adapter.getItem(0);
        fileDetailPresenter.onShareAction(entityId, fileMessage);
    }
}
