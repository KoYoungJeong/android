package com.tosslab.jandi.app.ui.share.presenter;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.ui.share.MainShareActivity;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 2. 14..
 */
@EBean
public class SharePresenter {

    @Bean
    ShareModel shareModel;
    private View view;
    private File imageFile;
    private String uriString;

    private int teamId;
    private int roomId;
    private String teamName;
    private String roomName;
    private boolean isPublic;
    private int roomType;

    private int mode;

    public void setView(View view) {
        this.view = view;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    @AfterInject
    void initObject() {
        EntityManager entityManager = EntityManager.getInstance();
        teamId = entityManager.getTeamId();
        teamName = entityManager.getTeamName();
    }

    @AfterViews
    void initView() {
        if (mode == MainShareActivity.MODE_SHARE_FILE) {
            String imagePath = shareModel.getImagePath(uriString);
            if (TextUtils.isEmpty(imagePath)) {
                view.finishOnUiThread();
                return;
            }
            if (imagePath.startsWith("https://") || imagePath.startsWith("http://")) {
                String downloadDir = GoogleImagePickerUtil.getDownloadPath();
                String downloadName = GoogleImagePickerUtil.getWebImageName();
                downloadImage(imagePath, downloadDir, downloadName);
            } else {
                this.imageFile = new File(imagePath);
                view.bindImage(this.imageFile);
            }
        }

        initEntityData(teamId, teamName, true, -1, null, -1);
    }

    @Background
    public void initEntityData(int teamId, String teamName,
                               boolean isDefaultTopic,
                               int roomId, String roomName, int roomType) {

        this.teamId = teamId;
        this.teamName = teamName;
        isPublic = false;

        if (isDefaultTopic) {
            int defaultTopicId =
                    Integer.valueOf(shareModel.getTeamInfoById(teamId).getTeamDefaultChannelId());
            ResRoomInfo roomInfo = shareModel.getEntityById(teamId, defaultTopicId);
            this.roomId = roomInfo.getId();
            this.roomName = roomInfo.getName();

            if (roomInfo.getType().equals("privateGroup")) {
                this.roomType = JandiConstants.TYPE_PRIVATE_TOPIC;
            } else if (roomInfo.getType().equals("channel")) {
                this.roomType = JandiConstants.TYPE_PUBLIC_TOPIC;
                isPublic = true;
            } else {
                this.roomType = JandiConstants.TYPE_DIRECT_MESSAGE;
            }
        } else {
            this.roomId = roomId;
            this.roomName = roomName;
            this.roomType = roomType;
            if (roomType == JandiConstants.TYPE_PUBLIC_TOPIC) {
                isPublic = true;
            }
        }

        view.setTeamName(this.teamName);
        view.setRoomName(this.roomName);

    }

    @Background
    public void uploadFile(File imageFile,
                           String tvTitle, String commentText,
                           ProgressDialog uploadProgress) {
        try {
            JsonObject result = shareModel.uploadFile(imageFile,
                    tvTitle, commentText, teamId, roomId, uploadProgress, isPublic);
            if (result.get("code") == null) {
                LogUtil.e("Upload Success : " + result);
                view.showSuccessToast(JandiApplication.getContext()
                        .getString(R.string.jandi_file_upload_succeed));
                int entityType = 0;
                shareModel.trackUploadingFile(entityType, result);
            } else {
                LogUtil.e("Upload Fail : Result : " + result);
                view.showFailToast(JandiApplication.getContext()
                        .getString(R.string.err_file_upload_failed));
            }
            view.finishOnUiThread();
        } catch (ExecutionException e) {
            if (view != null) {
                view.showFailToast(JandiApplication.getContext()
                        .getString(R.string.jandi_canceled));
            }
        } catch (Exception e) {
            LogUtil.e("Upload Error : ", e);
            view.showFailToast(JandiApplication.getContext()
                    .getString(R.string.err_file_upload_failed));
        } finally {
            // 이동해야 함.
            setupSelectedTeam(teamId);
            view.moveEntity(teamId, roomId, roomType);

        }
    }

    public boolean setupSelectedTeam(int teamId) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
            return getEntityInfo();
        } else {
            try {
                EntityManager.getInstance();
                return true;
            } catch (Exception e) {
                return getEntityInfo();
            }
        }
    }

    private boolean getEntityInfo() {
        try {
            EntityClientManager entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.upsertBadgeCount(EntityManager.getInstance().getTeamId(), totalUnreadCount);
            BadgeUtils.setBadge(JandiApplication.getContext(), totalUnreadCount);
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Background
    void downloadImage(String path, String downloadDir, String downloadName) {
        view.showProgressBar();
        try {
            Log.d("INFO", "Download Path " + downloadDir + "/" + downloadName);
            File file = GoogleImagePickerUtil
                    .downloadFile(JandiApplication.getContext(), null, path, downloadDir, downloadName);
            Log.d("INFO", "Downloaded Path " + file.getAbsolutePath());
            imageFile = file;
            view.bindImage(file);
        } catch (Exception e) {
        } finally {
            view.dismissProgressBar();
        }
    }

    @Background
    public void sendMessage(String messageText) {
        view.showProgressBar();
        try {
            shareModel.sendMessage(teamId, roomId, roomType, messageText);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_share_succeed, view.getComment()));
            view.finishOnUiThread();
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
        } finally {
            view.dismissProgressBar();
            setupSelectedTeam(teamId);
            view.moveEntity(teamId, roomId, roomType);
        }
    }

    public File getImageFile() {
        return imageFile;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int entityId) {
        this.roomId = entityId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public interface View {
        void showProgressBar();

        void dismissProgressBar();

        void bindImage(File imagePath);

        void finishOnUiThread();

        void showSuccessToast(String message);

        void showFailToast(String message);

        void setTeamName(String name);

        void setRoomName(String name);

        void moveEntity(int teamId, int entityId, int entityType);

        String getComment();

        void setComment(String comment);
    }

}
