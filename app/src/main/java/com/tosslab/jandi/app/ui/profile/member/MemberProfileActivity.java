package com.tosslab.jandi.app.ui.profile.member;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadViewModelImpl;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

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
            ResLeftSideMenu.User me = memberProfileModel.getProfile();
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
        memberProfileView.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_phone_number)
    void editPhoneNumber(View view) {
        // 핸드폰 번호
        memberProfileView.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_realname)
    void editName(View view) {
        memberProfileView.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_division)
    void editDivision(View view) {
        // 부서
        memberProfileView.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_position)
    void editPosition(View view) {
        // 직책
        memberProfileView.launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                ((TextView) view)
        );
    }

    @Click(R.id.profile_user_email)
    void editEmail(View view) {
        String[] accountEmails = memberProfileModel.getAccountEmails();
        String email = memberProfileView.getEmail();
        memberProfileView.showEmailChooseDialog(accountEmails, email);
    }

    public void onEvent(MemberEmailChangeEvent event) {
        memberProfileView.updateEmailTextColor(event.getEmail());
        uploadEmail(event.getEmail());
    }

    public void onEvent(ProfileChangeEvent event) {
        if (event.getEntityId() == memberProfileModel.getMyEntityId()) {
            memberProfileView.displayProfile(EntityManager.getInstance(MemberProfileActivity.this).getMe().getUser());
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
        try {
            memberProfileModel.updateProfileEmail(email);
            memberProfileView.updateProfileSucceed();
            memberProfileView.successUpdateEmailColor();
        } catch (RetrofitError e) {
            memberProfileView.updateProfileFailed();
        }
    }

    @Background
    void onUpdateProfile(ProgressDialog progressDialog) {
        // TODO Refactoring...

        memberProfileView.showProgressWheel();

        try {
            ResLeftSideMenu entitiesInfo = EntityClientManager_.getInstance_(MemberProfileActivity.this).getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(MemberProfileActivity.this).upsertLeftSideMenu(entitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entitiesInfo);
            JandiPreference.setBadgeCount(MemberProfileActivity.this, totalUnreadCount);
            BadgeUtils.setBadge(MemberProfileActivity.this, totalUnreadCount);
            EntityManager.getInstance(MemberProfileActivity.this).refreshEntity(entitiesInfo);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

        memberProfileView.dismissProgressWheel();


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

        filePickerViewModel.selectFileSelector(JandiConstants.TYPE_UPLOAD_GALLERY, MemberProfileActivity.this);

    }

    @OnActivityResult(JandiConstants.TYPE_UPLOAD_GALLERY)
    public void onImagePickResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        String filePath = filePickerViewModel.getFilePath(getApplicationContext(), JandiConstants.TYPE_UPLOAD_GALLERY, imageData).get(0);
        if (!TextUtils.isEmpty(filePath)) {

            filePickerViewModel.startUpload(MemberProfileActivity.this, null, -1, filePath, null);
        }
    }

    private String getDistictId() {
        EntityManager entityManager = EntityManager.getInstance(MemberProfileActivity.this);
        return entityManager.getDistictId();
    }
}
