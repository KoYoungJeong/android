package com.tosslab.jandi.app.ui.share.presenter.image;

import android.app.ProgressDialog;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;


@EBean
public class ImageSharePresenterImpl implements ImageSharePresenter {

    @Bean
    ShareModel shareModel;
    private View view;
    private File imageFile;

    private long teamId;
    private long roomId;
    private String teamName;
    private String roomName;
    private boolean isPublic;
    private int roomType;
    private TeamInfoLoader teamInfoLoader;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @AfterInject
    void initObject() {
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        teamId = teamInfoLoader.getTeamId();
        teamName = teamInfoLoader.getTeamName();
    }

    @Override
    public void initView(String uriString) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showFailToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
            view.finishOnUiThread();
            return;
        }

        String imagePath = shareModel.getImagePath(uriString);
        if (TextUtils.isEmpty(imagePath)) {
            view.finishOnUiThread();
            return;
        }
        if (imagePath.startsWith("https://") || imagePath.startsWith("http://")) {
            String downloadDir = FileUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            downloadImage(imagePath, downloadDir, downloadName);
        } else {
            this.imageFile = new File(imagePath);
            view.bindImage(this.imageFile);
        }

        initEntityData(teamId, teamName);
    }

    @Override
    @Background
    public void initEntityData(long teamId, String teamName) {

        this.teamId = teamId;
        this.teamName = teamName;

        if (!shareModel.hasLeftSideMenu(teamId)) {
            try {
                InitialInfo initialInfo = shareModel.getInitialInfo(teamId);
                shareModel.updateInitialInfo(initialInfo);
            } catch (Exception e) {
                e.printStackTrace();
                view.moveIntro();
                return;
            }
        }

        teamInfoLoader = shareModel.getTeamInfoLoader(teamId);

        this.roomId = teamInfoLoader.getDefaultTopicId();
        TopicRoom entity = teamInfoLoader.getTopic(roomId);
        this.roomName = entity.getName();
        this.roomType = JandiConstants.TYPE_PUBLIC_TOPIC;
        isPublic = true;

        view.setTeamName(this.teamName);
        view.setRoomName(this.roomName);
        view.setMentionInfo(teamId, this.roomId, this.roomType);

    }

    @Override
    @Background
    public void setEntityData(long roomId, String roomName, int roomType) {

        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        if (roomType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            isPublic = true;
        } else {
            isPublic = false;
        }

        view.setTeamName(this.teamName);
        view.setRoomName(this.roomName);
        view.setMentionInfo(this.teamId, this.roomId, this.roomType);

    }

    @Override
    @Background
    public void uploadFile(File imageFile,
                           String tvTitle, String commentText,
                           ProgressDialog uploadProgress, List<MentionObject> mentions) {
        try {
            JsonObject result = shareModel.uploadFile(imageFile,
                    tvTitle, commentText, teamId, roomId, uploadProgress, isPublic, mentions);
            if (result.get("code") == null) {
                view.showSuccessToast(JandiApplication.getContext()
                        .getString(R.string.jandi_file_upload_succeed));
                int entityType = 0;
                setupSelectedTeam(teamId);
                view.moveEntity(teamId, roomId, roomType);

                view.finishOnUiThread();

            } else {
                view.showFailToast(JandiApplication.getContext()
                        .getString(R.string.err_file_upload_failed));
            }
        } catch (ExecutionException e) {

            if (view != null) {
                view.showFailToast(JandiApplication.getContext()
                        .getString(R.string.jandi_canceled));
            }
        } catch (Exception e) {
            view.showFailToast(JandiApplication.getContext()
                    .getString(R.string.err_file_upload_failed));
        } finally {
            view.dismissDialog(uploadProgress);
        }
    }

    public boolean setupSelectedTeam(long teamId) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        }
        return true;
    }

    @Background
    void downloadImage(String path, String downloadDir, String downloadName) {
        view.showProgressBar();
        try {
            File file = GoogleImagePickerUtil
                    .downloadFile(JandiApplication.getContext(), null, path, downloadDir, downloadName);
            imageFile = file;
            view.bindImage(file);
        } catch (Exception e) {
        } finally {
            view.dismissProgressBar();
        }
    }

    @Override
    public File getImageFile() {
        return imageFile;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

}
