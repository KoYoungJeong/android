package com.tosslab.jandi.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.Menu;
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
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.CdpSelectListAdapter;
import com.tosslab.jandi.app.lists.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.CircleTransform;
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
    TextView textViewFileContentInfo;
    TextView textViewFileSharedCdp;

    ImageView imageViewPhotoFile;
    ImageView iconFileType;

    public String myToken;

    private ResMessages.FileMessage mResFileDetail;
    private Context mContext;
    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    public CdpItemManager cdpItemManager = null;

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = getLayoutInflater().inflate(R.layout.activity_file_detail_header, null, false);
        imageViewUserProfile = (ImageView)header.findViewById(R.id.img_file_detail_user_profile);
        textViewUserName = (TextView)header.findViewById(R.id.txt_file_detail_user_name);
        textViewFileCreateDate = (TextView)header.findViewById(R.id.txt_file_detail_create_date);
        textViewFileContentInfo = (TextView)header.findViewById(R.id.txt_file_detail_file_info);
        textViewFileSharedCdp = (TextView)header.findViewById(R.id.txt_file_detail_shared_cdp);
        imageViewPhotoFile = (ImageView)header.findViewById(R.id.img_file_detail_photo);
        iconFileType = (ImageView)header.findViewById(R.id.icon_file_detail_content_type);
        listFileDetailComments.addHeaderView(header);
        listFileDetailComments.setAdapter(fileDetailCommentListAdapter);

        myToken = JandiPreference.getMyToken(this);
        tossRestClient.setHeader("Authorization", myToken);
        getFileDetail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_detail_activity_menu, menu);
        return true;
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
                clickUnshareButton();
                return true;
            case R.id.action_file_detail_delete:
                deleteFileInBackground();
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

    @Override
    public void finish() {
        setResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        super.finish();
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
                Picasso.with(mContext).load(profileUrl).placeholder(R.drawable.jandi_profile).transform(new CircleTransform()).into(imageViewUserProfile);
                String userName = writer.u_firstName + " " + writer.u_lastName;
                textViewUserName.setText(userName);
                // 파일
                String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
                textViewFileCreateDate.setText(createTime);
                getActionBar().setTitle(fileMessage.content.name);

                String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
                textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

                // 공유 CDP 이름
                drawFileSharedEntities();

                if (fileMessage.content.type != null) {
                    String serverUrl = (fileMessage.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:fileMessage.content.serverUrl;
//                    String fileName = fileMessage.content.fileUrl.replace(" ", "%20");

                    if (fileMessage.content.type.startsWith("image")) {
                        // 이미지일 경우
                        iconFileType.setImageResource(R.drawable.jandi_fview_icon_img);
                        // 중간 썸네일을 가져온다.
                        String thumbnailUrl = "";
                        if (fileMessage.content.extraInfo != null) {
                            thumbnailUrl = fileMessage.content.extraInfo.largeThumbnailUrl;
                        }
                        final String photoUrl = serverUrl + thumbnailUrl;
                        Picasso.with(mContext).load(photoUrl).placeholder(R.drawable.jandi_down_img).centerCrop().fit().into(imageViewPhotoFile);
                        // 이미지를 터치하면 큰 화면 보기로 넘어감
                        imageViewPhotoFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent hideyBarPhotoViewIntent = HideyBarPhotoViewIntent.newConfiguration()
                                        .setPhotoUrl(photoUrl, new PicassoPhotoLoader().baseSetup()
                                                .setPlaceHolderResId(R.drawable.jandi_down_img)
                                                .showProgressView(false))
                                        .timeToStartHideyMode(2000)
                                        .screenTitle(fileMessage.content.name)
                                        .create(mContext, HideyBarPhotoViewScreen.class);
                                startActivity(hideyBarPhotoViewIntent);
                            }
                        });
                    } else {
                        if (fileMessage.content.type.startsWith("audio")) {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_audio);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_audio);
                        } else if (fileMessage.content.type.startsWith("video")) {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_video);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_video);
                        } else if (fileMessage.content.type.startsWith("application/pdf")) {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_pdf);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_pdf);
                        } else if (fileMessage.content.type.startsWith("text")) {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_txt);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_txt);
                        } else if (FormatConverter.isMsOfficeMimeType(fileMessage.content.type)) {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_txt);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_txt);
                        } else {
                            iconFileType.setImageResource(R.drawable.jandi_fview_icon_etc);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_etc);
                        }

                        // 파일 타입 이미지를 터치하면 다운로드로 넘어감.
                        imageViewPhotoFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String serverUrl = (fileMessage.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:fileMessage.content.serverUrl;
                                String fileName = fileMessage.content.fileUrl.replace(" ", "%20");
                                downloadInBackground(serverUrl + fileName, fileMessage.content.name, fileMessage.content.type);
                            }
                        });
                    }
                }

                break;
            }

        }
    }

    /************************************************************
     * 파일 공유
     ************************************************************/
    void clickShareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_shared);
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
     * 파일 공유 해제
     ************************************************************/
    void clickUnshareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_unshared);
        dialog.setIcon(android.R.drawable.ic_menu_agenda);
        dialog.setView(view);
        final AlertDialog cdpSelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 CDP를 추출
        List<Integer> shareEntitiesIds = mResFileDetail.shareEntities;
        final List<CdpItem> sharedEntities = cdpItemManager.retrieveGivenEntities(shareEntitiesIds);
        final CdpSelectListAdapter adapter = new CdpSelectListAdapter(this, sharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cdpSelectDialog != null)
                    cdpSelectDialog.dismiss();
                unshareMessageInBackground(sharedEntities.get(i).id);
            }
        });
    }

    @Background
    public void unshareMessageInBackground(int cdpIdToBeShared) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, myToken);
        try {
            messageManipulator.unshareMessage(fileId, cdpIdToBeShared);
            log.debug("success to unshare message");
            unshareMessageDone(true);
        } catch (RestClientException e) {
            log.error("fail to send message", e);
            unshareMessageDone(false);
        }
    }

    @UiThread
    public void unshareMessageDone(boolean isOk) {
        if (isOk) {
            ColoredToast.show(this, "공유가 해제되었습니다");
            fileDetailCommentListAdapter.clear();
            getFileDetail();
        } else {
            ColoredToast.showError(this, "FAIL Message Sharing !!");
        }
    }

    /************************************************************
     * 파일 삭제
     ************************************************************/

    @Background
    public void deleteFileInBackground() {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, myToken);
        try {
            messageManipulator.deleteFile(fileId);
            log.debug("success to delete file");
            deleteFileDone(true);
        } catch (RestClientException e) {
            log.error("delete file failed", e);
            deleteFileDone(false);
        }
    }

    @UiThread
    public void deleteFileDone(boolean isOk) {
        if (isOk) {
            ColoredToast.show(this, "파일이 삭제되었습니다");
            finish();
        } else {
            ColoredToast.showError(this, "파일 삭제가 실패하였습니다");
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
    public void download() {
        String serverUrl = (mResFileDetail.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:mResFileDetail.content.serverUrl;
        String fileName = mResFileDetail.content.fileUrl.replace(" ", "%20");
        downloadInBackground(serverUrl + fileName, mResFileDetail.content.name, mResFileDetail.content.type);
    }

    public void downloadInBackground(String url, String fileName, final String fileType) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();

        log.debug("downloadInBackground " + url);
        Ion.with(this)
                .load(url)
                .progressDialog(progressDialog)
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
