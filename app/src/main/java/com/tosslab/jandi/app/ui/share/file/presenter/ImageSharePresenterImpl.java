package com.tosslab.jandi.app.ui.share.file.presenter;

import android.app.ProgressDialog;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ImageSharePresenterImpl implements ImageSharePresenter {

    ShareModel shareModel;
    View view;
    private File imageFile;
    private long teamId;
    private long roomId = -1;
    private long entityId = -1;
    private String teamName;
    private String roomName;
    private boolean isPublic;
    private int roomType;
    private TeamInfoLoader teamInfoLoader;

    @Inject
    public ImageSharePresenterImpl(ShareModel shareModel, View view) {
        this.shareModel = shareModel;
        this.view = view;
        initObject();
    }


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
    public void initEntityData(long teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;

        Completable.fromAction(() -> {

            shareModel.refreshPollList(teamId);
            teamInfoLoader = shareModel.getTeamInfoLoader(teamId);
            roomId = -1;
            entityId = -1;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.setTeamName(this.teamName);
                    view.setRoomName("");
                });
    }

    @Override
    public void setEntityData(long roomId, String roomName, int roomType) {
        if (roomType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            this.roomId = TeamInfoLoader.getInstance().getChatId(roomId);
            this.entityId = roomId;
        } else {
            this.roomId = roomId;
            this.entityId = roomId;
        }
        this.roomName = roomName;
        this.roomType = roomType;

        if (roomType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            isPublic = true;
        } else {
            isPublic = false;
        }

        Completable.fromAction(() -> {
            view.setTeamName(this.teamName);
            view.setRoomName(this.roomName);
            view.setMentionInfo(this.teamId, this.roomId, this.roomType);
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();

    }

    @Override
    public void uploadFile(File imageFile,
                           String tvTitle, String commentText,
                           ProgressDialog uploadProgress, List<MentionObject> mentions) {

        if (teamId <= 0 || entityId <= 0) {
            view.dismissDialog(uploadProgress);
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_title_cdp_to_be_shared));
            return;
        }

        Completable.fromCallable(() -> {
            shareModel.uploadFile(imageFile,
                    tvTitle, commentText, teamId, entityId, uploadProgress, isPublic, mentions);

            setupSelectedTeam(teamId);
            return true;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.showSuccessToast(JandiApplication.getContext()
                            .getString(R.string.jandi_file_upload_succeed));
                    view.dismissDialog(uploadProgress);
                    view.moveEntity(teamId, roomId, entityId, roomType);
                    view.finishOnUiThread();
                }, t -> {
                    view.dismissDialog(uploadProgress);
                    view.showFailToast(JandiApplication.getContext()
                            .getString(R.string.err_file_upload_failed));
                });

    }

    public boolean setupSelectedTeam(long teamId) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        }
        return true;
    }

    void downloadImage(String path, String downloadDir, String downloadName) {
        view.showProgressBar();

        Completable.fromCallable(() -> {
            File file = GoogleImagePickerUtil
                    .downloadFile(null, path, downloadDir, downloadName);
            imageFile = file;
            return true;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> view.bindImage(imageFile),
                        t -> view.dismissProgressBar());
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
