package com.tosslab.jandi.app.ui.profile.account;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.events.profile.AccountEmailChangeEvent;
import com.tosslab.jandi.app.events.profile.ProfileImageCompleteEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.profile.account.model.AccountProfileModel;
import com.tosslab.jandi.app.ui.profile.email.EmailChooseActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ImageFilePath;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
@EActivity(R.layout.activity_account_profile)
public class AccountProfileActivity extends BaseAnalyticsActivity {

    private static final int REQ_CODE_PICK_IMAGE = 2011;

    @Bean
    AccountProfilePresenter accountProfilePresenter;

    @Bean
    AccountProfileModel accountProfileModel;

    private boolean isNeedUploadPrimaryEmail;
    private boolean isNeedUploadName;
    private boolean isNeedUploadImage;
    private File tempFile;
    private ProgressDialog progressDialog;


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initProfileView();
        EntityManager entityManager = EntityManager.getInstance(AccountProfileActivity.this);
        trackGaAccountInfo(entityManager.getDistictId());
    }

    @AfterViews
    void afterViews() {
        progressDialog = new ProgressDialog(AccountProfileActivity.this);
        setActionBarSetting();
    }

    private void setActionBarSetting() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelection() {
        finish();
    }

    @UiThread
    void initProfileView() {

        String name = accountProfileModel.getAccountName();
        accountProfilePresenter.setName(name);

        String email = accountProfileModel.getAccountPrimaryEmail();
        accountProfilePresenter.setEmail(email);

        String accountProfileImage = accountProfileModel.getAccountProfileImage();
        accountProfilePresenter.setProfileImage(Uri.parse(JandiConstantsForFlavors.SERVICE_ROOT_URL + accountProfileImage));
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();

        if (isNeedUploadImage || isNeedUploadName || isNeedUploadPrimaryEmail) {
            getMenuInflater().inflate(R.menu.account_profile_change, menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_confirm)
    void changeProfile() {

        // upload Email & Name
        uploadAccountProfile();

    }

    @Background
    void uploadAccountProfile() {
        accountProfilePresenter.showProgressDialog();
        ResAccountInfo resAccountInfo = null;
        try {

            if (isNeedUploadImage) {
                // 이미지 업로드
                accountProfilePresenter.showIncrementProgressDialog(progressDialog);
                String result = accountProfileModel.uploadImage(tempFile, progressDialog);
                resAccountInfo = new ObjectMapper().readValue(result, ResAccountInfo.class);
                accountProfilePresenter.dismissIncrementProgressDialog(progressDialog);
                isNeedUploadImage = false;
            }

            accountProfilePresenter.showProgressDialog();
            if (isNeedUploadPrimaryEmail) {
                // 이메일 업로드
                resAccountInfo = accountProfileModel.uploadPrimaryEmail(accountProfilePresenter.getPrimaryEmail());
                isNeedUploadPrimaryEmail = false;
            }
            if (isNeedUploadName) {
                // 이름 업로드
                resAccountInfo = accountProfileModel.uploadName(accountProfilePresenter.getName());
                isNeedUploadName = false;
            }
            accountProfilePresenter.showSuccessToModifyProfile();

            if (resAccountInfo != null) {

                MixpanelAccountAnalyticsClient
                        .getInstance(AccountProfileActivity.this, resAccountInfo.getId())
                        .trackSetAccount();
            }

            updateOptionMenu();
        } catch (Exception e) {
            accountProfilePresenter.showFailToModifyProfile();
        } finally {
            if (resAccountInfo != null) {
                accountProfileModel.updateAccountInfo(resAccountInfo);
            }
            // exception quit
            accountProfilePresenter.dismissIncrementProgressDialog(progressDialog);
            accountProfilePresenter.dismissProgressDialog();
            initProfileView();
        }

        accountProfilePresenter.dismissProgressDialog();
    }

    @UiThread
    void updateOptionMenu() {
        invalidateOptionsMenu();
    }

    @Click(R.id.btn_account_profile_sign_out)
    void signOut() {
        accountProfilePresenter.showSignoutDialog();
    }

    public void onEvent(SignOutEvent event) {
        startSignOut();
    }

    @Background
    void startSignOut() {
        accountProfilePresenter.showProgressDialog();
        try {
            accountProfileModel.signOut();

            ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(AccountProfileActivity.this).getAccountInfo();
            MixpanelAccountAnalyticsClient
                    .getInstance(AccountProfileActivity.this, accountInfo.getId())
                    .trackAccountSigningOut()
                    .flush()
                    .clear();

            EntityManager entityManager = EntityManager.getInstance(AccountProfileActivity.this);

            MixpanelMemberAnalyticsClient
                    .getInstance(AccountProfileActivity.this, entityManager.getDistictId())
                    .trackSignOut()
                    .flush()
                    .clear();


        } catch (Exception e) {
            // TODO Show Fail Toast
        }
        accountProfilePresenter.dismissProgressDialog();
        accountProfilePresenter.returnToLoginActivity();
    }

    @Click(R.id.txt_account_profile_user_email)
    void chooseEmail() {

        EmailChooseActivity_
                .intent(AccountProfileActivity.this)
                .start();

    }

    @Click(R.id.txt_account_profile_user_name)
    void changeName() {
        String currentText = accountProfilePresenter.getName();
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_ACCOUNT_NAME, currentText);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Click(R.id.img_account_profile_photo)
    void chooseProfileImage() {
        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT,      // 또는 ACTION_PICK
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");              // 모든 이미지
        intent.putExtra("crop", "true");        // Crop기능 활성화
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageFilePath.getTempUri());
        intent.putExtra("outputFormat",         // 포맷방식
                Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    @OnActivityResult(REQ_CODE_PICK_IMAGE)
    void onChooseProfileImageResult(int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

//        tempFile = new File(ImageFilePath.getPath(AccountProfileActivity.this, data.getData()));
        tempFile = new File(ImageFilePath.getTempPath());
        accountProfilePresenter.setProfileImage(Uri.fromFile(tempFile));
        isNeedUploadImage = true;
        invalidateOptionsMenu();

    }

    public void onEvent(AccountEmailChangeEvent event) {

        if (!TextUtils.equals(event.getUserEmail().getId(), accountProfileModel.getAccountPrimaryEmail())) {
            // if same Primary & choosed Email
            isNeedUploadPrimaryEmail = true;
            accountProfilePresenter.setEmail(event.getUserEmail().getId());
            invalidateOptionsMenu();
        }
    }

    public void onEvent(ProfileImageCompleteEvent e) {
        Exception exception = e.getException();

        if (exception == null) {
            // Success
            accountProfilePresenter.setProfileImage(Uri.fromFile(e.getFilePath()));
        } else {
            // fail
            ColoredToast.show(AccountProfileActivity.this, getString(R.string.jandi_profile_photo_upload_succeed));
        }
    }

    public void onEvent(ConfirmModifyProfileEvent e) {
        if (!TextUtils.equals(accountProfileModel.getAccountName(), e.inputMessage)) {
            isNeedUploadName = true;
            accountProfilePresenter.setName(e.inputMessage);
        } else {
            isNeedUploadName = false;
        }
        invalidateOptionsMenu();

    }
}
