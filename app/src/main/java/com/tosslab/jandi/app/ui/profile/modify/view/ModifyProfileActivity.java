package com.tosslab.jandi.app.ui.profile.modify.view;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.modify.dagger.DaggerModifyProfileComponent;
import com.tosslab.jandi.app.ui.profile.modify.dagger.ModifyProfileModule;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenter;
import com.tosslab.jandi.app.ui.profile.modify.property.dept.DeptPositionActivity;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.NameStatusActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.profile.ProfileLabelView;

import java.io.File;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ModifyProfileActivity extends BaseAppCompatActivity implements ModifyProfilePresenter.View {

    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    public static final int REQUEST_CODE = 1000;
    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQUEST_CHARACTER = 0x11;
    public static final int REQUEST_CROP = 11;

    public static final int REQUEST_GET_JOB_TITLE = 0x21;
    public static final int REQUEST_GET_DEPARTMENT = 0x22;
    public static final int REQUEST_NAME_STATUS = 0x23;

    @Nullable
    @InjectExtra
    boolean adminMode = false;

    @Nullable
    @InjectExtra
    long memberId = -1;

    @Inject
    ModifyProfilePresenter modifyProfilePresenter;

    @Bind(R.id.profile_photo)
    ImageView ivProfilePhoto;
    @Bind(R.id.profile_user_realname)
    ProfileLabelView tvProfileRealName;
    @Bind(R.id.profile_user_status_message)
    ProfileLabelView tvProfileStatusMessage;
    @Bind(R.id.profile_user_email)
    ProfileLabelView tvProfileUserEmail;
    @Bind(R.id.profile_user_phone_number)
    ProfileLabelView tvProfileUserPhone;
    @Bind(R.id.profile_user_division)
    ProfileLabelView tvProfileUserDivision;
    @Bind(R.id.profile_user_position)
    ProfileLabelView tvProfileUserPosition;

    ProgressWheel progressWheel;

    AlertDialog profileChoosedialog = null;

    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);
        Dart.inject(this);
        if (savedInstanceState != null) {
            photoFile = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }
        DaggerModifyProfileComponent.builder()
                .modifyProfileModule(new ModifyProfileModule(this, memberId))
                .build()
                .inject(this);

        initViews();
    }

    void initViews() {
        SprinklrScreenView.sendLog(ScreenViewProperty.PROFILE);

        setupActionBar();

        modifyProfilePresenter.onRequestProfile();

        if (adminMode && memberId > 0) {
            tvProfileUserEmail.setClickable(false);
            tvProfileUserEmail.setTextColorContent(0x96666666);
        }

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
        dismissProgressWheel();
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    @OnClick(R.id.profile_user_status_message)
    void editStatusMessage() {
        startActivityForResult(Henson.with(ModifyProfileActivity.this)
                .gotoNameStatusActivity()
                .memberId(memberId)
                .type(NameStatusActivity.EXTRA_TYPE_STATUS)
                .build(), REQUEST_NAME_STATUS);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Status);
    }

    @OnClick(R.id.profile_user_realname)
    void editName() {
        startActivityForResult(Henson.with(ModifyProfileActivity.this)
                .gotoNameStatusActivity()
                .memberId(memberId)
                .type(NameStatusActivity.EXTRA_TYPE_NAME_FOR_TEAM_PROFILE)
                .build(), REQUEST_NAME_STATUS);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Name);
    }

    @OnClick(R.id.profile_user_phone_number)
    void editPhoneNumber() {
        // 핸드폰 번호
        if (NetworkCheckUtil.isConnected()) {

            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                    tvProfileUserPhone.getContent()
            );
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.PhoneNumber);
        }
    }

    @OnClick(R.id.profile_user_division)
    void editDivision() {
        // 부서
        Intent intent = new Intent(this, DeptPositionActivity.class);
        intent.putExtra(DeptPositionActivity.EXTRA_INPUT_MODE,
                DeptPositionActivity.EXTRA_DEPARTMENT_MODE);
        if (!TextUtils.isEmpty(tvProfileUserDivision.getContent())) {
            intent.putExtra(DeptPositionActivity.EXTRA_DEFAULT, tvProfileUserDivision.getContent());
        }
        startActivityForResult(intent, REQUEST_GET_DEPARTMENT);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Department);
    }

    @OnClick(R.id.profile_user_position)
    void editPosition() {
        // 직책
        Intent intent = new Intent(this, DeptPositionActivity.class);
        intent.putExtra(DeptPositionActivity.EXTRA_INPUT_MODE,
                DeptPositionActivity.EXTRA_JOB_TITLE_MODE);
        if (!TextUtils.isEmpty(tvProfileUserPosition.getContent())) {
            intent.putExtra(DeptPositionActivity.EXTRA_DEFAULT, tvProfileUserPosition.getContent());
        }
        startActivityForResult(intent, REQUEST_GET_JOB_TITLE);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.JobTitle);
    }

    @OnClick(R.id.profile_user_email)
    void editEmail() {
        if (NetworkCheckUtil.isConnected()) {
            modifyProfilePresenter.onEditEmailClick();
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Email);
    }

    @OnClick(R.id.profile_photo)
    void getPicture() {
        // 프로필 사진
        Permissions.getChecker()
                .activity(ModifyProfileActivity.this)
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() -> {
                    showProfileChooseDialog();
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile,
                            AnalyticsValue.Action.PhotoEdit);
                })
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(ModifyProfileActivity.this, permissions, REQ_STORAGE_PERMISSION);
                })
                .check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(ModifyProfileActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::getPicture)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(ModifyProfileActivity.this);
                })
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    public void onEvent(MemberEmailChangeEvent event) {
        if (NetworkCheckUtil.isConnected()) {
            String email = event.getEmail();
            updateEmailTextColor(email);

            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.email = email;
            modifyProfilePresenter.onUpdateProfile(reqUpdateProfile);
        }
    }

    public void onEvent(ProfileChangeEvent event) {
        modifyProfilePresenter.onProfileChange(new User(event.getMember()));
    }

    @Override
    public void closeDialogFragment() {
        android.app.Fragment dialogFragment = getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null && dialogFragment instanceof DialogFragment) {
            ((DialogFragment) dialogFragment).dismiss();
        }
    }

    public void onEvent(ConfirmModifyProfileEvent event) {
        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        updateProfileTextColor(event.actionType, event.inputMessage);
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
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
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME:
                reqUpdateProfile.name = event.inputMessage;
                break;
        }
        modifyProfilePresenter.onUpdateProfile(reqUpdateProfile);
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(getString(event.errorMessageResId));
    }

    public void onImageCropResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        Uri output = imageData.getParcelableExtra("output");

        String filePath = output.getPath();
        if (!TextUtils.isEmpty(filePath)) {
            modifyProfilePresenter.onStartUpload(ModifyProfileActivity.this, filePath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                onCameraActivityResult(resultCode, data);
                break;
            case REQUEST_CHARACTER:
                onCharacterActivityResult(resultCode);
                break;
            case REQUEST_CROP:
                onImageCropResult(resultCode, data);
                break;
            case REQUEST_GET_JOB_TITLE:
                onGetJobTitle(resultCode, data);
                break;
            case REQUEST_GET_DEPARTMENT:
                onGetDepartmentResult(resultCode, data);
                break;
            case REQUEST_NAME_STATUS:
                modifyProfilePresenter.onRequestProfile();
                break;
        }
    }

    private void onGetJobTitle(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String jobTitle = data.getStringExtra(DeptPositionActivity.RESULT_EXTRA);
            tvProfileUserPosition.setTextContent(jobTitle);
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.position = jobTitle;
            modifyProfilePresenter.onUpdateProfile(reqUpdateProfile);
        }
    }

    private void onGetDepartmentResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String department = data.getStringExtra(DeptPositionActivity.RESULT_EXTRA);
            tvProfileUserDivision.setTextContent(department);
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.department = department;
            modifyProfilePresenter.onUpdateProfile(reqUpdateProfile);
        }
    }

    void onCameraActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        if (modifyProfilePresenter.getFilePath() != null) {
            modifyProfilePresenter.onStartUpload(ModifyProfileActivity.this, modifyProfilePresenter.getFilePath().getPath());
        } else {
            modifyProfilePresenter.onStartUpload(ModifyProfileActivity.this, photoFile.getPath());
        }
    }

    void onCharacterActivityResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        if (modifyProfilePresenter.getFilePath() != null) {
            modifyProfilePresenter.onStartUpload(ModifyProfileActivity.this, modifyProfilePresenter.getFilePath().getPath());
        } else {
            modifyProfilePresenter.onStartUpload(ModifyProfileActivity.this, photoFile.getPath());
        }
    }


    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(ModifyProfileActivity.this);
        }
        progressWheel.show();
    }

    @Override
    public void displayProfile(User user) {
        // 프로필 사진
        displayProfileImage(user.getPhotoUrl());
        // 프로필 이름
        tvProfileRealName.setTextContent(user.getName());
        // 상태 메시지
        tvProfileStatusMessage.setTextContent(user.getStatusMessage());

        // 이메일
        tvProfileUserEmail.setTextContent(user.getEmail());

        // 폰넘버
        tvProfileUserPhone.setTextContent(user.getPhoneNumber());
        // 부서
        tvProfileUserDivision.setTextContent(user.getDivision());
        // 직책
        tvProfileUserPosition.setTextContent(user.getPosition());
    }

    void displayProfileImage(String profileImageUrlPath) {
        if (!TextUtils.isEmpty(profileImageUrlPath) && !isFinishing()) {
            ImageUtil.loadProfileImage(ivProfilePhoto, profileImageUrlPath, R.drawable.profile_img);
        }
    }

    @Override
    public void dismissProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    private void updateProfileTextColor(int actionType, String inputMessage) {
        switch (actionType) {
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS:
                tvProfileStatusMessage.setTextContent(inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE:
                tvProfileUserPhone.setTextContent(inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION:
                tvProfileUserDivision.setTextContent(inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION:
                tvProfileUserPosition.setTextContent(inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME:
                tvProfileRealName.setTextContent(inputMessage);
                break;
            default:
                break;
        }
    }

    public void launchEditDialog(int dialogActionType, String text) {
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, text);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void updateProfileSucceed() {
        ColoredToast.show(JandiApplication.getContext()
                .getString(R.string.jandi_profile_update_succeed));
    }

    @Override
    public void updateProfileFailed() {
        ColoredToast.showError(JandiApplication.getContext()
                .getString(R.string.err_profile_update));
    }


    @Override
    public void showEmailChooseDialog(String[] emails, String currentEmail) {
        int checkedIdx = 0;

        for (int idx = 0; idx < emails.length; idx++) {

            if (TextUtils.equals(emails[idx], currentEmail)) {
                checkedIdx = idx;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyProfileActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_choose_email)
                .setSingleChoiceItems(emails, checkedIdx, null)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int checkedItemPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        EventBus.getDefault().post(new MemberEmailChangeEvent(emails[checkedItemPosition]));
                    }
                })
                .create().show();
    }

    public void updateEmailTextColor(String email) {
        tvProfileUserEmail.setTextContent(email);
    }

    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(ModifyProfileActivity.this, null);
    }

    public void initProfileChooseDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_profile_image_selector, null);
        profileChoosedialog = new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(view)
                .setNegativeButton(this.getResources().getString(R.string.jandi_cancel),
                        (dialog, id) -> dialog.dismiss())
                .create();

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(this.getResources().getString(R.string.jandi_member_profile_edit));

        view.findViewById(R.id.tv_from_galary).setOnClickListener(v -> {
            modifyProfilePresenter.onRequestCropImage(ModifyProfileActivity.this);
            profileChoosedialog.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile,
                    AnalyticsValue.Action.PhotoEdit, AnalyticsValue.Label.PhotoLibrary);

        });
        view.findViewById(R.id.tv_from_camera).setOnClickListener(v -> {
            modifyProfilePresenter.onRequestCamera(ModifyProfileActivity.this);
            profileChoosedialog.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile,
                    AnalyticsValue.Action.PhotoEdit, AnalyticsValue.Label.Camera);
        });
        view.findViewById(R.id.tv_from_character).setOnClickListener(v -> {
            modifyProfilePresenter.onRequestCharacter(ModifyProfileActivity.this);
            profileChoosedialog.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile,
                    AnalyticsValue.Action.PhotoEdit, AnalyticsValue.Label.ChooseCharacter);
        });

    }

    public void showProfileChooseDialog() {
        if (profileChoosedialog == null) {
            initProfileChooseDialog();
        }
        profileChoosedialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (modifyProfilePresenter.getFilePath() != null) {
            outState.putSerializable(EXTRA_NEW_PHOTO_FILE, modifyProfilePresenter.getFilePath());
        }
    }

}
