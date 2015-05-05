package com.tosslab.jandi.app.ui.profile.member;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EActivity(R.layout.activity_profile)
public class MemberProfileActivity extends BaseAnalyticsActivity {
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private final Logger logger = Logger.getLogger(MemberProfileActivity.class);

    @Bean
    MemberProfileModel memberProfileModel;

    @Bean
    MemberProfilePresenter memberProfilePresenter;

    private File mTempPhotoFile;  // 프로필 사진 변경시 선택한 임시 파일
    private boolean attemptToUpdateData = false;
    private boolean attemptToUpdateName = false;
    private boolean attemptToUpdatePhoto = false;
    private boolean attemptToUpdateEmail = false;

    @AfterViews
    void bindAdapter() {

        setupActionBar();

        getProfileInBackground();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();

        if (attemptToUpdateData || attemptToUpdatePhoto || attemptToUpdateName || attemptToUpdateEmail) {
            getMenuInflater().inflate(R.menu.update_profile_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_update_profile:
                ProgressDialog progressDialog = new ProgressDialog(MemberProfileActivity.this);
                onUpdateProfile(progressDialog);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        trackGaProfile(getDistictId());
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        memberProfilePresenter.dismissProgressWheel();
    }

    /**
     * *********************************************************
     * 프로필 가져오기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    @Background
    void getProfileInBackground() {
        memberProfilePresenter.showProgressWheel();
        try {
            ResLeftSideMenu.User me = memberProfileModel.getProfile();
            memberProfilePresenter.displayProfile(me);
        } catch (JandiNetworkException e) {
            logger.error("get profile failed", e);
            memberProfilePresenter.getProfileFailed();
        } catch (Exception e) {
            logger.error("get profile failed", e);
            memberProfilePresenter.getProfileFailed();
        } finally {
            memberProfilePresenter.dismissProgressWheel();
        }
    }

    /**
     * *********************************************************
     * 프로필 수정
     * **********************************************************
     */
    @Click(R.id.profile_user_status_message)
    void editStatusMessage(View view) {
        // 닉네임
        memberProfilePresenter.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_phone_number)
    void editPhoneNumber(View view) {
        // 핸드폰 번호
        memberProfilePresenter.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_realname)
    void editName(View view) {
        memberProfilePresenter.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_division)
    void editDivision(View view) {
        // 부서
        memberProfilePresenter.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_position)
    void editPosition(View view) {
        // 직책
        memberProfilePresenter.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_email)
    void editEmail(View view) {
        String[] accountEmails = memberProfileModel.getAccountEmails();
        String email = memberProfilePresenter.getEmail();
        memberProfilePresenter.showEmailChooseDialog(accountEmails, email);
    }

    public void onEvent(MemberEmailChangeEvent event) {
        memberProfilePresenter.updateEmailTextColor(event.getEmail());
        attemptToUpdateEmail = true;
        upateOptionMenu();
    }

    public void onEvent(ConfirmModifyProfileEvent event) {
        memberProfilePresenter.updateProfileTextColor(event.actionType, event.inputMessage);
        if (event.actionType == EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME) {
            attemptToUpdateName = true;
        } else {
            attemptToUpdateData = true;
        }
        upateOptionMenu();
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    @Background
    void onUpdateProfile(ProgressDialog progressDialog) {
        // TODO Refactoring...

        if (!attemptToUpdatePhoto && !attemptToUpdateData && !attemptToUpdateName && !attemptToUpdateEmail) {
            memberProfilePresenter.showToastNoUpdateProfile();
            return;
        }

        if (attemptToUpdatePhoto) {
            memberProfilePresenter.showPhotoUploadProgressDialog(progressDialog);
            try {
                memberProfileModel.uploadProfilePhoto(progressDialog, mTempPhotoFile);
                memberProfilePresenter.successPhotoUpload();
                attemptToUpdatePhoto = false;

            } catch (ExecutionException e) {
                logger.error("uploadFileDone: FAILED", e);
                memberProfilePresenter.failPhotoUpload();
            } catch (InterruptedException e) {
                logger.error("uploadFileDone: FAILED", e);
                memberProfilePresenter.failPhotoUpload();
            }


            memberProfilePresenter.dismissProgressDialog(progressDialog);
        }
        memberProfilePresenter.showProgressWheel();
        if (attemptToUpdateData) {
            ReqUpdateProfile reqUpdateProfile = memberProfilePresenter.getUpdateProfile();
            try {
                ResLeftSideMenu.User me = memberProfileModel.updateProfile(reqUpdateProfile);
                memberProfilePresenter.updateProfileSucceed();
                attemptToUpdateData = false;
                trackUpdateProfile(getDistictId(), me);
                memberProfilePresenter.displayProfile(me);
            } catch (JandiNetworkException e) {
                logger.error("get profile failed", e);
                memberProfilePresenter.updateProfileFailed();
            }
        }

        if (attemptToUpdateName) {
            String name = memberProfilePresenter.getName();
            try {
                memberProfileModel.updateProfileName(new ReqProfileName(name));
                memberProfilePresenter.updateProfileSucceed();
                attemptToUpdateName = false;
                memberProfilePresenter.successUpdateNameColor();
            } catch (JandiNetworkException e) {
                e.printStackTrace();
                memberProfilePresenter.updateProfileFailed();
            }
        }

        if (attemptToUpdateEmail) {
            String email = memberProfilePresenter.getEmail();
            try {
                memberProfileModel.updateProfileEmail(email);
                memberProfilePresenter.updateProfileSucceed();
                attemptToUpdateEmail = false;
                memberProfilePresenter.successUpdateEmailColor();
            } catch (JandiNetworkException e) {
                memberProfilePresenter.updateProfileFailed();
            }
        }

        try {
            ResLeftSideMenu entitiesInfo = JandiEntityClient_.getInstance_(MemberProfileActivity.this).getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(MemberProfileActivity.this).upsertLeftSideMenu(entitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entitiesInfo);
            JandiPreference.setBadgeCount(MemberProfileActivity.this, totalUnreadCount);
            BadgeUtils.setBadge(MemberProfileActivity.this, totalUnreadCount);
            EntityManager.getInstance(MemberProfileActivity.this).refreshEntity(entitiesInfo);
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }

        memberProfilePresenter.dismissProgressWheel();


        upateOptionMenu();
    }

    @UiThread
    void upateOptionMenu() {
        invalidateOptionsMenu();
    }

    /**
     * *********************************************************
     * 프로필 사진 업로드
     * **********************************************************
     */
    @Click(R.id.profile_photo)
    void getPicture() {

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);


    }

    @OnActivityResult(REQ_CODE_PICK_IMAGE)
    public void onImagePickResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (imageData != null && imageData.getData() != null) {
            String path = ImageFilePath.getPath(MemberProfileActivity.this, imageData.getData());

            if (GoogleImagePickerUtil.isUrl(path)) {

                String downloadDir = GoogleImagePickerUtil.getDownloadPath();
                String downloadName = GoogleImagePickerUtil.getWebImageName();
                ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(MemberProfileActivity.this, downloadDir, downloadName);
                downloadImage(downloadProgress, path, downloadDir, downloadName);
            } else {
                mTempPhotoFile = new File(path);
                memberProfilePresenter.updateLocalProfileImage(mTempPhotoFile);
            }
        } else {
            mTempPhotoFile = new File(ImageFilePath.getTempPath(MemberProfileActivity.this));
            memberProfilePresenter.updateLocalProfileImage(mTempPhotoFile);
        }

        attemptToUpdatePhoto = true;
        upateOptionMenu();


    }

    @Background
    void downloadImage(ProgressDialog downloadProgress, String path, String downloadDir, String downloadName) {

        try {
            Log.d("INFO", downloadDir + "/" + downloadName);
            File file = GoogleImagePickerUtil.downloadFile(MemberProfileActivity.this, downloadProgress, path, downloadDir, downloadName);
            Log.d("INFO", file.getAbsolutePath());
            memberProfilePresenter.dismissProgressDialog(downloadProgress);
            mTempPhotoFile = new File(file.getAbsolutePath());
            memberProfilePresenter.updateLocalProfileImage(mTempPhotoFile);
            Log.d("INFO", mTempPhotoFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDistictId() {
        EntityManager entityManager = EntityManager.getInstance(MemberProfileActivity.this);
        return entityManager.getDistictId();
    }
}
