package com.tosslab.jandi.app.ui.filedetail;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.ui.search.messages.adapter.spannable.MessageSpannable;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailPresenter {

    @RootContext
    ActionBarActivity activity;

    @SystemService
    InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    @SystemService
    ClipboardManager clipboardManager;

    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @ViewById(R.id.list_file_detail_comments)
    ListView listFileDetailComments;

    @ViewById(R.id.ly_file_detail_input_comment)
    LinearLayout inputCommentLayout;
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
    View disableLineThroughView;
    View disableCoverView;

    ImageView imageViewPhotoFile;
    ImageView iconFileType;

    private ProgressWheel mProgressWheel;
    private LinearLayout fileInfoLayout;


    @AfterViews
    void initViews() {
        addFileDetailViewAsListviewHeader();
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

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
        fileInfoLayout = (LinearLayout) header.findViewById(R.id.ly_file_detail_info);
        iconFileType = (ImageView) header.findViewById(R.id.icon_file_detail_content_type);
        disableLineThroughView = header.findViewById(R.id.img_entity_listitem_line_through);
        disableCoverView = header.findViewById(R.id.view_entity_listitem_warning);

        listFileDetailComments.addHeaderView(header);
        listFileDetailComments.setAdapter(fileDetailCommentListAdapter);
    }


    public void setSendButtonSelected(boolean selected) {
        buttonSendComment.setSelected(selected);
    }

    @UiThread
    public void drawFileDetail(ResFileDetail resFileDetail, boolean isSendAction) {

        ResMessages.OriginalMessage fileDetail = getFileMessage(resFileDetail.messageDetails);

        final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;

        // 사용자
        FormattedEntity writer = new FormattedEntity(fileMessage.writer);
        String profileUrl = writer.getUserSmallProfileUrl();
        Ion.with(imageViewUserProfile)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(profileUrl);
        String userName = writer.getName();
        textViewUserName.setText(userName);

        imageViewUserProfile.setOnClickListener(v -> UserInfoDialogFragment_.builder().entityId(fileDetail.writerId).build().show(activity.getSupportFragmentManager(), "dialog"));
        textViewUserName.setOnClickListener(v -> UserInfoDialogFragment_.builder().entityId(fileDetail.writerId).build().show(activity.getSupportFragmentManager(), "dialog"));

        // 파일
        String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
        textViewFileCreateDate.setText(createTime);
        // if Deleted File
        if (TextUtils.equals(fileMessage.status, "archived")) {

            imageViewPhotoFile.setImageResource(R.drawable.jandi_fl_icon_deleted);
            fileInfoLayout.setVisibility(View.GONE);
            inputCommentLayout.setVisibility(View.GONE);

            activity.getSupportActionBar().setTitle(R.string.jandi_deleted_file);

        } else {

            activity.getSupportActionBar().setTitle(fileMessage.content.title);
            String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
            textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.ext);

            // 공유 CDP 이름
            drawFileSharedEntities(fileMessage);

            if (!TextUtils.isEmpty(fileMessage.content.type)) {
                String serverUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL;

                if (fileMessage.content.type.startsWith("image")) {
                    // 이미지일 경우
                    iconFileType.setImageResource(R.drawable.jandi_fview_icon_img);
                    // 중간 썸네일을 가져온다.
                    String thumbnailUrl = "";
                    if (fileMessage.content.extraInfo != null) {
                        thumbnailUrl = fileMessage.content.extraInfo.largeThumbnailUrl;

                        final String thumbnailPhotoUrl = serverUrl + thumbnailUrl;
                        final String photoUrl = serverUrl + fileMessage.content.fileUrl;

                        if (TextUtils.equals(fileMessage.content.type, "image/gif")) {
                            Ion.with(activity)
                                    .load(thumbnailPhotoUrl)
                                    .intoImageView(imageViewPhotoFile);
                        } else {
                            Ion.with(imageViewPhotoFile)
                                    .fitCenter()
                                    .crossfade(true)
                                    .load(thumbnailPhotoUrl);
                        }
                        // 이미지를 터치하면 큰 화면 보기로 넘어감
                        imageViewPhotoFile.setOnClickListener(view -> PhotoViewActivity_
                                .intent(activity)
                                .imageUrl(photoUrl)
                                .imageName(fileMessage.content.name)
                                .imageType(fileMessage.content.type)
                                .start());
                    } else {
                        imageViewPhotoFile.setImageResource(R.drawable.jandi_fl_icon_img);
                    }

                } else {

                    iconFileType.setImageResource(getIconByFileType(fileMessage.content.type));
                    imageViewPhotoFile.setImageResource(getDownloadIcon(fileMessage.content.type));

                    // 파일 타입 이미지를 터치하면 다운로드로 넘어감.
                    imageViewPhotoFile.setOnClickListener(view -> {
                        String serverUrl1 = (fileMessage.content.serverUrl.equals("root"))
                                ? JandiConstantsForFlavors.SERVICE_ROOT_URL
                                : fileMessage.content.serverUrl;
                        String fileName = fileMessage.content.fileUrl.replace(" ", "%20");
                        EventBus.getDefault().post(new FileDownloadStartEvent(serverUrl1 + fileName, fileMessage.content.name, fileMessage.content.type));
                    });
                }
            }
        }

        fileDetailCommentListAdapter.clear();
        fileDetailCommentListAdapter.updateFileComments(resFileDetail);
        fileDetailCommentListAdapter.notifyDataSetChanged();

        if (isSendAction) {
            listFileDetailComments.setSelection(fileDetailCommentListAdapter.getCount());
        }
    }

    private ResMessages.OriginalMessage getFileMessage(List<ResMessages.OriginalMessage> messageDetails) {

        for (ResMessages.OriginalMessage messageDetail : messageDetails) {
            if (messageDetail instanceof ResMessages.FileMessage) {
                return messageDetail;
            }
        }

        return null;
    }

    private int getDownloadIcon(String type) {
        if (type.startsWith("audio")) {
            return R.drawable.jandi_down_audio;
        } else if (type.startsWith("video")) {
            return R.drawable.jandi_down_video;
        } else if (type.startsWith("application/pdf")) {
            return R.drawable.jandi_down_pdf;
        } else if (type.startsWith("text")) {
            return R.drawable.jandi_down_txt;
        } else if (TextUtils.equals(type, "application/x-hwp")) {
            return R.drawable.jandi_down_hwp;
        } else if (FormatConverter.isSpreadSheetMimeType(type)) {
            return R.drawable.jandi_down_txt;
        } else if (FormatConverter.isPresentationMimeType(type)) {
            return R.drawable.jandi_down_txt;
        } else if (FormatConverter.isDocmentMimeType(type)) {
            return R.drawable.jandi_down_txt;
        } else {
            return R.drawable.jandi_down_etc;
        }

    }

    private int getIconByFileType(String type) {
        if (type.startsWith("audio")) {
            return R.drawable.jandi_fview_icon_audio;
        } else if (type.startsWith("video")) {
            return R.drawable.jandi_fview_icon_video;
        } else if (type.startsWith("application/pdf")) {
            return R.drawable.jandi_fview_icon_pdf;
        } else if (type.startsWith("text")) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (TextUtils.equals(type, "application/x-hwp")) {
            return R.drawable.jandi_fl_icon_hwp;
        } else if (FormatConverter.isSpreadSheetMimeType(type)) {
            return R.drawable.jandi_fl_icon_exel;
        } else if (FormatConverter.isPresentationMimeType(type)) {
            return R.drawable.jandi_fview_icon_ppt;
        } else if (FormatConverter.isDocmentMimeType(type)) {
            return R.drawable.jandi_fview_icon_txt;
        } else {
            return R.drawable.jandi_fview_icon_etc;
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void drawFileSharedEntities(ResMessages.FileMessage resFileDetail) {
        if (resFileDetail == null) {
            return;
        }
        EntityManager mEntityManager = EntityManager.getInstance(activity);
        if (mEntityManager == null) {
            return;
        }

        int teamId = mEntityManager.getTeamId();

        if (resFileDetail.shareEntities != null && !resFileDetail.shareEntities.isEmpty()) {
            int nSharedEntities = resFileDetail.shareEntities.size();
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            int shareTextSize = (int) activity.getResources().getDimension(R.dimen.jandi_text_size_small);
            int shareTextColor = activity.getResources().getColor(R.color.file_type);
            MessageSpannable messageSpannable = new MessageSpannable(shareTextSize, shareTextColor);
            spannableStringBuilder.append(activity.getString(R.string.jandi_shared_in_room))
                    .setSpan(messageSpannable, 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (int idx = 0; idx < nSharedEntities; idx++) {
                FormattedEntity sharedEntity = mEntityManager.getEntityById(resFileDetail.shareEntities.get(idx));

                if (sharedEntity == null) {
                    continue;
                }

                if (spannableStringBuilder.length() > 0) {
                    spannableStringBuilder.append(", ");
                }

                int entityType;
                if (sharedEntity.isPrivateGroup()) {
                    entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
                } else if (sharedEntity.isPublicTopic()) {
                    entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
                } else {
                    entityType = JandiConstants.TYPE_DIRECT_MESSAGE;

                }

                EntitySpannable entitySpannable = new EntitySpannable(activity, teamId, sharedEntity.getId(), entityType, sharedEntity.isStarred);

                int length = spannableStringBuilder.length();
                spannableStringBuilder.append(sharedEntity.getName());

                spannableStringBuilder.setSpan(entitySpannable, length, length + sharedEntity.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            }
            textViewFileSharedCdp.setMovementMethod(LinkMovementMethod.getInstance());
            textViewFileSharedCdp.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
        } else {
            textViewFileSharedCdp.setText(R.string.jandi_nowhere_shared_file);
        }
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
        ColoredToast.show(activity, activity.getString(R.string.jandi_unshare_succeed, activity.getSupportActionBar().getTitle()));
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
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        UserInfoDialogFragment_.builder().entityId(user.getId()).build().show(activity.getSupportFragmentManager(), "dialog");

    }

    public String getCommentText() {
        return editTextComment.getText().toString();
    }

    public void copyToClipboard(String contentString) {
        ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void showDeleteFileDialog(int fileId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(activity.getString(R.string.jandi_file_delete_message))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_delete, (dialog, which) -> EventBus.getDefault().post(new ConfirmDeleteFileEvent(fileId)))
                .create().show();

    }

    @UiThread
    public void drawFileWriterState(boolean isEnabled) {
        if (isEnabled) {
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
            textViewUserName.setTextColor(activity.getResources().getColor(R.color.deactivate_text_color));
        }
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(activity, message);
    }
}
