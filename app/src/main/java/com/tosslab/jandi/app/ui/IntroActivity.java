package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 이번 실행이 업데이트 이후의 최초 실행인지 체크하여 Push notification token 을 갱신한다.
 *    갱신된 push notification 은 토큰이 있을 경우 업데이트되고 없으면 새로 로그인할 때 업데이트 된다.
 * 2. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 3. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro_final)
public class IntroActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroActivity.class);
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @RestService
    JandiRestClient mJandiRestClient;

    private JandiEntityClient mJandiEntityClient;
    private JandiAuthClient mJandiAuthClient;
    private GoogleCloudMessaging mGcm;
    private int mThisVersion;

    @AfterInject
    void init() {
        registerNewRelicToken();
        initAuthClient();
        retrieveThisAppVersion();
    }

    @AfterViews
    void startOn() {
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(this);
            String pushToken = getAlreadyGeneratedPushToken(this);

            // 지금 막 업데이트 한 경우에도 푸시 토큰을 갱신한다.
            if (pushToken.isEmpty() || isJustUpdated(this)) {
                generatePushTokenInBackground();
            }

            checkNewerVersionInBackground();
        } else {
            log.error("No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void retrieveThisAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            mThisVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerNewRelicToken() {
        NewRelic.withApplicationToken(JandiConstantsForFlavors.NEWRELIC_TOKEN_ID)
                .start(this.getApplication());
    }

    private void initAuthClient() {
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);
    }

    /************************************************************
     * for Push notification token
     ************************************************************/
    private String getAlreadyGeneratedPushToken(Context context) {
        String registrationId = JandiPreference.getPushToken(context);
        if (registrationId.isEmpty()) {
            return "";
        }
        return registrationId;
    }

    private boolean isJustUpdated(Context context) {
        int registeredVersion = JandiPreference.getPriorAppVersion(context);
        if (registeredVersion != mThisVersion) {
            log.info("Current app is just updated. It needs to regenerate push token.");
            return true;
        }
        return false;
    }

    @Background
    public void generatePushTokenInBackground() {
        try {
            if (mGcm == null) {
                mGcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            String pushToken = mGcm.register(JandiConstants.SENDER_ID);
            JandiPreference.setPushTokenToBeUpdated(this, pushToken);
            log.debug("push token to be updated is generated");
        } catch (IOException e) {
            log.error("generating push token failed", e);
            generatePushTokenFailed();
        }
    }

    @UiThread
    public void generatePushTokenFailed() {
        ColoredToast.showError(this, "Push error. Please try again after a while");
    }

    @Background
    public void registerPushTokenInBackground(String myAccessToken) {
        String oldPushToken = JandiPreference.getPushToken(this);
        String newPushToken = JandiPreference.getPushTokenToBeUpdated(this);
        log.debug("oldPushToken = " + oldPushToken);
        log.debug("newPushToken = " + newPushToken);
        try {
            mJandiEntityClient = new JandiEntityClient(mJandiRestClient, myAccessToken);

            if (newPushToken.isEmpty() == false) {
                mJandiEntityClient.registerNotificationToken(oldPushToken, newPushToken);
                log.debug("registering push token succeed, registration ID=" + newPushToken);
                sendRegistrationIdSucceed(newPushToken);
            } else {
                sendRegistrationIdSucceed(oldPushToken);
            }
        } catch (JandiNetworkException e) {
            if (e.errCode == 2000) {
                // 만료된 access 토큰이므로 로그인을 수행한 이후 등록한다.
                moveToIntroTutorialActivity();
            } else {
                log.error("Register Fail", e);
                sendRegistrationIdFailed(e.errCode + ":" + e.errReason);
            }
        }
    }

    @UiThread
    public void sendRegistrationIdSucceed(String updatedToken) {
        // 토큰 갱신이 성공했기 때문에 새로운 토큰을 push token 으로 저장.
        JandiPreference.setPushToken(this, updatedToken);
        JandiPreference.setPushTokenToBeUpdated(this, "");
        // 토큰 갱신이 성공했으므로 현재 버전을 저장
        JandiPreference.setPriorAppVersion(this, mThisVersion);
        moveToMainActivity();
    }

    @UiThread
    public void sendRegistrationIdFailed(String message) {
        ColoredToast.showError(this, message);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    /************************************************************
     * 최신 버전 체크
     ************************************************************/
    @Background
    public void checkNewerVersionInBackground() {
        boolean isLatestVersion = true;     // 기본 값 : 업데이트 안내가 뜨지 않는다.
        try {
            int latestVersion = getLatestVersionInBackground();
            if (mThisVersion < latestVersion) {
                isLatestVersion = false;
                log.info("A new version of JANDI is available.");
            }
        } catch (JandiNetworkException e) {
        } finally {
            checkWhetherUpdating(isLatestVersion);
        }
    }

    private int getLatestVersionInBackground() throws JandiNetworkException {
        ResConfig resConfig = mJandiAuthClient.getConfig();
        return resConfig.versions.android;
    }

    @UiThread
    public void checkWhetherUpdating(boolean isLatestVersion) {
        // 만약 최신 업데이트 앱이 존재한다면 다운로드 안내 창이 뜬다.
        if (!isLatestVersion) {
            showUpdateDialog();
        } else {
            checkSignInAndRegister();
        }
    }

    @UiThread
    public void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.jandi_update_title)
                .setMessage(R.string.jandi_update_message)
                .setPositiveButton(R.string.jandi_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                } finally {
                                    finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                                }
                            }
                        }
                )
                .create()
                .show();
    }

    /************************************************************
     * 자동 로그인 유무
     ************************************************************/
    private void checkSignInAndRegister() {
        String myToken = JandiPreference.getMyToken(this);
        if (myToken != null && myToken.length() > 0) {
            registerPushTokenInBackground(myToken);
        } else {
            if (JandiPreference.getFlagForTutorial(this)) {
                moveToLoginInputIdActivity();
            } else {
                moveToIntroTutorialActivity();
            }

        }
    }

    /************************************************************
     * 이동
     ************************************************************/
    @UiThread
    public void moveToMainActivity() {
        // MainActivity 이동
        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        finish();
    }

    @UiThread
    public void moveToIntroTutorialActivity() {
        IntroMainActivity_.intent(this).start();
        finish();
    }

    @UiThread
    public void moveToLoginInputIdActivity() {
        IntroSelectTeamActivity_.intent(this).start();
        finish();
    }
}
