package com.tosslab.jandi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tosslab.jandi.app.network.JandiNetworkClient;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResLogin;
import com.tosslab.jandi.app.network.models.TossRestToken;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(LoginActivity.class);

    @ViewById(R.id.et_login_email)
    EditText edtxtLoginId;
    @ViewById(R.id.et_login_password)
    EditText edtxtLoginPassword;
    @RestService
    TossRestClient tossRestClient;

    private Context mContext;
    private ProgressWheel mProgressWheel;
    private String myToken;

    private GoogleCloudMessaging mGcm;
    private String mRegId;

    @AfterViews
    void init() {
        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // 자동 로그인 과정.
        // 토큰이 저장되어 있으면 로그인 과정을 건너뛴다.
        // 푸쉬 등록 과정에서 해당 토큰을 사용한 통신이 실패하면 토큰이 만료되었다고 판단하여
        // 다시 본 activity를 실행한다.
        myToken = JandiPreference.getMyToken(this);

        if (myToken.length() > 0) {
            registerGcmInBackground();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /************************************************************
     * Login
     ************************************************************/

    /**
     * 로그인 버튼을 눌렀을 때, 로그인 실행
     */
    @Click(R.id.btn_login)
    void pressLoginButton() {
        mProgressWheel.show();
        doLogin();
    }

    @Background
    void doLogin() {
        ResLogin resLogin = new ResLogin();
        resLogin.email = edtxtLoginId.getText().toString();
        resLogin.password = edtxtLoginPassword.getText().toString();

        TossRestToken tossRestToken = null;
        try {
            tossRestToken = tossRestClient.loginAndReturnToken(resLogin);
            doneLogin(true, tossRestToken, -1);
        } catch (RestClientException e) {
            log.error("Login Fail", e);
            doneLogin(false, null, R.string.err_login);
        } catch (Exception e) {
            log.error("Login Fail", e);
            doneLogin(false, null, R.string.err_login);
        }
    }

    @UiThread
    void doneLogin(boolean isOk, TossRestToken token, int resId) {
        mProgressWheel.dismiss();

        if (isOk) {
            log.debug("Login Success : " + token.token);
            myToken = token.token;
            if (token != null && token.token != null) {
                registerGcmInBackground();
            }
        } else {
            ColoredToast.showError(this, getString(resId));
        }
    }

    public void moveToMainActivity() {
        // Preference 저장 - Token
        JandiPreference.setMyToken(this, myToken);

        // MainActivity 이동
        MainActivity_.intent(this).start();

        finish();
    }

    /************************************************************
     * GCM
     ************************************************************/
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * 현재 디바이스의 notification token을 가져온다. 아래와 같은 조건에서 갱신한다.
     * - 앱 버전이 바뀜
     * - GCM에서 현재 생성된 토큰이 기존과 다름
     *
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

                if (furtherGeneratedRegId.isEmpty()) {
                    // 버전이 바뀌었거나, 기존에 notification 토큰이 존재하지 않는다면
                    // 새로 notification 토큰을 생성한다.
                    sendRegistrationIdInBackground(justGeneratedRegId);
                } else if (justGeneratedRegId.equals(furtherGeneratedRegId)) {
                    // 기존 토큰과 같다면 바로 main activity로 이동
                    moveToMainActivity();
                } else {
                    // GCM에서 막 생성된 토큰이 기존과 다르다면, 서버에 토큰 업데이트를 수행한다.
                    updateRegistrationIdInBackground(furtherGeneratedRegId, justGeneratedRegId);
                }
            } catch (IOException ex) {
                log.error("Error :" + ex.getMessage());
                ColoredToast.showError(mContext, "Push 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
                return;
            }
        } else {
            log.warn("No valid Google Play Services APK found.");
            // TODO : Push 안 됨
            ColoredToast.showWarning(mContext, "Push 서비스를 사용할 수 없는 단말입니다");
            moveToMainActivity();
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

//    /**
//     * 현재 앱 버전의 변경 여부 조사
//     * @param context
//     * @return
//     */
//    private boolean isChangedAppVersion(Context context) {
//        final SharedPreferences prefs = getGCMPreferences();
//        int registeredVersion = prefs.getInt(JandiConstants.PREF_APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion(context);
//        return (registeredVersion != currentVersion);
//    }
//
//    private static int getAppVersion(Context context) {
//        try {
//            PackageInfo packageInfo = context.getPackageManager()
//                    .getPackageInfo(context.getPackageName(), 0);
//            return packageInfo.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            // should never happen
//            throw new RuntimeException("Could not get package name: " + e);
//        }
//    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
//        int appVersion = getAppVersion(context);
//        log.info("Saving regId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, regId);
//        editor.putInt(JandiConstants.PREF_APP_VERSION, appVersion);
        editor.commit();
    }

    private void clearRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
//        int appVersion = getAppVersion(context);
//        log.info("Saving regId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, "");
        editor.commit();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    @Background
    public void sendRegistrationIdInBackground(String regId) {
        try {
            JandiNetworkClient jandiNetworkClient = new JandiNetworkClient(tossRestClient, myToken);
            jandiNetworkClient.registerNotificationToken(regId);
            mRegId = regId;
            log.debug("New device token registered, registration ID=" + regId);
            sendRegistrationIdDone(true, null);
        } catch (JandiException e) {
            log.error("Register Fail", e);

            if (e.errCode == 2000) {
                // 만료된 토큰이므로 다시 로그인하라는 안내 표시.
                sendRegistrationIdDone(false, "만료된 토큰입니다.");
            } else if (e.errCode == 4001) {
                // 4001 은 duplicate token 이기 때문에 무시한다.
                moveToMainActivity();
            } else {
                sendRegistrationIdDone(false, e.errCode + ":" + e.errReason);
            }
        }
    }

    @Background
    public void updateRegistrationIdInBackground(String futherGenRegId, String justGenRegId) {
        try {
            JandiNetworkClient jandiNetworkClient = new JandiNetworkClient(tossRestClient, myToken);
            // 기존 Dev Token 갱신
            jandiNetworkClient.updateNotificateionToken(futherGenRegId, justGenRegId);
            mRegId = justGenRegId;
            log.debug("Device token updated, registration ID=" + mRegId);
            sendRegistrationIdDone(true, null);
        } catch (JandiException e) {
            log.error("Register Fail", e);
            if (e.errCode == 1839) {
                // 기존 토큰이 서버에 존재하지 않기 때문에 다시 새로 등록.
                clearRegistrationId(mContext);
                sendRegistrationIdInBackground(justGenRegId);
            } else if (e.errCode == 2000) {
                // 만료된 토큰이므로 다시 로그인하라는 안내 표시.
                sendRegistrationIdDone(false, "만료된 토큰입니다.");
            } else {
                sendRegistrationIdDone(false, e.errCode + ":" + e.errReason);
            }
        }
    }

    @UiThread
    public void sendRegistrationIdDone(boolean isOk, String message) {
        if (isOk) {
            sendSubscriptionInBackground();
        } else {
            ColoredToast.showError(this, message);
        }
    }

    @Background
    public void sendSubscriptionInBackground() {
        try {
            JandiNetworkClient jandiNetworkClient = new JandiNetworkClient(tossRestClient, myToken);
            jandiNetworkClient.subscribeNotification(mRegId, true);
            sendSubscriptionDone(true, null);
        } catch (JandiException e) {
            log.error("Register Fail", e);
            sendSubscriptionDone(false, e.errCode + ":" + e.errReason);
        }
    }

    @UiThread
    public void sendSubscriptionDone(boolean isOk, String message) {
        if (isOk) {
            log.debug("subscribe OK");
            storeRegistrationId(getApplicationContext(), mRegId);
            moveToMainActivity();
        } else {
            ColoredToast.showError(this, message);
        }
    }

    /************************************************************
     * SSL 인증서 우회
     * TODO : remove this
     ************************************************************/
    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
