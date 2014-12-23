package com.tosslab.jandi.app.ui.profile.member;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URLConnection;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EActivity(R.layout.activity_profile)
public class MemberProfileActivity extends BaseAnalyticsActivity {
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final String TEMP_PHOTO_FILE = "temp.png";   // 임시 저장파일
    private final Logger log = Logger.getLogger(MemberProfileActivity.class);
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

    @Bean
    JandiEntityClient mJandiEntityClient;


    private Context mContext;
    private ProgressWheel mProgressWheel;

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

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        getProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_update_profile:
                onUpdateProfile();
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
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    /**
     * *********************************************************
     * 프로필 가져오기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    @UiThread
    void getProfile() {
        mProgressWheel.show();
        getProfileInBackground();
    }

    @Background
    void getProfileInBackground() {
        try {
            EntityManager entityManager = ((JandiApplication) getApplication()).getEntityManager();
            ResLeftSideMenu.User me = mJandiEntityClient.getUserProfile(entityManager.getMe().getId());
            getProfileSuccess(me);
        } catch (JandiNetworkException e) {
            log.error("get profile failed", e);
            getProfileFailed();
        } catch (Exception e) {
            log.error("get profile failed", e);
            getProfileFailed();
        }
    }

    @UiThread
    void getProfileSuccess(ResLeftSideMenu.User me) {
        mProgressWheel.dismiss();
        attemptToUpdate = false;
        attemptToUpdatePhoto = false;
        displayProfile(me);
    }

    @UiThread
    void getProfileFailed() {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    void displayProfile(ResLeftSideMenu.User user) {
        // 프로필 사진
        Glide.with(this)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + (!TextUtils.isEmpty(user.u_photoThumbnailUrl.largeThumbnailUrl) ? user.u_photoThumbnailUrl.largeThumbnailUrl : user.u_photoUrl))
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(this))
                .skipMemoryCache(true)              // 메모리 캐시를 쓰지 않는다.
                .into(imageViewProfilePhoto);
        // 프로필 이름
        textViewProfileRealName.setText(user.name);
        // 상태 메시지
        String strStatus = (user.u_statusMessage);
        if (!TextUtils.isEmpty(strStatus)) {
            textViewProfileStatusMessage.setText(strStatus);
            textViewProfileStatusMessage.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileStatusMessage.setText(R.string.jandi_profile_optional);
            textViewProfileStatusMessage.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }

        // 이메일
        textViewProfileUserEmail.setText(user.u_email);
        // 폰넘버
        String strPhone = (user.u_extraData.phoneNumber);
        if (!TextUtils.isEmpty(strPhone)) {
            textViewProfileUserPhone.setText(strPhone);
            textViewProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPhone.setText(R.string.jandi_profile_optional);
            textViewProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
        // 부서
        String strDivision = (user.u_extraData.department);
        if (!TextUtils.isEmpty(strDivision)) {
            textViewProfileUserDivision.setText(strDivision);
            textViewProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserDivision.setText(R.string.jandi_profile_optional);
            textViewProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
        // 직책
        String strPosition = user.u_extraData.position;
        if (!TextUtils.isEmpty(strPosition)) {
            textViewProfileUserPosition.setText(strPosition);
            textViewProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPosition.setText(R.string.jandi_profile_optional);
            textViewProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
    }

    /**
     * *********************************************************
     * 프로필 수정
     * **********************************************************
     */
    @Click(R.id.profile_user_status_message)
    void editStatusMessage() {
        // 닉네임
        launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS,
                textViewProfileStatusMessage
        );
    }

    @Click(R.id.profile_user_phone_number)
    void editPhoneNumber() {
        // 핸드폰 번호
        launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                textViewProfileUserPhone
        );
    }

    @Click(R.id.profile_user_division)
    void editDivision() {
        // 부서
        launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION,
                textViewProfileUserDivision
        );
    }

    @Click(R.id.profile_user_position)
    void editPosition() {
        // 직책
        launchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                textViewProfileUserPosition
        );
    }

    private void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        // 현재 Text가 미지정 된 경우 빈칸으로 바꿔서 입력 받는다.
        if (currentText.equals(getString(R.string.jandi_profile_optional))) {
            currentText = "";
        }
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(ConfirmModifyProfileEvent event) {
        switch (event.actionType) {
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_STATUS:
                setTextAndChangeColor(textViewProfileStatusMessage, event.inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE:
                setTextAndChangeColor(textViewProfileUserPhone, event.inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION:
                setTextAndChangeColor(textViewProfileUserDivision, event.inputMessage);
                break;
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION:
                setTextAndChangeColor(textViewProfileUserPosition, event.inputMessage);
                break;
            default:
                break;
        }
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(this, getString(event.errorMessageResId));
    }

    @UiThread
    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        if (textToBeChanged.length() <= 0) {
            ColoredToast.showError(this, getString(R.string.err_profile_empty_info));
            return;
        }
        textView.setText(textToBeChanged);
        textView.setTextColor(getResources().getColor(R.color.jandi_profile_edited_text));
        attemptToUpdate = true;
    }

    @UiThread
    void onUpdateProfile() {
        if (attemptToUpdatePhoto) {
            uploadProfilePhoto();
        } else if (attemptToUpdate) {
            updateProfile();
        } else {
            ColoredToast.showWarning(this, getString(R.string.err_profile_unmodified));
            return;
        }
    }

    @UiThread
    void updateProfile() {
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = textViewProfileStatusMessage.getText().toString();
        reqUpdateProfile.phoneNumber = getOptionalString(textViewProfileUserPhone);
        reqUpdateProfile.department = getOptionalString(textViewProfileUserDivision);
        reqUpdateProfile.position = getOptionalString(textViewProfileUserPosition);
        updateProfileInBackground(reqUpdateProfile);
    }

    private String getOptionalString(TextView textView) {
        if (textView.getText().toString().equals(getString(R.string.jandi_profile_optional))) {
            return "";
        } else {
            return textView.getText().toString();
        }
    }

    @Background
    void updateProfileInBackground(ReqUpdateProfile reqUpdateProfile) {
        try {
            EntityManager entityManager = ((JandiApplication) getApplicationContext()).getEntityManager();
            ResLeftSideMenu.User me = mJandiEntityClient.updateUserProfile(entityManager.getMe().getId(), reqUpdateProfile);
            updateProfileSucceed(me);
        } catch (JandiNetworkException e) {
            log.error("get profile failed", e);
            updateProfileFailed();
        }
    }

    @UiThread
    void updateProfileSucceed(ResLeftSideMenu.User user) {
        ColoredToast.show(this, getString(R.string.jandi_profile_update_succeed));
        trackUpdateProfile(getDistictId(), user);
        getProfileSuccess(user);
    }

    @UiThread
    void updateProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_update));
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
        intent.putExtra("outputFormat",         // 포맷방식
                Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    /**
     * 다시 액티비티로 복귀하였을때 이미지를 셋팅
     */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        attemptToUpdatePhoto = true;
                        mTempPhotoFile = new File(ImageFilePath.getPath(MemberProfileActivity.this, imageData.getData()));
                        Glide.with(this)
                                .load(mTempPhotoFile)
                                .placeholder(R.drawable.jandi_profile)
                                .transform(new GlideCircleTransform(this))
                                .skipMemoryCache(true)              // 메모리 캐시를 쓰지 않는다.
                                .into(imageViewProfilePhoto);
                    }
                }
                break;
        }
    }

    // File Upload 확인 이벤트 획득
    public void uploadProfilePhoto() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.jandi_file_uploading));
        progressDialog.show();

        EntityManager entityManager = ((JandiApplication) getApplication()).getEntityManager();

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/members/" + entityManager.getMe().getId() + "/profile/photo";

        Ion.with(mContext)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(mContext).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(mTempPhotoFile.getName()), mTempPhotoFile)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        progressDialog.dismiss();
                        log.debug("Upload Result : " + result);
                        uploadProfilePhotoDone(e, result);
                    }
                });
    }

    @UiThread
    void uploadProfilePhotoDone(Exception exception, String result) {
        if (exception == null) {
            log.debug(result);
            attemptToUpdatePhoto = false;
            if (attemptToUpdate) {
                updateProfile();
            } else {
                ColoredToast.show(mContext, getString(R.string.jandi_profile_photo_upload_succeed));
            }

        } else {
            log.error("uploadFileDone: FAILED", exception);
            ColoredToast.showError(mContext, getString(R.string.err_profile_photo_upload));
        }
    }

    private String getDistictId() {
        EntityManager entityManager = ((JandiApplication) getApplication()).getEntityManager();
        return entityManager.getDistictId();
    }
}
