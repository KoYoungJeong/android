package com.tosslab.jandi.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.dialogs.SelectCdpDialogFragment;
import com.tosslab.jandi.app.events.ConfirmShareEvent;
import com.tosslab.jandi.app.events.RequestSelectionOfCdpToBeShared;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.FileDetailListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends Activity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);

    @Extra
    public int fileId;

    @RestService
    TossRestClient tossRestClient;
    @Bean
    FileDetailListAdapter fileDetailListAdapter;
    @ViewById(R.id.list_file_detail_items)
    ListView listFileDetails;
    @ViewById(R.id.et_file_detail_comment)
    EditText etFileDetailComment;

    public String myToken;

    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    public CdpItemManager cdpItemManager = null;

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

        listFileDetails.setAdapter(fileDetailListAdapter);
        myToken = JandiPreference.getMyToken(this);
        tossRestClient.setHeader("Authorization", myToken);
        getFileDetailFromServer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @ItemLongClick
    void list_file_detail_itemsItemLongClicked(ResMessages.OriginalMessage item) {
        if (item instanceof ResMessages.CommentMessage) {
            if (cdpItemManager != null && item.writerId == cdpItemManager.mMe.id) {
                ColoredToast.show(this, "long click");
            } else {
                ColoredToast.showError(this, "권한이 없습니다.");
            }

        }
    }

    @Background
    void getFileDetailFromServer() {
        log.debug("try to get file detail having ID, " + fileId);
        try {
            ResFileDetail resFileDetail = tossRestClient.getFileDetail(fileId);
            fileDetailListAdapter.updateFileDetails(resFileDetail);
            reloadList();
        } catch (RestClientException e) {
            log.error("fail to get file detail.", e);
        }
    }

    @UiThread
    void reloadList() {
        log.debug("reload");
        fileDetailListAdapter.notifyDataSetChanged();
    }

    /************************************************************
     * 댓글 작성 관련
     ************************************************************/

    @Click(R.id.btn_file_detail_send_comment)
    void sendComment() {
        String comment = etFileDetailComment.getText().toString();
        hideSoftKeyboard();

        if (comment.length() > 0) {
            sendCommentInBackground(comment);
        }
    }

    @UiThread
    void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(etFileDetailComment.getWindowToken(),0);
        etFileDetailComment.setText("");
    }


    @Background
    public void sendCommentInBackground(String message) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, myToken);
        try {
            messageManipulator.sendMessageComment(fileId, message);
            log.debug("success to send message");
        } catch (RestClientException e) {
            log.error("fail to send message", e);
        }

        sendCommentDone();
    }

    @UiThread
    public void sendCommentDone() {
        fileDetailListAdapter.clear();
        getFileDetailFromServer();
    }

    public void onEvent(RequestSelectionOfCdpToBeShared event) {
        // 파일 쉐어를 위한 이벤트
        log.debug("GOoooooOOd");
        DialogFragment newFragment = SelectCdpDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(CdpItemManager event) {
        cdpItemManager = event;
    }

    public void onEvent(ConfirmShareEvent event) {
        sharemessageInBackground(event.selectedCdpIdToBeShared);
    }

    @Background
    public void sharemessageInBackground(int cdpIdToBeShared) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, myToken);
        try {
            messageManipulator.shareMessage(fileId, cdpIdToBeShared);
            log.debug("success to share message");
            shareMessageDone(true);
        } catch (RestClientException e) {
            log.error("fail to send message", e);
            shareMessageDone(false);
        }
    }

    @UiThread
    public void shareMessageDone(boolean isOk) {
        if (isOk) {
            ColoredToast.show(this, "Message has Shared !!");
        } else {
            ColoredToast.showError(this, "FAIL Message Sharing !!");
        }
    }
}
