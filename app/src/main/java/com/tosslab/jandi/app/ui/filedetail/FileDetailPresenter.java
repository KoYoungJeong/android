package com.tosslab.jandi.app.ui.filedetail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailPresenter {

    @RootContext
    Activity activity;

    @SystemService
    InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

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

    private ProgressWheel mProgressWheel;


    @AfterViews
    void initViews() {
        addFileDetailViewAsListviewHeader();
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

        listFileDetailComments.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
    }

    private void addFileDetailViewAsListviewHeader() {
        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = LayoutInflater.from(activity).inflate(R.layout.activity_file_detail_header, null, false);
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


    public void setSendButtonSelected(boolean selected) {
        buttonSendComment.setSelected(selected);
    }

    @UiThread
    public void drawFileDetail(ResFileDetail resFileDetail, boolean isSendAction) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
                // 사용자
                FormattedEntity writer = new FormattedEntity(fileMessage.writer);
                String profileUrl = writer.getUserSmallProfileUrl();
                Glide.with(activity)
                        .load(profileUrl)
                        .placeholder(R.drawable.jandi_profile)
                        .transform(new GlideCircleTransform(activity))
                        .into(imageViewUserProfile);
                String userName = writer.getName();
                textViewUserName.setText(userName);

                // 파일
                String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
                textViewFileCreateDate.setText(createTime);
                activity.getActionBar().setTitle(fileMessage.content.title);
                String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
                textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

                // 공유 CDP 이름
                drawFileSharedEntities(fileMessage);

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
                        Glide.with(activity)
                                .load(thumbnailPhotoUrl)
                                .fitCenter()
                                .crossFade()
                                .into(imageViewPhotoFile);
                        // 이미지를 터치하면 큰 화면 보기로 넘어감
                        imageViewPhotoFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                PhotoViewActivity_.intent(activity).imageUrl(photoUrl).start();

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
                        } else if (TextUtils.equals(fileMessage.content.type, "application/x-hwp")) {
                            iconFileType.setImageResource(R.drawable.jandi_fl_icon_hwp);
                            imageViewPhotoFile.setImageResource(R.drawable.jandi_down_hwp);
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
                                EventBus.getDefault().post(new FileDownloadStartEvent(serverUrl + fileName, fileMessage.content.name, fileMessage.content.type));
                            }
                        });
                    }
                }

                break;
            }
        }

        fileDetailCommentListAdapter.clear();
        fileDetailCommentListAdapter.updateFileComments(resFileDetail);
        fileDetailCommentListAdapter.notifyDataSetChanged();

        if (isSendAction) {
            listFileDetailComments.setSelection(fileDetailCommentListAdapter.getCount());
        }
    }

    @UiThread
    public void drawFileSharedEntities(ResMessages.FileMessage resFileDetail) {
        if (resFileDetail == null) {
            return;
        }
        EntityManager mEntityManager = EntityManager.getInstance(activity);
        if (mEntityManager == null) {
            return;
        }

        // 공유 CDP 이름
        String sharedEntityNames = "";
        if (!resFileDetail.shareEntities.isEmpty()) {
            int nSharedEntities = resFileDetail.shareEntities.size();
            for (int i = 0; i < nSharedEntities; i++) {
                String sharedEntityName = mEntityManager.getEntityNameById(resFileDetail.shareEntities.get(i));
                if (!sharedEntityName.isEmpty()) {
                    sharedEntityNames += sharedEntityName;
                    sharedEntityNames += (i < nSharedEntities - 1) ? ", " : "";
                }
            }
        }
        textViewFileSharedCdp.setText(sharedEntityNames);
    }


    public void clearAdapter() {
        fileDetailCommentListAdapter.clear();
    }

    @UiThread
    public void showProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }

        if (mProgressWheel != null) {
            mProgressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {

        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }

    }


    @UiThread
    public void unshareMessageSucceed(int entityIdToBeUnshared) {
        ColoredToast.show(activity, activity.getString(R.string.jandi_unshare_succeed));
        fileDetailCommentListAdapter.clear();
    }


    public void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
        editTextComment.setText("");
    }

    @UiThread
    public void downloadDone(File file, String fileType, ProgressDialog progressDialog) {

        progressDialog.dismiss();

        // Success
        Intent i = new Intent();
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.setDataAndType(Uri.fromFile(file), fileType);
        try {
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            String rawString = activity.getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(activity, formatString);
        }

    }

    @UiThread
    public void showUserInfoDialog(FormattedEntity user) {
        EntityManager entityManager = EntityManager.getInstance(activity);
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        UserInfoDialogFragment_.builder().entityId(user.getId()).build().show(activity.getFragmentManager(), "dialog");

    }

    public String getCommentText() {
        return editTextComment.getText().toString();
    }

}
