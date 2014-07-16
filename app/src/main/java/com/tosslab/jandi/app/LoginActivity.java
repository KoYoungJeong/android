package com.tosslab.jandi.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLogin;
import com.tosslab.jandi.app.network.models.TossRestToken;
import com.tosslab.jandi.app.utils.ColoredToast;
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
public class LoginActivity extends Activity {
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

//        trustEveryone();    // SSL 우회! 꼭 지울 것!

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // 토큰이 저장되어 있으면 로그인 과정을 건너뛴다.
        // MainActivity에서 해당 토큰을 사용한 통신이 실패하면 토큰이 만료되었다고 판단하여
        // 다시 본 activity를 실행한다.
        myToken = JandiPreference.getMyToken(this);

        if (myToken.length() > 0) {
            moveToMainActivity();
        } else {
            // DO NOTHING
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /************************************************************
     * Login
     ************************************************************/

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
            doneLogin(true, tossRestToken, null);
        } catch (RestClientException e) {
            log.error("Login Fail", e);
            doneLogin(false, null, "Login failed");
        } catch (Exception e) {
            log.error("Login Fail", e);
            doneLogin(false, null, "Login failed");
        }
    }

    @UiThread
    void doneLogin(boolean isOk, TossRestToken token, String message) {
        mProgressWheel.dismiss();

        if (isOk) {
            log.debug("Login Success : " + token.token);
            myToken = token.token;
            if (token != null && token.token != null) {
                registerGcm();
            }
        } else {
            ColoredToast.showError(this, message);
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

    private void registerGcm() {
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(this);
            mRegId = getRegistrationId(mContext);
            // GCM 등록
            if (mRegId.isEmpty()) {
                registerInBackground();
            } else {
                moveToMainActivity();
            }
        } else {
            log.warn("No valid Google Play Services APK found.");
            // TODO : Push 안 됨
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
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(JandiConstants.PREF_REG_ID, "");
        if (registrationId.isEmpty()) {
            log.info("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(JandiConstants.PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            log.info("App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * GCM 서버에 registration ID and app versionCode를 등록하고 shared preference 에도 등록함.
     */
    @Background
    void registerInBackground() {

        try {
            if (mGcm == null) {
                mGcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            mRegId = mGcm.register(JandiConstants.SENDER_ID);
            log.debug("Device registered, registration ID=" + mRegId);

            sendRegistrationIdToBackend(mRegId);
        } catch (IOException ex) {
            log.error("Error :" + ex.getMessage());
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
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
        int appVersion = getAppVersion(context);
        log.info("Saving regId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, regId);
        editor.putInt(JandiConstants.PREF_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    @Background
    public void sendRegistrationIdToBackend(String regId) {
        // Your implementation here.
        try {
            ReqNotificationRegister req = new ReqNotificationRegister("android", regId);
            tossRestClient.setHeader("Authorization", myToken);
            ResCommon res = tossRestClient.registerNotificationToken(req);
            sendRegistrationIdDone(true, null);
        } catch (RestClientException e) {
            log.error("Register Fail", e);
            sendRegistrationIdDone(false, "register failed");
        } catch (Exception e) {
            log.error("Register Fail", e);
            sendRegistrationIdDone(false, "register failed");
        }
    }

    @UiThread
    public void sendRegistrationIdDone(boolean isOk, String message) {
        if (isOk) {
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
