package com.tosslab.jandi.app.ui.profile.member;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.MemberEmailChangeEvent;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 31..
 */
@EBean
public class MemberProfilePresenter {

    @RootContext
    Activity activity;

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


    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @UiThread
    public void showProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

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
            textViewProfileStatusMessage.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        }

        // 이메일
        textViewProfileUserEmail.setText(user.u_email);
        // 폰넘버
        String strPhone = (user.u_extraData.phoneNumber);
        if (!TextUtils.isEmpty(strPhone)) {
            textViewProfileUserPhone.setText(strPhone);
            textViewProfileUserPhone.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        }
        // 부서
        String strDivision = (user.u_extraData.department);
        if (!TextUtils.isEmpty(strDivision)) {
            textViewProfileUserDivision.setText(strDivision);
            textViewProfileUserDivision.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        }
        // 직책
        String strPosition = user.u_extraData.position;
        if (!TextUtils.isEmpty(strPosition)) {
            textViewProfileUserPosition.setText(strPosition);
            textViewProfileUserPosition.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        }
    }

    public void displayProfileImage(String profileImageUrlPath) {
        if (!TextUtils.isEmpty(profileImageUrlPath)) {
            Glide.with(activity)
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileImageUrlPath)
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new GlideCircleTransform(activity))
                    .into(imageViewProfilePhoto);
        }
    }

    public void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
        newFragment.show(activity.getFragmentManager(), "dialog");
    }

    public ReqUpdateProfile getUpdateProfile() {
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = textViewProfileStatusMessage.getText().toString();
        reqUpdateProfile.phoneNumber = textViewProfileUserPhone.getText().toString();
        reqUpdateProfile.department = textViewProfileUserDivision.getText().toString();
        reqUpdateProfile.position = textViewProfileUserPosition.getText().toString();
        return reqUpdateProfile;
    }

    @UiThread
    public void updateLocalProfileImage(File mTempPhotoFile) {
        Glide.with(activity)
                .load(mTempPhotoFile)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(activity))
                .into(imageViewProfilePhoto);

    }

    @UiThread
    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        textView.setText(textToBeChanged);
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
    public void showToastNoUpdateProfile() {
        ColoredToast.showWarning(activity, activity.getString(R.string.err_profile_unmodified));
    }

    @UiThread
    public void showPhotoUploadProgressDialog(ProgressDialog progressDialog) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(activity.getString(R.string.jandi_file_uploading));
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
        ColoredToast.show(activity, activity.getString(R.string.jandi_profile_photo_upload_succeed));

    }

    @UiThread
    public void failPhotoUpload() {
        ColoredToast.showError(activity, activity.getString(R.string.err_profile_photo_upload));

    }

    @UiThread
    public void updateProfileSucceed() {
        ColoredToast.show(activity, activity.getString(R.string.jandi_profile_update_succeed));
    }

    @UiThread
    public void updateProfileFailed() {
        ColoredToast.showError(activity, activity.getString(R.string.err_profile_update));

    }

    @UiThread
    void getProfileFailed() {

        ColoredToast.showError(activity, activity.getString(R.string.err_profile_get_info));
        activity.finish();
    }

    public String getName() {
        return textViewProfileRealName.getText().toString();
    }

    @UiThread
    public void successUpdateNameColor() {
        textViewProfileRealName.setTextColor(activity.getResources().getColor(R.color.jandi_text));

    }

    public void showEmailChooseDialog(String[] emails, String currentEmail) {

        int checkedIdx = 0;

        for (int idx = 0; idx < emails.length; idx++) {

            if (TextUtils.equals(emails[idx], currentEmail)) {
                checkedIdx = idx;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

    public void updateEmailTextColor(String email) {
        setTextAndChangeColor(textViewProfileUserEmail, email);
    }

    @UiThread
    public void successUpdateEmailColor() {
        textViewProfileUserEmail.setTextColor(activity.getResources().getColor(R.color.jandi_text));
    }
}
