package com.tosslab.jandi.app.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.LoginFragmentDialog;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResAuthToken;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@Fullscreen
@EActivity(R.layout.activity_intro_final)
public class LoginActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(LoginActivity.class);

    @RestService
    JandiRestClient mJandiRestClient;
    private JandiAuthClient mJandiAuthClient;
    private JandiEntityClient mJandiEntityClient;

    private Context mContext;
    private ProgressWheel mProgressWheel;
    private String myToken;

    private GoogleCloudMessaging mGcm;
    private String mRegId;

    @AfterViews
    void initView() {
        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // 로그인 관련 Network Client 설정
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);

        checkVersionInBackground();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
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

    /************************************************************
     * 최신 버전 체크
     ************************************************************/
    @Background
    public void checkVersionInBackground() {
        // 만약 최신 업데이트 앱이 존재한다면 다운로드 안내 창이 뜬다.
        if (isLatestVersion()) {
            // 자동 로그인 과정.
            // 토큰이 저장되어 있으면 로그인 과정을 건너뛴다.
            // 푸쉬 등록 과정에서 해당 토큰을 사용한 통신이 실패하면 토큰이 만료되었다고 판단하여
            // 다시 본 activity를 실행한다.
            myToken = JandiPreference.getMyToken(this);
            if (myToken.length() > 0) {
                registerGcm();
            } else {
                showLoginFragment();
            }

        } else {
            showUpdateDialog();
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
                                }
                                finish();
                            }
                        }
                )
                .create()
                .show();
    }

    /************************************************************
     * Login
     ************************************************************/
    private void showLoginFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        LoginFragmentDialog dialog = new LoginFragmentDialog();
        dialog.show(ft, "dialog");
    }

    /**
     * from LoginDialogFragment
     * @param id
     * @param passwd
     */
    public void pressLoginButton(String id, String passwd) {
        mProgressWheel.show();
        doLogin(id, passwd);
    }

    @Background
    void doLogin(String id, String passwd) {
        // 입력들의 포멧 체크
        if (FormatConverter.isInvalidEmailString(id)) {
            doneLogin(false, null, R.string.err_login_invalid_id);
            return;
        }
        if (FormatConverter.isInvalidPasswd(passwd)) {
            doneLogin(false, null, R.string.err_login_invalid_passwd);
            return;
        }
        try {
            // 나의 팀 ID 획득
            ResMyTeam resMyTeam = mJandiAuthClient.getMyTeamId(id);
            if (resMyTeam.teamList.size() <= 0) {
                doneLogin(false, null, R.string.err_login_unregistered_id);
                return;
            }
            // 진짜 로그인
            ResAuthToken resAuthToken = mJandiAuthClient.login(resMyTeam.teamList.get(0).teamId, id, passwd);
            JandiPreference.setMyId(mContext, id);
            doneLogin(true, resAuthToken, -1);
        } catch (JandiNetworkException e) {
            if (e.errCode == 1818) {
                doneLogin(false, null, R.string.err_login_invalid_info);
            } else {
                log.error("Login failed", e);
                doneLogin(false, null, R.string.err_login);
            }
        } catch (Exception e) {
            log.error("Login Fail", e);
            doneLogin(false, null, R.string.err_login);
        }
    }

    @UiThread
    void doneLogin(boolean isOk, ResAuthToken token, int resId) {
        mProgressWheel.dismiss();

        if (isOk) {
            log.debug("Login Success : " + token.token);
            myToken = token.token;
            if (token != null && token.token != null) {
                registerGcm();
            }
        } else {
            JandiPreference.clearMyToken(this);
            ColoredToast.showError(this, getString(resId));
        }
    }

    /************************************************************
     * GCM
     ************************************************************/
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @UiThread
    public void registerGcm() {
        mProgressWheel.show();
        registerGcmInBackground();
    }
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
                    registerGcmDone(true, null, null);
                } else {
                    // 새로 생성된 토큰이면 sendRegistrationId로 이동
                    registerGcmDone(true, justGeneratedRegId, null);
                }
            } catch (IOException ex) {
                log.error("Error :" + ex.getMessage());
                registerGcmDone(false, null, getString(R.string.err_push_registration));
                return;
            }
        } else {
            log.warn("No valid Google Play Services APK found.");
            // TODO : Push 안 됨
            registerGcmDone(false, null, getString(R.string.err_push_invalid_device));
        }
    }

    @UiThread
    public void registerGcmDone(boolean isOk, String justGeneratedRegId, String errMessage) {
        if (isOk) {
            if (justGeneratedRegId == null) {
                moveToMainActivity();
            } else {
                sendRegistrationIdInBackground(justGeneratedRegId);
            }
        } else {
            mProgressWheel.dismiss();
            showLoginFragment();
            ColoredToast.showWarning(mContext, errMessage);
        }

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
        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, myToken);
        try {
            mJandiEntityClient.registerNotificationToken(regId);
            mRegId = regId;
            log.debug("New device token registered, registration ID=" + regId);
            sendRegistrationIdDone(true, null);
        } catch (JandiNetworkException e) {
            if (e.errCode == 2000) {
                // 만료된 토큰이므로 다시 로그인하라는 안내 표시.
                sendRegistrationIdDone(false, getString(R.string.err_expired_token));
            } else if (e.errCode == 4001) {
                // 4001 은 duplicate token 이기 때문에 무시한다.
                log.warn("duplicated notification token");
                moveToMainActivity();
            } else {
                log.error("Register Fail", e);
                sendRegistrationIdDone(false, e.errCode + ":" + e.errReason);
            }
        }
    }

    @UiThread
    public void sendRegistrationIdDone(boolean isOk, String message) {
        mProgressWheel.dismiss();

        if (isOk) {
            sendSubscriptionInBackground();
        } else {
            showLoginFragment();
            ColoredToast.showError(this, message);
        }
    }

    @Background
    public void sendSubscriptionInBackground() {
        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, myToken);
        try {
            mJandiEntityClient.subscribeNotification(mRegId, true);
            sendSubscriptionDone(true, null);
        } catch (JandiNetworkException e) {
            log.error("Register Fail", e);
            sendSubscriptionDone(false, e.errCode + ":" + e.errReason);
        }
    }

    @UiThread
    public void sendSubscriptionDone(boolean isOk, String message) {
        if (isOk) {
            log.debug("subscribe OK");
            storeRegistrationId(mRegId);
            moveToMainActivity();
        } else {
            showLoginFragment();
            ColoredToast.showError(this, message);
        }
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGCMPreferences();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, regId);

        editor.commit();
    }
}
