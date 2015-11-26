package com.tosslab.jandi.app.ui.profile.modify.view;

import android.Manifest;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenter;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenterImpl;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.transform.glide.GlideCircleTransform;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_profile)
public class ModifyProfileActivity extends BaseAppCompatActivity implements ModifyProfilePresenter.View {
    public static final int REQUEST_CODE = 1000;
    public static final int REQ_STORAGE_PERMISSION = 101;

    @Bean(ModifyProfilePresenterImpl.class)
    ModifyProfilePresenter memberProfilePresenter;

    @ViewById(R.id.profile_photo)
    ImageView imageViewProfilePhoto;
    @ViewById(R.id.profile_user_realname)
    TextView textViewProfileRealName;
    @ViewById(R.id.profile_user_status_message)
    TextView textViewProfileStatusMessage;
    @ViewById(R.id.profile_user_email)
    TextView textViewProfileUserEmail;
    @ViewById(R.id.profile_user_phone_number)
    TextView textViewProfileUserPhone;
    @ViewById(R.id.profile_user_division)
    TextView textViewProfileUserDivision;
    @ViewById(R.id.profile_user_position)
    TextView textViewProfileUserPosition;
    ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        memberProfilePresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.PROFILE)
                        .build());

        setupActionBar();

        memberProfilePresenter.onRequestProfile();

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
    @Click(R.id.profile_user_status_message)
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

    @Click(R.id.profile_user_phone_number)
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

    @Click(R.id.profile_user_realname)
    void editName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
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
            launchEditDialog(
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
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                    ((TextView) view)
            );
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Position);
        }
    }

    @Click(R.id.profile_user_email)
    void editEmail(View view) {
        if (NetworkCheckUtil.isConnected()) {
            memberProfilePresenter.onEditEmailClick(getEmail());

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditProfile, AnalyticsValue.Action.Email);
        }
    }

    @Click(R.id.profile_photo)
    void getPicture() {
        // 프로필 사진

        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() -> {
                    memberProfilePresenter.onRequestCropImage(ModifyProfileActivity.this);
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
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::getPicture)
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    public void onEvent(MemberEmailChangeEvent event) {
        if (NetworkCheckUtil.isConnected()) {
            updateEmailTextColor(event.getEmail());
            memberProfilePresenter.onUploadEmail(event.getEmail());
        }
    }

    public void onEvent(ProfileChangeEvent event) {
        memberProfilePresenter.onProfileChange(event.getMember());

    }

    @UiThread
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
        if (event.actionType == EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME) {
            memberProfilePresenter.updateProfileName(event.inputMessage);
        } else {
            ReqUpdateProfile reqUpdateProfile = getUpdateProfile();
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

            memberProfilePresenter.onUpdateProfileExtraInfo(reqUpdateProfile);
        }
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    @OnActivityResult(Crop.REQUEST_CROP)
    public void onImageCropResult(int resultCode, Intent imageData) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        Uri output = Crop.getOutput(imageData);

        String filePath = output.getPath();
        if (!TextUtils.isEmpty(filePath)) {
            memberProfilePresenter.onStartUpload(ModifyProfileActivity.this, filePath);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailProfile() {

        ColoredToast.showError(ModifyProfileActivity.this, JandiApplication.getContext().getString(R.string.err_profile_get_info));
        finish();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showProgressWheel() {

        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(ModifyProfileActivity.this);
        }
        progressWheel.show();
    }

    @Override
    @UiThread
    public void displayProfile(ResLeftSideMenu.User user) {
        // 프로필 사진

        String profileImageUrlPath = null;
        if (user.u_photoThumbnailUrl != null) {
            profileImageUrlPath = !TextUtils.isEmpty(user.u_photoThumbnailUrl.largeThumbnailUrl) ? user.u_photoThumbnailUrl.largeThumbnailUrl : user.u_photoUrl;
        } else if (!TextUtils.isEmpty(user.u_photoUrl)) {
            profileImageUrlPath = user.u_photoUrl;
        }

        displayProfileImage(profileImageUrlPath);
        // 프로필 이름
        textViewProfileRealName.setText(user.name);
        // 상태 메시지
        String strStatus = (user.u_statusMessage);
        if (!TextUtils.isEmpty(strStatus)) {
            textViewProfileStatusMessage.setText(strStatus);
            textViewProfileStatusMessage.setTextColor(getResources().getColor(R.color.jandi_text));
        }

        // 이메일
        textViewProfileUserEmail.setText(user.u_email);
        // 폰넘버
        String strPhone = (user.u_extraData.phoneNumber);
        if (!TextUtils.isEmpty(strPhone)) {
            textViewProfileUserPhone.setText(strPhone);
            textViewProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text));
        }
        // 부서
        String strDivision = (user.u_extraData.department);
        if (!TextUtils.isEmpty(strDivision)) {
            textViewProfileUserDivision.setText(strDivision);
            textViewProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text));
        }
        // 직책
        String strPosition = user.u_extraData.position;
        if (!TextUtils.isEmpty(strPosition)) {
            textViewProfileUserPosition.setText(strPosition);
            textViewProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text));
        }
    }

    void displayProfileImage(String profileImageUrlPath) {
        if (!TextUtils.isEmpty(profileImageUrlPath)
                && !isFinishing()) {
            Glide.with(ModifyProfileActivity.this)
                    .load(profileImageUrlPath)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .transform(new GlideCircleTransform(ModifyProfileActivity.this))
                    .into(imageViewProfilePhoto);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public ReqUpdateProfile getUpdateProfile() {
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = textViewProfileStatusMessage.getText().toString();
        reqUpdateProfile.phoneNumber = textViewProfileUserPhone.getText().toString();
        reqUpdateProfile.department = textViewProfileUserDivision.getText().toString();
        reqUpdateProfile.position = textViewProfileUserPosition.getText().toString();
        return reqUpdateProfile;
    }

    public void updateProfileTextColor(int actionType, String inputMessage) {
        switch (actionType) {
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS:
                setTextAndChangeColor(textViewProfileStatusMessage, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE:
                setTextAndChangeColor(textViewProfileUserPhone, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION:
                setTextAndChangeColor(textViewProfileUserDivision, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION:
                setTextAndChangeColor(textViewProfileUserPosition, inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME:
                setTextAndChangeColor(textViewProfileRealName, inputMessage);
                break;
            default:
                break;
        }
    }

    @UiThread
    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        textView.setText(textToBeChanged);
    }

    @UiThread
    public void updateLocalProfileImage(File mTempPhotoFile) {
        Glide.with(ModifyProfileActivity.this)
                .load(mTempPhotoFile)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new GlideCircleTransform(ModifyProfileActivity.this))
                .into(imageViewProfilePhoto);

    }


    public void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @UiThread
    public void showToastNoUpdateProfile() {
        ColoredToast.showWarning(ModifyProfileActivity.this, JandiApplication.getContext()
                .getString(R.string.err_profile_unmodified));
    }

    @UiThread
    public void showPhotoUploadProgressDialog(ProgressDialog progressDialog) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(JandiApplication.getContext().getString(R.string.jandi_file_uploading));
        progressDialog.show();

    }

    @UiThread
    public void dismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @UiThread
    public void successPhotoUpload() {
        ColoredToast.show(ModifyProfileActivity.this, JandiApplication.getContext()
                .getString(R.string.jandi_profile_photo_upload_succeed));

    }

    @UiThread
    public void failPhotoUpload() {
        ColoredToast.showError(ModifyProfileActivity.this, JandiApplication.getContext()
                .getString(R.string.err_profile_photo_upload));

    }

    @UiThread
    public void updateProfileSucceed() {
        ColoredToast.show(ModifyProfileActivity.this, JandiApplication.getContext()
                .getString(R.string.jandi_profile_update_succeed));
    }

    @UiThread
    public void updateProfileFailed() {
        ColoredToast.showError(ModifyProfileActivity.this, JandiApplication.getContext()
                .getString(R.string.err_profile_update));

    }


    public String getName() {
        return textViewProfileRealName.getText().toString();
    }

    @UiThread
    public void successUpdateNameColor() {
        textViewProfileRealName.setTextColor(JandiApplication.getContext().getResources().getColor(R.color.jandi_text));

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

    public String getEmail() {

        return textViewProfileUserEmail.getText().toString();
    }

    @UiThread
    public void updateEmailTextColor(String email) {
        setTextAndChangeColor(textViewProfileUserEmail, email);
    }

    @UiThread
    public void successUpdateEmailColor() {
        textViewProfileUserEmail.setTextColor(JandiApplication.getContext().getResources().getColor(R.color.jandi_text));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(ModifyProfileActivity.this, null);
    }

}
