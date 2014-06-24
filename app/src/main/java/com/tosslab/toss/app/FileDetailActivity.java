package com.tosslab.toss.app;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.toss.app.lists.FileDetailListAdapter;
import com.tosslab.toss.app.network.MultipartUtility;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResFileDetail;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.DateTransformator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
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

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

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


//    @UiThread
//    void showFileDetail(ResFileDetail resFileDetail) {
//        for (ResMessages.OriginalMessage fileDetail : resFileDetail.fileDetails) {
//            if (fileDetail instanceof ResMessages.FileMessage) {
//                ResMessages.FileMessage fileMessage = (ResMessages.FileMessage)fileDetail;
//                // 사용자
//                ResMessages.Writer writer = fileMessage.writer;
//                String profileUrl = TossConstants.SERVICE_ROOT_URL + writer.u_photoUrl;
//                Picasso.with(this).load(profileUrl).centerCrop().fit().into(imageViewUserProfile);
//                String userName = writer.u_firstName + " " + writer.u_lastName;
//                textViewUserName.setText(userName);
//                // 파일
//                String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
//                textViewFileCreateDate.setText(createTime);
//                textViewFileName.setText(fileMessage.content.name);
//
//                // 이미지일 경우
//                if (fileMessage.content.type != null && fileMessage.content.type.startsWith("image")) {
//                    imageViewPhotoFile.setVisibility(View.VISIBLE);
//                    String photoUrl = TossConstants.SERVICE_ROOT_URL + fileMessage.content.fileUrl;
//                    Picasso.with(this).load(photoUrl).centerCrop().fit().into(imageViewPhotoFile);
//                }
//            } else if (fileDetail instanceof ResMessages.CommentMessage) {
//
//            }
//        }
//
//    }
}
