package com.tosslab.jandi.app.ui.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.share.presenter.SharePresenter;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EFragment(R.layout.fragment_share_image)
public class MainShareFragment extends Fragment implements SharePresenter.View {

    @FragmentArg
    String uriString;

    @FragmentArg
    String subject;

    @FragmentArg
    String text;

    @FragmentArg
    int mode;

    @ViewById(R.id.iv_share_image)
    ImageView ivShareImage;

    @ViewById(R.id.tv_share_image_title)
    TextView tvTitle;

    @ViewById(R.id.et_share_comment)
    EditText etComment;

    @ViewById(R.id.progress_share_image)
    ProgressBar downloadingProgressBar;

    @ViewById(R.id.tv_room_name)
    TextView tvRoomName;

    @ViewById(R.id.vg_file_icon)
    LinearLayout vgFileIcon;

    @ViewById(R.id.iv_share_file_icon)
    ImageView ivShareFileIcon;

    @ViewById(R.id.tv_team_name)
    TextView tvTeamName;

    @ViewById(R.id.tv_share_file_type)
    TextView tvShareFileType;

    @ViewById(R.id.vg_viwer)
    LinearLayout vgViewer;

    @Bean
    SharePresenter sharePresenter;

    @AfterInject
    void initObject() {
        sharePresenter.setMode(mode);
        sharePresenter.setView(this);
        sharePresenter.setUriString(uriString);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void initViews() {


        if (mode == MainShareActivity.MODE_SHARE_TEXT) {
            vgViewer.setVisibility(View.GONE);
            tvTitle.setVisibility(View.GONE);
            StringBuffer buffer = new StringBuffer();
            if (!TextUtils.isEmpty(subject)) {
                buffer.append(subject).append("\n");
            }

            if (!TextUtils.isEmpty(text)) {
                buffer.append(text);
            }

            etComment.setText(buffer.toString());
            etComment.setSelection(etComment.getText().length());
            etComment.setMaxLines(Integer.MAX_VALUE);
        }

    }

    @UiThread
    @Override
    public void bindImage(File filePath) {
        tvTitle.setText(filePath.getName());
        if (FileExtensionsUtil.getExtensions(filePath.getName()) ==
                FileExtensionsUtil.Extensions.IMAGE) {
            vgFileIcon.setVisibility(View.GONE);
            ivShareImage.setVisibility(View.VISIBLE);
            Glide.with(JandiApplication.getContext())
                    .load(filePath)
                    .fitCenter()
                    .crossFade()
                    .into(ivShareImage);
        } else {
            vgFileIcon.setVisibility(View.VISIBLE);
            tvShareFileType.setText(FileExtensionsUtil.getFileTypeText(filePath.getName()));
            ivShareImage.setVisibility(View.GONE);
            Glide.with(JandiApplication.getContext())
                    .load(FileExtensionsUtil.getFileTypeBigImageResource(filePath.getName()))
                    .fitCenter()
                    .crossFade()
                    .into(ivShareFileIcon);
        }
    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(getActivity().getApplicationContext(), message);
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(getActivity().getApplicationContext(), message);
    }

    @UiThread
    @Override
    public void dismissProgressBar() {
        downloadingProgressBar.setVisibility(View.GONE);
    }

    @UiThread
    @Override
    public void showProgressBar() {
        downloadingProgressBar.setVisibility(View.VISIBLE);
    }

    public ProgressDialog getUploadProgress(String absolutePath, String name) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getActivity().getApplicationContext()
                .getString(R.string.jandi_upload) + " " + absolutePath + "/" + name);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @UiThread
    @Override
    public void finishOnUiThread() {
        getActivity().finish();
    }

    public String getTitleText() {
        return tvTitle.getText().toString();
    }

    public String getCommentText() {
        return etComment.getText().toString();
    }

    @UiThread
    @Override
    public void moveEntity(int teamId, int entityId, int entityType) {

        MainTabActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .roomId(entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityId : -1)
                .entityId(entityId)
                .entityType(entityType)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setTeamName(String name) {
        tvTeamName.setText(name);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setRoomName(String name) {
        tvRoomName.setText(name);
    }

    public void startShare() {
        if (mode == MainShareActivity.MODE_SHARE_FILE) {
            File imageFile = sharePresenter.getImageFile();
            ProgressDialog uploadProgress = getUploadProgress(imageFile.getParentFile().getAbsolutePath(), imageFile.getName());
            sharePresenter.uploadFile(imageFile, getTitleText(), getCommentText(), uploadProgress);
        } else if (mode == MainShareActivity.MODE_SHARE_TEXT) {
            String messageText = etComment.getText().toString();
            sharePresenter.sendMessage(messageText);
        }
    }

    @Click(R.id.vg_team)
    void clickSelectTeam() {
        LogUtil.e("team");
        ShareSelectTeamActivity_
                .intent(this)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @Click(R.id.vg_room)
    void clickSelectRoom() {
        LogUtil.e("room");
        ShareSelectRoomActivity_
                .intent(this)
                .extra("teamId", sharePresenter.getTeamId())
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @Click(R.id.et_share_comment)
    void clickComment() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TapComment);
    }

    public void onEvent(ShareSelectTeamEvent event) {
        int teamId = event.getTeamId();
        String teamName = event.getTeamName();
        sharePresenter.initEntityData(teamId, teamName, true, -1, null, -1);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        int roomId = event.getRoomId();
        String roomName = event.getRoomName();
        int roomType = event.getRoomType();
        sharePresenter.initEntityData(
                sharePresenter.getTeamId(), sharePresenter.getTeamName(),
                false, roomId, roomName, roomType);
    }

    @Override
    public String getComment() {
        return etComment.getText().toString();
    }

    @Override
    public void setComment(String comment) {
        etComment.setText(comment);
    }

}