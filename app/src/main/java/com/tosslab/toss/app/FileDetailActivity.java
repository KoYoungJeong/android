package com.tosslab.toss.app;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.toss.app.lists.FileDetailListAdapter;
import com.tosslab.toss.app.network.MessageManipulator;
import com.tosslab.toss.app.network.MultipartUtility;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResFileDetail;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.DateTransformator;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends Activity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);
    @Extra
    public String myToken;
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

    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

        listFileDetails.setAdapter(fileDetailListAdapter);

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
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
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

    @Click(R.id.btn_file_detail_send_comment)
    void sendComment() {
        String comment = etFileDetailComment.getText().toString();
        hideSoftKeyboard();

        if (comment.length() > 0) {
            sendMessageInBackground(comment);
        }
    }

    @UiThread
    void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(etFileDetailComment.getWindowToken(),0);
        etFileDetailComment.setText("");
    }


    @Background
    public void sendMessageInBackground(String message) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, myToken);
        try {
            messageManipulator.sendMessageComment(fileId, message);
            log.debug("success to send message");
        } catch (RestClientException e) {
            log.error("fail to send message", e);
        }

        sendMessageDone();
    }

    @UiThread
    public void sendMessageDone() {
        fileDetailListAdapter.clear();
        getFileDetailFromServer();
    }
}
