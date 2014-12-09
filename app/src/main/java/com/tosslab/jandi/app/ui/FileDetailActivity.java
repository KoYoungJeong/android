package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

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
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAnalyticsActivity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);
    @Extra
    public int fileId;

    @RestService
    JandiRestClient jandiRestClient;
    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @ViewById(R.id.list_file_detail_comments)
    ListView listFileDetailComments;
    @ViewById(R.id.et_file_detail_comment)
    EditText editTextComment;
    @ViewById(R.id.btn_file_detail_send_comment)
    Button buttonSendComment;

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
    private EntityManager mEntityManager;
    private JandiEntityClient mJandiEntityClient;

    @AfterViews
    public void initForm() {
        mContext = getApplicationContext();

        setUpActionBar();
        initProgressWheel();
        addFileDetailViewAsListviewHeader();
        initNetworkClientForFileDetail();
        setEditTextWatcher();

        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        mEntityManager = ((JandiApplication) getApplication()).getEntityManager();

        getFileDetail();
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private void initProgressWheel() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

    private void addFileDetailViewAsListviewHeader() {
        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = getLayoutInflater().inflate(R.layout.activity_file_detail_header, null, false);
        imageViewUserProfile = (ImageView) header.findViewById(R.id.img_file_detail_user_profile);
        textViewUserName = (TextView) header.findViewById(R.id.txt_file_detail_user_name);
        textViewFileCreateDate = (TextView) header.findViewById(R.id.txt_file_detail_create_date);
        textViewFileContentInfo = (TextView) header.findViewById(R.id.txt_file_detail_file_info);
        textViewFileSharedCdp = (TextView) header.findViewById(R.id.txt_file_detail_shared_cdp);
        imageViewPhotoFile = (ImageView) header.findViewById(R.id.img_file_detail_photo);
        iconFileType = (ImageView) header.findViewById(R.id.icon_file_detail_content_type);
        listFileDetailComments.addHeaderView(header);
        listFileDetailComments.setAdapter(fileDetailCommentListAdapter);
    }

    private void initNetworkClientForFileDetail() {
        myToken = JandiPreference.getMyToken(this);
        mJandiEntityClient = new JandiEntityClient(jandiRestClient, myToken);
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
        EventBus.getDefault().register(this);
        trackGaFileDetail(mEntityManager);
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
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    void setEditTextWatcher() {
        editTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int inputLength = editable.length();
                buttonSendComment.setSelected(inputLength > 0);
            }
        });
    }

//    @ItemLongClick
//    void list_file_detail_commentsItemLongClicked(ResMessages.OriginalMessage item) {
//        if (item instanceof ResMessages.CommentMessage) {
//            if (cdpItemManager != null && item.writerId == cdpItemManager.mMe.id) {
//                ColoredToast.show(this, "long click");
//            } else {
//                ColoredToast.showError(this, "권한이 없습니다.");
//            }
//
//        }
//    }

    /**
     * *********************************************************
     * 파일 상세 출력 관련
     * **********************************************************
     */
    @UiThread
    void getFileDetail() {
        mProgressWheel.show();
        getFileDetailInBackend();
    }

    @Background
    void getFileDetailInBackend() {
        log.debug("try to get file detail having ID, " + fileId);
        try {
            ResFileDetail resFileDetail = jandiRestClient.getFileDetail(fileId);
            getFileDetailSucceed(resFileDetail);
        } catch (RestClientException e) {
            log.error("fail to get file detail.", e);
            getFileDetailFailed(getString(R.string.err_file_detail));
        }
    }

    @UiThread
    void getFileDetailSucceed(ResFileDetail resFileDetail) {
        mProgressWheel.dismiss();
        drawFileDetail(resFileDetail);
        fileDetailCommentListAdapter.updateFileComments(resFileDetail);
        fileDetailCommentListAdapter.notifyDataSetChanged();
    }

    @UiThread
    void getFileDetailFailed(String errMessage) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, errMessage);
    }

    @UiThread
    public void drawFileSharedEntities() {
        if (mResFileDetail == null) return;
        if (mEntityManager == null) return;

        // 공유 CDP 이름
        String sharedEntityNames = "";
        if (!mResFileDetail.shareEntities.isEmpty()) {
            int nSharedEntities = mResFileDetail.shareEntities.size();
            for (int i = 0; i < nSharedEntities; i++) {
                String sharedEntityName = mEntityManager.getEntityNameById(mResFileDetail.shareEntities.get(i));
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
                FormattedEntity writer = new FormattedEntity(fileMessage.writer);
                String profileUrl = writer.getUserSmallProfileUrl();
                Glide.with(mContext)
                        .load(profileUrl)
                        .placeholder(R.drawable.jandi_profile)
                        .transform(new GlideCircleTransform(mContext))
                        .into(imageViewUserProfile);
                String userName = writer.getName();
                textViewUserName.setText(userName);

                // 파일
                String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
                textViewFileCreateDate.setText(createTime);
                getActionBar().setTitle(fileMessage.content.title);
                String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
                textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

                // 공유 CDP 이름
                drawFileSharedEntities();

                if (fileMessage.content.type != null) {
                    String serverUrl = (fileMessage.content.serverUrl.equals("root"))
                            ? JandiConstantsForFlavors.SERVICE_ROOT_URL
                            : fileMessage.content.serverUrl;

                    if (fileMessage.content.type.startsWith("image")) {
                        // 이미지일 경우
                        iconFileType.setImageResource(R.drawable.jandi_fview_icon_img);
                        // 중간 썸네일을 가져온다.
                        String thumbnailUrl = "";
                        if (fileMessage.content.extraInfo != null) {
                            thumbnailUrl = fileMessage.content.extraInfo.largeThumbnailUrl;
                        }
                        final String thumbnailPhotoUrl = serverUrl + thumbnailUrl;
                        final String photoUrl = serverUrl + fileMessage.content.fileUrl;
                        Glide.with(mContext).load(thumbnailPhotoUrl).placeholder(R.drawable.jandi_down_img).centerCrop().fitCenter().into(imageViewPhotoFile);
                        // 이미지를 터치하면 큰 화면 보기로 넘어감
                        imageViewPhotoFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                Intent hideyBarPhotoViewIntent = HideyBarPhotoViewIntent.newConfiguration()
//                                        .setPhotoUrl(photoUrl, new PicassoPhotoLoader().baseSetup()
//                                                .setPlaceHolderResId(R.drawable.jandi_down_img)
//                                                .showProgressView(false))
//                                        .timeToStartHideyMode(2000)
//                                        .screenTitle(fileMessage.content.name)
//                                        .create(mContext, HideyBarPhotoViewScreen.class);
//                                startActivity(hideyBarPhotoViewIntent);

                                PhotoViewActivity_.intent(FileDetailActivity.this).imageUrl(photoUrl).start();

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
                                String serverUrl = (fileMessage.content.serverUrl.equals("root"))
                                        ? JandiConstantsForFlavors.SERVICE_ROOT_URL
                                        : fileMessage.content.serverUrl;
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

    /**
     * *********************************************************
     * 파일 공유
     * **********************************************************
     */
    void clickShareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_shared);
        dialog.setView(view);
        final AlertDialog cdpSelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 entity를 추출
        List<Integer> shareEntities = mResFileDetail.shareEntities;
        final List<FormattedEntity> unSharedEntities = mEntityManager.retrieveExclusivedEntities(shareEntities);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, unSharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cdpSelectDialog != null)
                    cdpSelectDialog.dismiss();
                shareMessageInBackground(unSharedEntities.get(i).getEntity().id);
            }
        });
    }

    @Background
    public void shareMessageInBackground(int entityIdToBeShared) {
        try {
            mJandiEntityClient.shareMessage(fileId, entityIdToBeShared);
            log.debug("success to share message");
            shareMessageSucceed(entityIdToBeShared);
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
            shareMessageFailed();
        }
    }

    @UiThread
    public void shareMessageSucceed(int entityIdToBeShared) {
        ColoredToast.show(this, getString(R.string.jandi_share_succeed));
        trackSharingFile(mEntityManager,
                mEntityManager.getEntityById(entityIdToBeShared).type,
                mResFileDetail);
        fileDetailCommentListAdapter.clear();
        getFileDetail();
    }

    @UiThread
    public void shareMessageFailed() {
        ColoredToast.showError(this, getString(R.string.err_share));
    }

    /**
     * *********************************************************
     * 파일 공유 해제
     * **********************************************************
     */
    void clickUnshareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_unshared);
        dialog.setView(view);
        final AlertDialog entitySelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 CDP를 추출
        List<Integer> shareEntitiesIds = mResFileDetail.shareEntities;
        final List<FormattedEntity> sharedEntities = mEntityManager.retrieveGivenEntities(shareEntitiesIds);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, sharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (entitySelectDialog != null)
                    entitySelectDialog.dismiss();
                unshareMessageInBackground(sharedEntities.get(i).getEntity().id);
            }
        });
    }

    @Background
    public void unshareMessageInBackground(int entityIdToBeUnshared) {
        try {
            mJandiEntityClient.unshareMessage(fileId, entityIdToBeUnshared);
            log.debug("success to unshare message");
            unshareMessageSucceed(entityIdToBeUnshared);
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
            unshareMessageFailed();
        }
    }

    @UiThread
    public void unshareMessageSucceed(int entityIdToBeUnshared) {
        ColoredToast.show(this, getString(R.string.jandi_unshare_succeed));
        trackUnsharingFile(mEntityManager,
                mEntityManager.getEntityById(entityIdToBeUnshared).type,
                mResFileDetail);
        fileDetailCommentListAdapter.clear();
        getFileDetail();
    }

    @UiThread
    public void unshareMessageFailed() {
        ColoredToast.showError(this, getString(R.string.err_unshare));
    }

    /**
     * *********************************************************
     * 파일 삭제
     * **********************************************************
     */

    @Background
    public void deleteFileInBackground() {
        try {
            mJandiEntityClient.deleteFile(fileId);
            log.debug("success to delete file");
            deleteFileDone(true);
        } catch (JandiNetworkException e) {
            log.error("delete file failed", e);
            deleteFileDone(false);
        }
    }

    @UiThread
    public void deleteFileDone(boolean isOk) {
        if (isOk) {
            ColoredToast.show(this, getString(R.string.jandi_delete_succeed));
            finish();
        } else {
            ColoredToast.showError(this, getString(R.string.err_delete_file));
        }
    }

    /**
     * *********************************************************
     * 댓글 작성 관련
     * **********************************************************
     */

    @Click(R.id.btn_file_detail_send_comment)
    void sendComment() {
        String comment = editTextComment.getText().toString();
        hideSoftKeyboard();

        if (comment.length() > 0) {
            sendCommentInBackground(comment);
        }
    }

    @UiThread
    void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
        editTextComment.setText("");
    }


    @Background
    public void sendCommentInBackground(String message) {
        try {
            mJandiEntityClient.sendMessageComment(fileId, message);
            log.debug("success to send message");
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
        }

        sendCommentDone();
    }

    @UiThread
    public void sendCommentDone() {
        fileDetailCommentListAdapter.clear();
        getFileDetail();
    }

    /**
     * *********************************************************
     * 파일 연결 관련
     * **********************************************************
     */
    public void download() {
        String serverUrl = (mResFileDetail.content.serverUrl.equals("root"))
                ? JandiConstantsForFlavors.SERVICE_ROOT_URL
                : mResFileDetail.content.serverUrl;
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
            trackDownloadingFile(mEntityManager, mResFileDetail);
            // Success
            Intent i = new Intent();
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            i.setDataAndType(Uri.fromFile(file), fileType);
            try {
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                String rawString = getString(R.string.err_unsupported_file_type);
                String formatString = String.format(rawString, file);
                ColoredToast.showError(mContext, formatString);
            }
        } else {
            log.error("Download failed", exception);
            ColoredToast.showError(mContext, getString(R.string.err_download));
        }

    }

    /**
     * *********************************************************
     * 사용자 프로필 보기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    public void onEvent(RequestUserInfoEvent event) {
        int userEntityId = event.userId;
        getProfileInBackground(userEntityId);
    }

    @Background
    void getProfileInBackground(int userEntityId) {
        try {
            ResLeftSideMenu.User user = mJandiEntityClient.getUserProfile(userEntityId);
            getProfileSuccess(user);
        } catch (JandiNetworkException e) {
            log.error("get profile failed", e);
            getProfileFailed();
        } catch (Exception e) {
            log.error("get profile failed", e);
            getProfileFailed();
        }
    }

    @UiThread
    void getProfileSuccess(ResLeftSideMenu.User user) {
        showUserInfoDialog(new FormattedEntity(user));
    }

    @UiThread
    void getProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    private void showUserInfoDialog(FormattedEntity user) {
        boolean isMe = mEntityManager.isMe(user.getId());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        UserInfoDialogFragment dialog = UserInfoDialogFragment.newInstance(user, isMe);
        dialog.show(ft, "dialog");
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListActivity_.intent(mContext)
                        .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                        .entityId(event.userId)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .start();
            }
        }, 250);
    }

}
