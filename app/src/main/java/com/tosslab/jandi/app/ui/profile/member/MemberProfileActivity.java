package com.tosslab.jandi.app.ui.profile.member;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadViewModelImpl;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileModel;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EActivity(R.layout.activity_profile)
public class MemberProfileActivity extends BaseAnalyticsActivity {

    @Bean
    MemberProfileModel memberProfileModel;

    @Bean
    MemberProfilePresenter memberProfileView;

    @Bean(ProfileFileUploadViewModelImpl.class)
    FilePickerViewModel filePickerViewModel;

    @Bean
    AlertUtil alertUtil;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
        memberProfileView.dismissProgressWheel();
    }

    /**
     * *********************************************************
     * 프로필 가져오기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    @Background
    void getProfileInBackground() {
        memberProfileView.showProgressWheel();
        try {
            ResLeftSideMenu.User me;
            if (!NetworkCheckUtil.isConnected()) {
                me = memberProfileModel.getSavedProfile(JandiApplication.getContext());
            } else {
                me = memberProfileModel.getProfile();
            }
            memberProfileView.displayProfile(me);
        } catch (RetrofitError e) {
            LogUtil.e("get profile failed", e);
            memberProfileView.getProfileFailed();
        } catch (Exception e) {
            LogUtil.e("get profile failed", e);
            memberProfileView.getProfileFailed();
        } finally {
            memberProfileView.dismissProgressWheel();
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
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.profile_user_phone_number)
    void editPhoneNumber(View view) {
        // 핸드폰 번호
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.profile_user_realname)
    void editName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.profile_user_division)
    void editDivision(View view) {
        // 부서
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.profile_user_position)
    void editPosition(View view) {
        // 직책
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.profile_user_email)
    void editEmail(View view) {
        if (NetworkCheckUtil.isConnected()) {
            String[] accountEmails = memberProfileModel.getAccountEmails();
            String email = memberProfileView.getEmail();
            memberProfileView.showEmailChooseDialog(accountEmails, email);
        }
    }

    @Click(R.id.profile_photo)
    void getPicture() {
        // 프로필 사진
        filePickerViewModel.selectFileSelector(FilePickerViewModel.TYPE_UPLOAD_GALLERY, MemberProfileActivity.this);

    }

    public void onEvent(MemberEmailChangeEvent event) {
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.updateEmailTextColor(event.getEmail());
            uploadEmail(event.getEmail());
        }
    }

    public void onEvent(ProfileChangeEvent event) {
        ResLeftSideMenu.User member = event.getMember();
        if (memberProfileModel.isMyId(member.id)) {
            memberProfileView.displayProfile(member);
            closeDialogFragment();
        }
    }

    @UiThread
    void closeDialogFragment() {
        android.app.Fragment dialogFragment = getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null && dialogFragment instanceof DialogFragment) {
            ((DialogFragment) dialogFragment).dismiss();
        }
    }

    public void onEvent(ConfirmModifyProfileEvent event) {


        if (!NetworkCheckUtil.isConnected()) {
            alertUtil.showCheckNetworkDialog(MemberProfileActivity.this, null);
            return;
        }

        memberProfileView.updateProfileTextColor(event.actionType, event.inputMessage);
        if (event.actionType == EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME) {
            updateProfileName(event.inputMessage);
        } else {
            ReqUpdateProfile reqUpdateProfile = memberProfileView.getUpdateProfile();
            switch (event.actionType) {
                case EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS:
                    reqUpdateProfile.statusMessage = event.inputMessage;
                    break;
                case EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE:
                    reqUpdateProfile.phoneNumber = event.inputMessage;
                    break;
                case EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION:
                    reqUpdateProfile.department = event.inputMessage;
                    break;
                case EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION:
                    reqUpdateProfile.position = event.inputMessage;
                    break;
            }

            updateProfileExtraInfo(reqUpdateProfile);
        }
    }

    @Background
    void updateProfileExtraInfo(ReqUpdateProfile reqUpdateProfile) {
        memberProfileView.showProgressWheel();
        try {
            ResLeftSideMenu.User me = memberProfileModel.updateProfile(reqUpdateProfile);
            memberProfileView.updateProfileSucceed();
            trackUpdateProfile(getDistictId(), me);
            memberProfileView.displayProfile(me);
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("get profile failed", e);
            memberProfileView.updateProfileFailed();
        } finally {
            memberProfileView.dismissProgressWheel();
        }
    }

    @Background
    void updateProfileName(String name) {
        memberProfileView.showProgressWheel();
        try {
            memberProfileModel.updateProfileName(new ReqProfileName(name));
            memberProfileView.updateProfileSucceed();
            memberProfileView.successUpdateNameColor();
        } catch (RetrofitError e) {
            e.printStackTrace();
            memberProfileView.updateProfileFailed();
        } finally {
            memberProfileView.dismissProgressWheel();
        }
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    @Background
    void uploadEmail(String email) {

        if (!NetworkCheckUtil.isConnected()) {
            alertUtil.showCheckNetworkDialog(MemberProfileActivity.this, null);
            return;
        }

        try {
            memberProfileModel.updateProfileEmail(email);
            memberProfileView.updateProfileSucceed();
            memberProfileView.successUpdateEmailColor();
        } catch (RetrofitError e) {
            memberProfileView.updateProfileFailed();
        }
    }

    @UiThread
    void upateOptionMenu() {
        invalidateOptionsMenu();
    }

    @OnActivityResult(FilePickerViewModel.TYPE_UPLOAD_GALLERY)
    public void onImagePickResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        String filePath = filePickerViewModel.getFilePath(getApplicationContext(), FilePickerViewModel.TYPE_UPLOAD_GALLERY, imageData).get(0);
        if (!TextUtils.isEmpty(filePath)) {
            try {
                Crop.of(Uri.fromFile(new File(filePath)),
                        Uri.fromFile(File.createTempFile("temp_", ".jpg",
                                new File(GoogleImagePickerUtil.getDownloadPath()))))
                        .asSquare()
                        .start(MemberProfileActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @OnActivityResult(Crop.REQUEST_CROP)
    public void onImageCropResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            alertUtil.showCheckNetworkDialog(MemberProfileActivity.this, null);
            return;
        }

        Uri output = Crop.getOutput(imageData);

        String filePath = output.getPath();
        if (!TextUtils.isEmpty(filePath)) {
            filePickerViewModel.startUpload(MemberProfileActivity.this, null, -1, filePath, null);
        }
    }

    private String getDistictId() {
        EntityManager entityManager = EntityManager.getInstance(MemberProfileActivity.this);
        return entityManager.getDistictId();
    }
}
