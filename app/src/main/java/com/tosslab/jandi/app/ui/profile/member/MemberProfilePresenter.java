package com.tosslab.jandi.app.ui.profile.member;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
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

        dismissProgressWheel();

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
        Glide.with(activity)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + (!TextUtils.isEmpty(user.u_photoThumbnailUrl.largeThumbnailUrl) ? user.u_photoThumbnailUrl.largeThumbnailUrl : user.u_photoUrl))
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(activity))
                .skipMemoryCache(true)              // 메모리 캐시를 쓰지 않는다.
                .into(imageViewProfilePhoto);
        // 프로필 이름
        textViewProfileRealName.setText(user.name);
        // 상태 메시지
        String strStatus = (user.u_statusMessage);
        if (!TextUtils.isEmpty(strStatus)) {
            textViewProfileStatusMessage.setText(strStatus);
            textViewProfileStatusMessage.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileStatusMessage.setText(R.string.jandi_profile_optional);
            textViewProfileStatusMessage.setTextColor(activity.getResources().getColor(R.color.jandi_text_light));
        }

        // 이메일
        textViewProfileUserEmail.setText(user.u_email);
        // 폰넘버
        String strPhone = (user.u_extraData.phoneNumber);
        if (!TextUtils.isEmpty(strPhone)) {
            textViewProfileUserPhone.setText(strPhone);
            textViewProfileUserPhone.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPhone.setText(R.string.jandi_profile_optional);
            textViewProfileUserPhone.setTextColor(activity.getResources().getColor(R.color.jandi_text_light));
        }
        // 부서
        String strDivision = (user.u_extraData.department);
        if (!TextUtils.isEmpty(strDivision)) {
            textViewProfileUserDivision.setText(strDivision);
            textViewProfileUserDivision.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserDivision.setText(R.string.jandi_profile_optional);
            textViewProfileUserDivision.setTextColor(activity.getResources().getColor(R.color.jandi_text_light));
        }
        // 직책
        String strPosition = user.u_extraData.position;
        if (!TextUtils.isEmpty(strPosition)) {
            textViewProfileUserPosition.setText(strPosition);
            textViewProfileUserPosition.setTextColor(activity.getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPosition.setText(R.string.jandi_profile_optional);
            textViewProfileUserPosition.setTextColor(activity.getResources().getColor(R.color.jandi_text_light));
        }
    }

    public void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        if (currentText.equals(activity.getString(R.string.jandi_profile_optional))) {
            currentText = "";
        }
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
        newFragment.show(activity.getFragmentManager(), "dialog");
    }

    private String getOptionalString(TextView textView) {
        if (textView.getText().toString().equals(activity.getString(R.string.jandi_profile_optional))) {
            return "";
        } else {
            return textView.getText().toString();
        }
    }

    public ReqUpdateProfile getUpdateProfile() {
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = textViewProfileStatusMessage.getText().toString();
        reqUpdateProfile.phoneNumber = getOptionalString(textViewProfileUserPhone);
        reqUpdateProfile.department = getOptionalString(textViewProfileUserDivision);
        reqUpdateProfile.position = getOptionalString(textViewProfileUserPosition);
        return reqUpdateProfile;
    }

    public void updateLocalProfileImage(File mTempPhotoFile) {
        Glide.with(activity)
                .load(mTempPhotoFile)
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(activity))
                .skipMemoryCache(true)              // 메모리 캐시를 쓰지 않는다.
                .into(imageViewProfilePhoto);

    }

    @UiThread
    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        if (textToBeChanged.length() <= 0) {
            ColoredToast.showError(activity, activity.getString(R.string.err_profile_empty_info));
            return;
        }
        textView.setText(textToBeChanged);
        textView.setTextColor(activity.getResources().getColor(R.color.jandi_profile_edited_text));
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

}
