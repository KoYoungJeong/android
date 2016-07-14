package com.tosslab.jandi.app.ui.profile.modify.view;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.tosslab.jandi.app.ui.profile.inputlist.InputProfileListActivity;
import com.tosslab.jandi.app.ui.profile.modify.dagger.DaggerModifyProfileComponent;
import com.tosslab.jandi.app.ui.profile.modify.dagger.ModifyProfileModule;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenter;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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

    @Inject
    ModifyProfilePresenter modifyProfilePresenter;

    @Bind(R.id.profile_photo)
    ImageView ivProfilePhoto;
    @Bind(R.id.profile_user_realname)
    TextView tvProfileRealName;
    @Bind(R.id.profile_user_status_message)
    TextView tvProfileStatusMessage;
    @Bind(R.id.profile_user_email)
    TextView tvProfileUserEmail;
    @Bind(R.id.profile_user_phone_number)
    TextView tvProfileUserPhone;
    @Bind(R.id.profile_user_division)
    TextView tvProfileUserDivision;
    @Bind(R.id.profile_user_position)
    TextView tvProfileUserPosition;

    ProgressWheel progressWheel;

    AlertDialog profileChoosedialog = null;

    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (savedInstanceState != null) {
            photoFile = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }

        ButterKnife.bind(this);

        DaggerModifyProfileComponent.builder()
                .modifyProfileModule(new ModifyProfileModule(this))
                .build()
                .inject(this);

        initViews();
    }

    void initViews() {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.ScreenView)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ScreenView, ScreenViewProperty.PROFILE)
                .build());

        setupActionBar();

        modifyProfilePresenter.onRequestProfile();

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

    /**
     * *********************************************************
     * 프로필 수정
     * **********************************************************
     */
    @OnClick(R.id.profile_user_status_message)
    void editStatusMessage(View view) {
        // 닉네임
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS,
                    ((TextView) view)
            );

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Status);
        }
    }

    @OnClick(R.id.profile_user_phone_number)
    void editPhoneNumber(View view) {
        // 핸드폰 번호
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                    ((TextView) view)
            );
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Mobile);
        }
    }

    @OnClick(R.id.profile_user_realname)
    void editName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                    ((TextView) view)
            );
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Name);
        }
    }

    @OnClick(R.id.profile_user_division)
    void editDivision() {
        // 부서
        Intent intent = new Intent(this, InputProfileListActivity.class);
        intent.putExtra(InputProfileListActivity.EXTRA_INPUT_MODE,
                InputProfileListActivity.EXTRA_DEPARTMENT_MODE);
        if (!TextUtils.isEmpty(tvProfileUserDivision.getText().toString())) {
            intent.putExtra(InputProfileListActivity.EXTRA_DEFAULT,
                    tvProfileUserDivision.getText().toString());
        }
        startActivityForResult(intent, REQUEST_GET_DEPARTMENT);
    }

    @OnClick(R.id.profile_user_position)
    void editPosition() {
        // 직책
        Intent intent = new Intent(this, InputProfileListActivity.class);
        intent.putExtra(InputProfileListActivity.EXTRA_INPUT_MODE,
                InputProfileListActivity.EXTRA_JOB_TITLE_MODE);
        if (!TextUtils.isEmpty(tvProfileUserPosition.getText().toString())) {
            intent.putExtra(InputProfileListActivity.EXTRA_DEFAULT,
                    tvProfileUserPosition.getText().toString());
        }
        startActivityForResult(intent, REQUEST_GET_JOB_TITLE);
    }

    @OnClick(R.id.profile_user_email)
    void editEmail(View view) {
        if (NetworkCheckUtil.isConnected()) {
            modifyProfilePresenter.onEditEmailClick(getEmail());
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Email);
        }
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
        }
    }

    private void onGetJobTitle(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String jobTitle = data.getStringExtra(InputProfileListActivity.RESULT_EXTRA);
            tvProfileUserPosition.setText(jobTitle);
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.position = jobTitle;
            modifyProfilePresenter.onUpdateProfile(reqUpdateProfile);
        }
    }

    private void onGetDepartmentResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String department = data.getStringExtra(InputProfileListActivity.RESULT_EXTRA);
            tvProfileUserDivision.setText(department);
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

        String profileImageUrlPath = user.getPhotoUrl();

        displayProfileImage(profileImageUrlPath);
        // 프로필 이름
        tvProfileRealName.setText(user.getName());
        // 상태 메시지
        String strStatus = (user.getStatusMessage());
        if (!TextUtils.isEmpty(strStatus)) {
            tvProfileStatusMessage.setText(strStatus);
            tvProfileStatusMessage.setTextColor(getResources().getColor(R.color.jandi_text));
        }

        // 이메일
        tvProfileUserEmail.setText(user.getEmail());

        // 폰넘버
        String strPhone = (user.getPhoneNumber());
        if (!TextUtils.isEmpty(strPhone)) {
            tvProfileUserPhone.setText(strPhone);
            tvProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text));
        }
        // 부서
        String strDivision = (user.getDivision());
        if (!TextUtils.isEmpty(strDivision)) {
            tvProfileUserDivision.setText(strDivision);
            tvProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text));
        }
        // 직책
        String strPosition = user.getPosition();
        if (!TextUtils.isEmpty(strPosition)) {
            tvProfileUserPosition.setText(strPosition);
            tvProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text));
        }
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
                setTextAndChangeColor(tvProfileStatusMessage, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE:
                setTextAndChangeColor(tvProfileUserPhone, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION:
                setTextAndChangeColor(tvProfileUserDivision, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION:
                setTextAndChangeColor(tvProfileUserPosition, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME:
                setTextAndChangeColor(tvProfileRealName, inputMessage);
                break;
            default:
                break;
        }
    }

    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        textView.setText(textToBeChanged);
    }

    public void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
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

    private String getEmail() {

        return tvProfileUserEmail.getText().toString();
    }

    public void updateEmailTextColor(String email) {
        setTextAndChangeColor(tvProfileUserEmail, email);
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

        });
        view.findViewById(R.id.tv_from_camera).setOnClickListener(v -> {
            modifyProfilePresenter.onRequestCamera(ModifyProfileActivity.this);
            profileChoosedialog.dismiss();
        });
        view.findViewById(R.id.tv_from_character).setOnClickListener(v -> {
            modifyProfilePresenter.onRequestCharacter(ModifyProfileActivity.this);
            profileChoosedialog.dismiss();
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
