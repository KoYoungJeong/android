package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 9. 24..
 */
@Fullscreen
@EActivity(R.layout.activity_intro_final)
public class IntroFinalActivity extends Activity {
    private final Logger log = Logger.getLogger(LoginActivity.class);
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @RestService
    JandiRestClient mJandiRestClient;
    @Extra
    String myToken;

    private JandiEntityClient mJandiEntityClient;
    private ProgressWheel mProgressWheel;
    private GoogleCloudMessaging mGcm;

    @AfterViews
    public void init() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, myToken);

        mProgressWheel.show();
        registerGcmInBackground();
    }

    @UiThread
    public void moveToMainActivity() {
        mProgressWheel.dismiss();

        // Preference 저장 - Token
        JandiPreference.setMyToken(this, myToken);
        // MainActivity 이동
        MainTabActivity_.intent(this).start();

        finish();
    }

    @UiThread
    public void moveToLoginStartActivity() {
        mProgressWheel.dismiss();

        // TODO
        finish();
    }

    /************************************************************
     * GCM
     ************************************************************/

    /**
     * 현재 디바이스의 notification token을 가져온다. 아래와 같은 조건에서 갱신한다.
     * - 앱 버전이 바뀜
     * - GCM에서 현재 생성된 토큰이 기존과 다름
     */
    @Background
    public void registerGcmInBackground() {
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            try {
                if (mGcm == null) {
                    mGcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                }
                String justGeneratedRegId = mGcm.register(JandiConstants.SENDER_ID);
                String furtherGeneratedRegId = getRegistrationId();

                if (justGeneratedRegId.equals(furtherGeneratedRegId)) {
                    // 기존 토큰과 같다면 바로 main activity로 이동
                    moveToMainActivity();
                } else {
                    // 새로 생성된 토큰이면 sendRegistrationId로 이동
                    registerGcmSucceed(justGeneratedRegId);
                }
            } catch (IOException ex) {
                log.error("Error :" + ex.getMessage());
                registerGcmFailed(getString(R.string.err_push_registration));
                return;
            }
        } else {
            log.warn("No valid Google Play Services APK found.");
            // TODO : Push 안 됨
            registerGcmFailed(getString(R.string.err_push_invalid_device));
        }
    }

    @UiThread
    public void registerGcmSucceed(String justGeneratedRegId) {
        sendRegistrationIdInBackground(justGeneratedRegId);
    }

    @UiThread
    public void registerGcmFailed(String errMessage) {
        ColoredToast.showWarning(this, errMessage);
        moveToLoginStartActivity();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                log.info("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * 현재 GCM regID를 획득
     * 없으면 null 리턴, then 등록한다.
     */
    private String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(JandiConstants.PREF_REG_ID, "");
        if (registrationId.isEmpty()) {
            log.info("Registration not found.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
    }

    /************************************************************
     * GCM 에 필요한 ID를 서버에 등록
     ************************************************************/

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    @Background
    public void sendRegistrationIdInBackground(String regId) {
        try {
            mJandiEntityClient.registerNotificationToken(regId);
            log.debug("New device token registered, registration ID=" + regId);
            sendRegistrationIdSucceed(regId);
        } catch (JandiNetworkException e) {
            if (e.errCode == 2000) {
                // 만료된 토큰이므로 다시 로그인하라는 안내 표시.
                sendRegistrationIdFailed(getString(R.string.err_expired_token));
            } else if (e.errCode == 4001) {
                // 4001 은 duplicate token 이기 때문에 무시한다.
                log.warn("duplicated notification token");
                moveToMainActivity();
            } else {
                log.error("Register Fail", e);
                sendRegistrationIdFailed(e.errCode + ":" + e.errReason);
            }
        }
    }

    @UiThread
    public void sendRegistrationIdSucceed(String regId) {
        sendSubscriptionInBackground(regId);
    }

    @UiThread
    public void sendRegistrationIdFailed(String message) {
        moveToLoginStartActivity();
        ColoredToast.showError(this, message);
    }

    @Background
    public void sendSubscriptionInBackground(String regId) {
        try {
            mJandiEntityClient.subscribeNotification(regId, true);
            sendSubscriptionSucceed(regId);
        } catch (JandiNetworkException e) {
            log.error("Register Fail", e);
            sendSubscriptionFailed(e.errCode + ":" + e.errReason);
        }
    }

    @UiThread
    public void sendSubscriptionSucceed(String regId) {
        log.debug("subscribe OK");
        storeRegistrationId(regId);
        moveToMainActivity();
    }

    @UiThread
    public void sendSubscriptionFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
        moveToLoginStartActivity();
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGCMPreferences();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, regId);

        editor.commit();
    }
}
