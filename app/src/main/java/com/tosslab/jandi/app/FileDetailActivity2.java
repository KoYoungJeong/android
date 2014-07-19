package com.tosslab.jandi.app;

import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.dialogs.SelectCdpDialogFragment;
import com.tosslab.jandi.app.events.ConfirmShareEvent;
import com.tosslab.jandi.app.events.RequestViewFile;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
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

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity2 extends BaseActivity {
    private final Logger log = Logger.getLogger(FileDetailActivity2.class);
    @Extra
    public int fileId;

    @RestService
    TossRestClient tossRestClient;
    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @ViewById(R.id.list_file_detail_items)
    ListView listFileDetailComments;
    @ViewById(R.id.et_file_detail_comment)
    EditText etFileDetailComment;

    // in File Detail Header
    ImageView imageViewUserProfile;
    TextView textViewUserName;
    TextView textViewFileCreateDate;
    TextView textViewFileName;
    TextView textViewFileContentInfo;

    ImageView imageViewPhotoFile;
    ImageView buttonFileDetailShare;

    private BroadcastReceiver mCompleteReceiver = new FileDownloadBroadcastReceiver();

    public String myToken;

    private Context mContext;
    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    public CdpItemManager cdpItemManager = null;

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

        listFileDetailComments.setAdapter(fileDetailCommentListAdapter);

        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = getLayoutInflater().inflate(R.layout.activity_file_detail_header, null, false);
        imageViewUserProfile = (ImageView)header.findViewById(R.id.img_file_detail_user_profile_2);
        textViewUserName = (TextView)header.findViewById(R.id.txt_file_detail_user_name_2);
        textViewFileCreateDate = (TextView)header.findViewById(R.id.txt_file_detail_create_date_2);
        textViewFileName = (TextView)header.findViewById(R.id.txt_file_detail_name_2);
        textViewFileContentInfo = (TextView)header.findViewById(R.id.txt_file_detail_file_info_2);
        imageViewPhotoFile = (ImageView)header.findViewById(R.id.img_file_detail_photo_2);
        buttonFileDetailShare = (ImageView)header.findViewById(R.id.btn_file_detail_share_2);
        listFileDetailComments.addHeaderView(header);

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
        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mCompleteReceiver, completeFilter);
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        unregisterReceiver(mCompleteReceiver);
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
            drawFileDetail(resFileDetail);
            fileDetailCommentListAdapter.updateFileComments(resFileDetail);
            reloadList();
        } catch (RestClientException e) {
            log.error("fail to get file detail.", e);
        }
    }

    @UiThread
    void reloadList() {
        log.debug("reload");
        fileDetailCommentListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void drawFileDetail(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
                // 사용자
                ResMessages.Writer writer = fileMessage.writer;
                String profileUrl = JandiConstants.SERVICE_ROOT_URL + writer.u_photoUrl;
                Picasso.with(mContext).load(profileUrl).centerCrop().fit().into(imageViewUserProfile);
                String userName = writer.u_firstName + " " + writer.u_lastName;
                textViewUserName.setText(userName);
                // 파일
                String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
                textViewFileCreateDate.setText(createTime);
                textViewFileName.setText(fileMessage.content.name);
                // 파일 이름을 터치하면 파일 연결
                textViewFileName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String serverUrl = (fileMessage.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:fileMessage.content.serverUrl;
                        EventBus.getDefault().post(new RequestViewFile(serverUrl + fileMessage.content.fileUrl, fileMessage.content.type));
                    }
                });

                String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
                textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

                // 이미지일 경우
                if (fileMessage.content.type != null && fileMessage.content.type.startsWith("image")) {
                    imageViewPhotoFile.setVisibility(View.VISIBLE);
                    String photoUrl = (JandiConstants.SERVICE_ROOT_URL + fileMessage.content.fileUrl).replaceAll(" ", "%20");
                    Picasso.with(mContext).load(photoUrl).centerCrop().fit().into(imageViewPhotoFile);
                }
                buttonFileDetailShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogFragment newFragment = SelectCdpDialogFragment.newInstance();
                        newFragment.show(getFragmentManager(), "dialog");
                    }
                });
                break;
            }

        }
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
        fileDetailCommentListAdapter.clear();
        getFileDetailFromServer();
    }

//    /**
//     * Event from FileDetailView
//     * FileDetailView 에서 파일 쉐어 버튼을 눌렀을 때, 발생하는 이벤트
//     * @param event
//     */
//    public void onEvent(RequestSelectionOfCdpToBeShared event) {
//        DialogFragment newFragment = SelectCdpDialogFragment.newInstance();
//        newFragment.show(getFragmentManager(), "dialog");
//    }

    /**
     * Event from SelectCdpDialogFragment
     * Share 할 CDP를 선택한 다음에 "공유"를 눌렀을때 발생하는 이벤트
     * @param event
     */
    public void onEvent(ConfirmShareEvent event) {
        sharemessageInBackground(event.selectedCdpIdToBeShared);
    }

    /**
     * Sticky Event from SearchListFragment or MainMessageListFragment
     * 파일 공유를 위한 다른 CDP 리스트 정보를 가져오기 위해
     * SearchListFragment 나 MainMessageListFragment 에서 리스트 메시지 타입이 파일일 경우 던져줌
     * @param event
     */
    public void onEvent(CdpItemManager event) {
        cdpItemManager = event;
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


    /************************************************************
     * 파일 연결 관련
     ************************************************************/
    public void onEvent(RequestViewFile event) {
        download(event.fileUrl, event.fileType);
    }

    private DownloadManager mDownloadManager;
    private long mDownloadQueueId;
    private String mFileName;
    private String mFileType;

    public void download(String url, String fileType) {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        List<String> pathSegmentList = uri.getPathSegments();
        mFileName = pathSegmentList.get(pathSegmentList.size()-1);

        request.setTitle("Jandi");
        request.setDescription("download " + mFileName);
        request.setMimeType(fileType);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/temp").mkdirs();

        mFileType = fileType;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/temp", mFileName);
        mDownloadQueueId = mDownloadManager.enqueue(request);

    }

    private class FileDownloadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                ColoredToast.show(mContext, "Complete");
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
//                Intent i = new Intent();
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.setAction(Intent.ACTION_VIEW);
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                String localUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + mFileName;
////                String mimeType = mFileType;
//                String extension = MimeTypeMap.getFileExtensionFromUrl(localUrl);
//                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//                log.debug(mimeType + " for " + localUrl);
//
//                File file = new File(localUrl);
//                i.setDataAndType(Uri.fromFile(file), mimeType);
//                try {
//                    startActivity(i);
//                } catch (ActivityNotFoundException e) {
//                    ColoredToast.showError(mContext, file + "을 확인할 수 있는 앱이 설치되지 않았습니다.");
//                }
            } else {
                ColoredToast.showError(mContext, action);
            }
        }
    }
}
