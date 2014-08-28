package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EActivity(R.layout.activity_profile)
public class ProfileActivity extends Activity {
    private final Logger log = Logger.getLogger(ProfileActivity.class);

    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final String TEMP_PHOTO_FILE = "temp.png";   // 임시 저장파일

    @Extra
    int myEntityId;
    @ViewById(R.id.profile_photo)
    ImageView imageViewProfilePhoto;
    @ViewById(R.id.profile_user_realname)
    TextView textViewProfileRealName;
    @ViewById(R.id.profile_user_nickname)
    TextView textViewProfileNickName;
    @ViewById(R.id.profile_user_email)
    TextView textViewProfileUserEmail;
    @ViewById(R.id.profile_user_phone_number)
    TextView textViewProfileUserPhone;
    @ViewById(R.id.profile_user_division)
    TextView textViewProfileUserDivision;
    @ViewById(R.id.profile_user_position)
    TextView textViewProfileUserPosition;

    @RestService
    JandiRestClient jandiRestClient;
    private JandiAuthClient mJandiAuthClient;

    private Context mContext;
    private String mMyToken;
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
        actionBar.setTitle("Profile");

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(mContext);
        mJandiAuthClient = new JandiAuthClient(jandiRestClient);
        mJandiAuthClient.setAuthToken(mMyToken);
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

    /************************************************************
     * 프로필 가져오기
     ************************************************************/
    @UiThread
    void getProfile() {
        mProgressWheel.show();
        getProfileInBackground();
    }

    @Background
    void getProfileInBackground() {
        try {
            ResLeftSideMenu.User me = mJandiAuthClient.getUserProfile(myEntityId);
            getProfileSuccess(me);
        } catch (JandiException e) {
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
        ColoredToast.showError(this, "프로필 획득에 실패하였습니다");
    }

    void displayProfile(ResLeftSideMenu.User me) {
        FormattedEntity user = new FormattedEntity(me);
        // 프로필 사진
        Picasso.with(this)
                .load(user.getUserLargeProfileUrl())
                .placeholder(R.drawable.jandi_profile)
                .transform(new CircleTransform())
                .skipMemoryCache()              // 메모리 캐시를 쓰지 않는다.
                .into(imageViewProfilePhoto);
        // 프로필 이름
        textViewProfileRealName.setText(user.getUserName());
        // 닉네임
        textViewProfileNickName.setText(user.getUserNickName());
        textViewProfileNickName.setTextColor(getResources().getColor(R.color.jandi_text));
        // 이메일
        textViewProfileUserEmail.setText(user.getUserEmail());
        // 폰넘버
        String strPhone = (user.getUserPhoneNumber());
        if (strPhone.length() > 0) {
            textViewProfileUserPhone.setText(strPhone);
            textViewProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPhone.setText(R.string.jandi_profile_optional);
            textViewProfileUserPhone.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
        // 부서
        String strDivision = (user.getUserDivision());
        if (strDivision.length() > 0) {
            textViewProfileUserDivision.setText(strDivision);
            textViewProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserDivision.setText(R.string.jandi_profile_optional);
            textViewProfileUserDivision.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
        // 직책
        String strPosition = (user.getUserPosition());
        if (strPosition.length() > 0) {
            textViewProfileUserPosition.setText(strPosition);
            textViewProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text));
        } else {
            textViewProfileUserPosition.setText(R.string.jandi_profile_optional);
            textViewProfileUserPosition.setTextColor(getResources().getColor(R.color.jandi_text_light));
        }
    }

    /************************************************************
     * 프로필 수정
     ************************************************************/
    @Click(R.id.profile_user_nickname)
    void editNickName() {
        // 닉네임
        runchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_NICKNAME,
                textViewProfileNickName
        );
    }

    @Click(R.id.profile_user_phone_number)
    void editPhoneNumber() {
        // 핸드폰 번호
        runchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE,
                textViewProfileUserPhone
        );
    }

    @Click(R.id.profile_user_division)
    void editDivision() {
        // 부서
        runchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_DIVISION,
                textViewProfileUserDivision
        );
    }

    @Click(R.id.profile_user_position)
    void editPosition() {
        // 직책
        runchEditDialog(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_POSITION,
                textViewProfileUserPosition
        );
    }

    private void runchEditDialog(int dialogActionType, TextView textView) {
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
            case EditTextDialogFragment.ACTION_MODIFY_PROFILE_NICKNAME:
                setTextAndChangeColor(textViewProfileNickName, event.inputMessage);
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

    @UiThread
    void setTextAndChangeColor(TextView textView, String textToBeChanged) {
        if (textToBeChanged.length() <= 0) {
            ColoredToast.showError(this, "내용을 입력해주세요");
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
            ColoredToast.showWarning(this, "수정된 내역이 없습니다");
            return;
        }
    }

    @UiThread
    void updateProfile() {
        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.nickname = textViewProfileNickName.getText().toString();
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
            ResLeftSideMenu.User me = mJandiAuthClient.updateUserProfile(reqUpdateProfile);
            updateProfileSucceed(me);
        } catch (JandiException e) {
            log.error("get profile failed", e);
            updateProfileFailed();
        }
    }

    @UiThread
    void updateProfileSucceed(ResLeftSideMenu.User me) {
        ColoredToast.show(this, "프로필이 수정되었습니다");
        getProfileSuccess(me);
    }

    @UiThread
    void updateProfileFailed() {
        ColoredToast.showError(this, "프로필 수정에 실패하였습니다");
    }


    /************************************************************
     * 프로필 사진 업로드
     ************************************************************/
    @Click(R.id.profile_photo)
    void getPicture() {
        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT,      // 또는 ACTION_PICK
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");              // 모든 이미지
        intent.putExtra("crop", "true");        // Crop기능 활성화
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
        intent.putExtra("outputFormat",         // 포맷방식
                Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        mTempPhotoFile = getTempFile();
        return Uri.fromFile(mTempPhotoFile);
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    TEMP_PHOTO_FILE);
            try {
                f.createNewFile();      // 외장메모리에 temp.png 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    /** 다시 액티비티로 복귀하였을때 이미지를 셋팅 */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        String filePath = Environment.getExternalStorageDirectory() + "/" + TEMP_PHOTO_FILE;
                        log.debug("temp profile img : " + filePath);
                        attemptToUpdatePhoto = true;
                        mTempPhotoFile = new File(filePath);
                        Picasso.with(this)
                                .load(mTempPhotoFile)
                                .placeholder(R.drawable.jandi_profile)
                                .transform(new CircleTransform())
                                .skipMemoryCache()              // 메모리 캐시를 쓰지 않는다.
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
        progressDialog.setMessage(getString(R.string.file_uploading));
        progressDialog.show();

        String requestURL = JandiConstants.SERVICE_ROOT_URL + "inner-api/settings/profiles/photo";

        Ion.with(mContext, requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int)(downloaded/total));
                    }
                })
                .setHeader("Authorization", mMyToken)
                .setHeader("Accept", "application/vnd.tosslab.jandi-v1+json")
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(mTempPhotoFile.getName()), mTempPhotoFile)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        uploadProfilePhotoDone(e, result);
                    }
                });
    }

    @UiThread
    void uploadProfilePhotoDone(Exception exception, JsonObject result) {
        if (exception == null) {
            log.debug(result);
            attemptToUpdatePhoto = false;
            if (attemptToUpdate) {
                updateProfile();
            } else {
                ColoredToast.show(mContext, "사진이 수정되었습니다");
            }

        } else {
            log.error("uploadFileDone: FAILED", exception);
            ColoredToast.showError(mContext, "사진 업로드 실패");
        }
    }
}
