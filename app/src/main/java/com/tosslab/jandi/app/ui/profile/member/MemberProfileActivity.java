package com.tosslab.jandi.app.ui.profile.member;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
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
    private static final String TEMP_PHOTO_FILE = "temp.png";   // 임시 저장파일
    private final Logger log = Logger.getLogger(MemberProfileActivity.class);

    @Bean
    MemberProfileModel memberProfileModel;

    @Bean
    MemberProfilePresenter memberProfilePresenter;

    private Context mContext;

    private File mTempPhotoFile;  // 프로필 사진 변경시 선택한 임시 파일
    private boolean attemptToUpdate = false;
    private boolean attemptToUpdatePhoto = false;

    @AfterViews
    void bindAdapter() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mContext = getApplicationContext();

        getProfileInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();

        if (attemptToUpdate || attemptToUpdatePhoto) {
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
            log.error("get profile failed", e);
            memberProfilePresenter.getProfileFailed();
        } catch (Exception e) {
            log.error("get profile failed", e);
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

    public void onEvent(ConfirmModifyProfileEvent event) {
        memberProfilePresenter.updateProfileTextColor(event.actionType, event.inputMessage);
        attemptToUpdate = true;
        invalidateOptionsMenu();
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    @Background
    void onUpdateProfile(ProgressDialog progressDialog) {
        if (!attemptToUpdatePhoto && !attemptToUpdate) {
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
                log.error("uploadFileDone: FAILED", e);
                memberProfilePresenter.failPhotoUpload();
            } catch (InterruptedException e) {
                log.error("uploadFileDone: FAILED", e);
                memberProfilePresenter.failPhotoUpload();
            }


            memberProfilePresenter.dismissProgressDialog(progressDialog);
        }
        if (attemptToUpdate) {
            ReqUpdateProfile reqUpdateProfile = memberProfilePresenter.getUpdateProfile();
            memberProfilePresenter.showProgressWheel();
            try {
                ResLeftSideMenu.User me = memberProfileModel.updateProfile(reqUpdateProfile);
                memberProfilePresenter.updateProfileSucceed();
                attemptToUpdate = false;
                trackUpdateProfile(getDistictId(), me);
                memberProfilePresenter.displayProfile(me);
            } catch (JandiNetworkException e) {
                log.error("get profile failed", e);
                memberProfilePresenter.updateProfileFailed();
            } finally {
                memberProfilePresenter.dismissProgressWheel();
            }
        }

        invalidateOptionsMenu();
    }

    /**
     * *********************************************************
     * 프로필 사진 업로드
     * **********************************************************
     */
    @Click(R.id.profile_photo)
    void getPicture() {
        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT,      // 또는 ACTION_PICK
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");              // 모든 이미지
        intent.putExtra("crop", "true");        // Crop기능 활성화
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageFilePath.getTempUri());
        intent.putExtra("outputFormat",         // 포맷방식
                Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    @OnActivityResult(REQ_CODE_PICK_IMAGE)
    public void onImagePickResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (imageData != null && imageData.getData() != null) {
            attemptToUpdatePhoto = true;
//            mTempPhotoFile = new File(ImageFilePath.getPath(MemberProfileActivity.this, imageData.getData()));
            mTempPhotoFile = new File(ImageFilePath.getTempPath());
            memberProfilePresenter.updateLocalProfileImage(mTempPhotoFile);
            invalidateOptionsMenu();
        }
    }

    private String getDistictId() {
        EntityManager entityManager = ((JandiApplication) getApplication()).getEntityManager();
        return entityManager.getDistictId();
    }
}
