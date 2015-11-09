package com.tosslab.jandi.app.ui.profile.modify;

import android.Manifest;
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
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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
public class ModifyProfileActivity extends BaseAppCompatActivity {
    public static final int REQUEST_CODE = 1000;
    public static final int REQ_STORAGE_PERMISSION = 101;

    @Bean
    ModifyProfileModel modifyProfileModel;

    @Bean
    ModifyProfilePresenter memberProfileView;

    @Bean(ProfileFileUploadViewModelImpl.class)
    FilePickerViewModel filePickerViewModel;

    @AfterViews
    void bindAdapter() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.PROFILE)
                        .build());

        setupActionBar();

        getProfileInBackground();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.EditProfile);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
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

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
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
                me = modifyProfileModel.getSavedProfile(JandiApplication.getContext());
            } else {
                me = modifyProfileModel.getProfile();
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

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Status);
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
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Mobile);
        }
    }

    @Click(R.id.profile_user_realname)
    void editName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                    ((TextView) view)
            );
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Name);
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
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Division);
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
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Position);
        }
    }

    @Click(R.id.profile_user_email)
    void editEmail(View view) {
        if (NetworkCheckUtil.isConnected()) {
            String[] accountEmails = modifyProfileModel.getAccountEmails();
            String email = memberProfileView.getEmail();
            memberProfileView.showEmailChooseDialog(accountEmails, email);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Email);
        }
    }

    @Click(R.id.profile_photo)
    void getPicture() {
        // 프로필 사진

        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() -> {
                    filePickerViewModel.selectFileSelector(FilePickerViewModel.TYPE_UPLOAD_GALLERY,
                            ModifyProfileActivity.this);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile,
                            AnalyticsValue.Action.PhotoEdit);
                })
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, REQ_STORAGE_PERMISSION);
                })
                .check();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Permissions.getResult()
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::getPicture)
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    public void onEvent(MemberEmailChangeEvent event) {
        if (NetworkCheckUtil.isConnected()) {
            memberProfileView.updateEmailTextColor(event.getEmail());
            uploadEmail(event.getEmail());
        }
    }

    public void onEvent(ProfileChangeEvent event) {
        ResLeftSideMenu.User member = event.getMember();
        if (modifyProfileModel.isMyId(member.id)) {
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
            memberProfileView.showCheckNetworkDialog();
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
            ResLeftSideMenu.User me = modifyProfileModel.updateProfile(reqUpdateProfile);
            memberProfileView.updateProfileSucceed();
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
            modifyProfileModel.updateProfileName(new ReqProfileName(name));
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
            memberProfileView.showCheckNetworkDialog();
            return;
        }

        try {
            modifyProfileModel.updateProfileEmail(email);
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
        UnLockPassCodeManager.getInstance().setUnLocked(true);

        if (resultCode != RESULT_OK) {
            return;
        }

        String filePath = filePickerViewModel.getFilePath(getApplicationContext(), FilePickerViewModel.TYPE_UPLOAD_GALLERY, imageData).get(0);
        if (!TextUtils.isEmpty(filePath) && isJpgOrPng(filePath)) {
            try {
                // fileExt = .jpg or .png
                String fileExt = filePath.substring(filePath.lastIndexOf("."), filePath.length());
                Crop.of(Uri.fromFile(new File(filePath)),
                        Uri.fromFile(File.createTempFile("temp_", fileExt,
                                new File(GoogleImagePickerUtil.getDownloadPath()))))
                        .asSquare()
                        .start(ModifyProfileActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ColoredToast.showWarning(ModifyProfileActivity.this, getString(R.string.jandi_unsupported_type_picture));
        }

    }

    private boolean isJpgOrPng(String filePath) {
        String filePathLowerCase = filePath.toLowerCase();
        return filePathLowerCase.endsWith("png") ||
                filePathLowerCase.endsWith("jpg") ||
                filePathLowerCase.endsWith("jpeg");
    }

    @OnActivityResult(Crop.REQUEST_CROP)
    public void onImageCropResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            memberProfileView.showCheckNetworkDialog();
            return;
        }

        Uri output = Crop.getOutput(imageData);

        String filePath = output.getPath();
        if (!TextUtils.isEmpty(filePath)) {
            filePickerViewModel.startUpload(ModifyProfileActivity.this, null, -1, filePath, null);
        }
    }

    private String getDistictId() {
        EntityManager entityManager = EntityManager.getInstance();
        return entityManager.getDistictId();
    }
}
