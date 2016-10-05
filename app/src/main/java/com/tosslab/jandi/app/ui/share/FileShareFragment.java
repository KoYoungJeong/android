package com.tosslab.jandi.app.ui.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.share.model.ScrollViewHelper;
import com.tosslab.jandi.app.ui.share.presenter.image.ImageSharePresenter;
import com.tosslab.jandi.app.ui.share.presenter.image.ImageSharePresenterImpl;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EFragment(R.layout.fragment_share_image)
public class FileShareFragment extends Fragment implements ImageSharePresenterImpl.View, MainShareActivity.Share {

    @FragmentArg
    String uriString;

    @FragmentArg
    String subject;

    @FragmentArg
    String text;

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

    @ViewById(R.id.vg_share_root)
    ScrollView vgRoot;

    @Bean(ImageSharePresenterImpl.class)
    ImageSharePresenter imageSharePresenter;

    MentionControlViewModel mentionControlViewModel;

    ScrollViewHelper scrollViewHelper;

    @AfterInject
    void initObject() {
        imageSharePresenter.setView(this);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void initViews() {

        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });

        setOnScrollMode();

        imageSharePresenter.initView(uriString);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.jandi_share_to_jandi) + " (1/1)");
        }
    }

    private void setOnScrollMode() {
        scrollViewHelper = new ScrollViewHelper(etComment, vgRoot);
        scrollViewHelper.initTouchMode();
    }

    private String getMentionType() {
//        return mode == MainShareActivity.MODE_SHARE_TEXT ? MentionControlViewModel.MENTION_TYPE_MESSAGE : MentionControlViewModel.MENTION_TYPE_FILE_COMMENT;
        return MentionControlViewModel.MENTION_TYPE_FILE_COMMENT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1, 1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                });

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void bindImage(File file) {
        final String fileName = file.getName();
        tvTitle.setText(fileName);
        if (FileExtensionsUtil.getExtensions(fileName) == FileExtensionsUtil.Extensions.IMAGE) {
            vgFileIcon.setVisibility(View.GONE);
            ivShareImage.setVisibility(View.VISIBLE);

            ImageLoader.newInstance()
                    .fragment(this)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .uri(Uri.fromFile(file))
                    .into(ivShareImage);
        } else {
            vgFileIcon.setVisibility(View.VISIBLE);
            tvShareFileType.setText(FileExtensionsUtil.getFileTypeText(fileName));
            ivShareImage.setVisibility(View.GONE);

            int resId = FileExtensionsUtil.getFileTypeBigImageResource(fileName);
            ivShareFileIcon.setImageResource(resId);
        }
    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
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

    private ProgressDialog getUploadProgress(String absolutePath, String name) {
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
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }


    @Override
    public void onPause() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishOnUiThread() {
        getActivity().finish();
    }

    public String getTitleText() {
        return tvTitle.getText().toString();
    }

    @UiThread
    @Override
    public void moveEntity(long teamId, long entityId, int entityType) {

        if (getActivity() == null) {
            return;
        }
        startActivity(Henson.with(getActivity())
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

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

    @Override
    public void startShare() {

        List<MentionObject> mentions;
        String messageText;
        if (mentionControlViewModel != null) {
            ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject();
            mentions = mentionInfoObject.getMentions();
            messageText = mentionInfoObject.getMessage();
        } else {
            mentions = new ArrayList<>();
            messageText = etComment.getText().toString();
        }


        File imageFile = imageSharePresenter.getImageFile();
        ProgressDialog uploadProgress = getUploadProgress(imageFile.getParentFile().getAbsolutePath(), imageFile.getName());
        imageSharePresenter.uploadFile(imageFile, getTitleText(), messageText, uploadProgress, mentions);
    }

    @Click(R.id.vg_team)
    void clickSelectTeam() {
        LogUtil.e("team");
        startActivity(new Intent(getActivity(), ShareSelectTeamActivity.class));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @Click(R.id.vg_room)
    void clickSelectRoom() {
        LogUtil.e("room");
        ShareSelectRoomActivity_
                .intent(this)
                .extra("teamId", imageSharePresenter.getTeamId())
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @Click(R.id.et_share_comment)
    void clickComment() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TapComment);
    }

    public void onEvent(ShareSelectTeamEvent event) {
        long teamId = event.getTeamId();
        String teamName = event.getTeamName();
        imageSharePresenter.initEntityData(teamId, teamName);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        long roomId = event.getRoomId();
        String roomName = event.getRoomName();
        int roomType = event.getRoomType();
        imageSharePresenter.setEntityData(roomId, roomName, roomType);
    }

    @Override
    public String getComment() {
        return etComment.getText().toString();
    }

    @Override
    public void setComment(String comment) {
        etComment.setText(comment);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setMentionInfo(long teamId, long roomId, int roomType) {

        if (mentionControlViewModel != null) {
            mentionControlViewModel.reset();
            mentionControlViewModel = null;
        }

        mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(), etComment, teamId, Arrays.asList(roomId), getMentionType());
    }

    @UiThread
    @Override
    public void dismissDialog(ProgressDialog uploadProgress) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (uploadProgress != null && uploadProgress.isShowing()) {
            uploadProgress.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveIntro() {
        IntroActivity.startActivity(getActivity(), false);

        finishOnUiThread();
    }


    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (mentionControlViewModel != null) {
            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(event.getId());
            searchedItemVO.setName(event.getName());
            searchedItemVO.setType(event.getType());
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }

}