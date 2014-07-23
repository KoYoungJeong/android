package com.tosslab.jandi.app;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hideybarphotoviewscreen.HideyBarPhotoViewIntent;
import com.hideybarphotoviewscreen.HideyBarPhotoViewScreen;
import com.hideybarphotoviewscreen.photoloader.PicassoPhotoLoader;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.CdpSelectListAdapter;
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

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);
    @Extra
    public int fileId;

    @RestService
    TossRestClient tossRestClient;
    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @ViewById(R.id.list_file_detail_comments)
    ListView listFileDetailComments;
    @ViewById(R.id.et_file_detail_comment)
    EditText etFileDetailComment;

    // in File Detail Header
    ImageView imageViewUserProfile;
    TextView textViewUserName;
    TextView textViewFileCreateDate;
    TextView textViewFileName;
    TextView textViewFileContentInfo;
    TextView textViewFileSharedCdp;

    ImageView imageViewPhotoFile;
    ImageView buttonFileDetailShare;
    ImageView buttonFileDetailMore;

    public String myToken;

    private ResMessages.FileMessage mResFileDetail;
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

        imageViewUserProfile = (ImageView)header.findViewById(R.id.img_file_detail_user_profile);
        textViewUserName = (TextView)header.findViewById(R.id.txt_file_detail_user_name);
        textViewFileCreateDate = (TextView)header.findViewById(R.id.txt_file_detail_create_date);
        textViewFileName = (TextView)header.findViewById(R.id.txt_file_detail_name);
        textViewFileContentInfo = (TextView)header.findViewById(R.id.txt_file_detail_file_info_2);
        textViewFileSharedCdp = (TextView)header.findViewById(R.id.txt_file_detail_shared_cdp);
        imageViewPhotoFile = (ImageView)header.findViewById(R.id.img_file_detail_photo_2);
        buttonFileDetailShare = (ImageView)header.findViewById(R.id.btn_file_detail_share);
        buttonFileDetailMore = (ImageView)header.findViewById(R.id.btn_file_detail_more);
        listFileDetailComments.addHeaderView(header);

        myToken = JandiPreference.getMyToken(this);
        tossRestClient.setHeader("Authorization", myToken);
        getFileDetail();
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


    /**
     * Sticky Event from SearchListFragment or MainMessageListFragment
     * 파일 공유를 위한 다른 CDP 리스트 정보를 가져오기 위해
     * SearchListFragment 나 MainMessageListFragment 에서 리스트 메시지 타입이 파일일 경우 던져줌
     * @param event
     */
    public void onEvent(CdpItemManager event) {
        log.debug("cdpItemManager is set");
        cdpItemManager = event;
        drawFileSharedEntities();
    }

    @ItemLongClick
    void list_file_detail_commentsItemLongClicked(ResMessages.OriginalMessage item) {
        if (item instanceof ResMessages.CommentMessage) {
            if (cdpItemManager != null && item.writerId == cdpItemManager.mMe.id) {
                ColoredToast.show(this, "long click");
            } else {
                ColoredToast.showError(this, "권한이 없습니다.");
            }

        }
    }

    /************************************************************
     * 파일 상세 출력 관련
     ************************************************************/
    @UiThread
    void getFileDetail() {
        mProgressWheel.show();
        getFileDetailInBackend();
    }

    @Background
    void getFileDetailInBackend() {
        log.debug("try to get file detail having ID, " + fileId);
        try {
            ResFileDetail resFileDetail = tossRestClient.getFileDetail(fileId);
            drawFileDetail(resFileDetail);
            fileDetailCommentListAdapter.updateFileComments(resFileDetail);
            getFileDetailDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to get file detail.", e);
            getFileDetailDone(false, "File detail failed");
        }
    }

    @UiThread
    void getFileDetailDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        if (isOk) {
            log.debug("reload");
            fileDetailCommentListAdapter.notifyDataSetChanged();
        } else {
            ColoredToast.showError(this, message);
        }

    }

    @UiThread
    public void drawFileSharedEntities() {
        if (mResFileDetail == null) return;
        if (cdpItemManager == null) return;

        // 공유 CDP 이름
        String sharedEntityNames = "";
        if (!mResFileDetail.shareEntities.isEmpty()) {
            int nSharedEntities = mResFileDetail.shareEntities.size();
            for (int i=0; i<nSharedEntities; i++) {
                String sharedEntityName = cdpItemManager.getCdpNameById(mResFileDetail.shareEntities.get(i));
                if (!sharedEntityName.isEmpty()) {
                    sharedEntityNames += sharedEntityName;
                    sharedEntityNames += (i < nSharedEntities - 1) ? ", " : "";
                }
            }
        }
        textViewFileSharedCdp.setText(sharedEntityNames);
    }

    @UiThread
    public void drawFileDetail(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
                mResFileDetail = fileMessage;
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

                String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
                textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

                // 공유 CDP 이름
                drawFileSharedEntities();

                // 이미지일 경우
                if (fileMessage.content.type != null && fileMessage.content.type.startsWith("image")) {
                    imageViewPhotoFile.setVisibility(View.VISIBLE);
                    final String photoUrl = (JandiConstants.SERVICE_ROOT_URL + fileMessage.content.fileUrl).replaceAll(" ", "%20");
                    Picasso.with(mContext).load(photoUrl).centerCrop().fit().into(imageViewPhotoFile);
                    // 이미지를 터치하면 큰 화면 보기로 넘어감
                    imageViewPhotoFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent hideyBarPhotoViewIntent = HideyBarPhotoViewIntent.newConfiguration()
                                    .setPhotoUrl(photoUrl, new PicassoPhotoLoader().baseSetup()
                                            .setPlaceHolderResId(R.drawable.ic_actionbar_logo)
                                            .showProgressView(false))
                                    .timeToStartHideyMode(2000)
                                    .screenTitle(fileMessage.content.name)
                                    .create(mContext, HideyBarPhotoViewScreen.class);
                            startActivity(hideyBarPhotoViewIntent);
                        }
                    });

                }
                buttonFileDetailShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 전체 CDP 리스트를 보여주고 선택한 CDP를 공유 액션으로 연결
                        clickShareButton();
                    }
                });
                buttonFileDetailMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String serverUrl = (fileMessage.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:fileMessage.content.serverUrl;
                        String fileName = fileMessage.content.fileUrl.replace(" ", "%20");
                        download(serverUrl + fileName, fileMessage.content.name, fileMessage.content.type);
                    }
                });
                break;
            }

        }
    }

    /************************************************************
     * 파일 공유
     ************************************************************/
    void clickShareButton() {
        /**
         * 사용자 리스트 Dialog 를 보여준 뒤, 선택된 사용자가 올린 파일을 검색
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.title_cdp_to_be_shared);
        dialog.setIcon(android.R.drawable.ic_menu_agenda);
        dialog.setView(view);
        final AlertDialog cdpSelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 CDP를 추출
        List<Integer> shareEntities = mResFileDetail.shareEntities;
        final List<CdpItem> unSharedEntities = cdpItemManager.retrieveExceptGivenEntities(shareEntities);
        final CdpSelectListAdapter adapter = new CdpSelectListAdapter(this, unSharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cdpSelectDialog != null)
                    cdpSelectDialog.dismiss();
                shareMessageInBackground(unSharedEntities.get(i).id);
            }
        });
    }

    @Background
    public void shareMessageInBackground(int cdpIdToBeShared) {
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
            fileDetailCommentListAdapter.clear();
            getFileDetail();
        } else {
            ColoredToast.showError(this, "FAIL Message Sharing !!");
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
        getFileDetail();
    }

    /************************************************************
     * 파일 연결 관련
     ************************************************************/
    public void download(String url, String fileName, final String fileType) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();

        log.debug("download " + url);
        Ion.with(this)
                .load(url)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .write(new File(dir, fileName))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        progressDialog.dismiss();
                        downloadDone(e, result, fileType);
                    }
                });
    }

    @UiThread
    public void downloadDone(Exception exception, File file, String fileType) {
        if (exception == null) {
            // Success
            Intent i = new Intent();
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            i.setDataAndType(Uri.fromFile(file), fileType);
            try {
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                ColoredToast.showError(mContext, file + "을 확인할 수 있는 앱이 설치되지 않았습니다.");
            }
        } else {
            log.error("Download failed", exception);
            ColoredToast.showError(mContext, "파일 다운로드에 실패하였습니다");
        }

    }
}
