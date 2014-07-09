package com.tosslab.jandi.app;

import android.app.Activity;
import android.widget.EditText;

import com.tosslab.jandi.app.network.TossRestClient;
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

    private ProgressWheel mProgressWheel;
    private String myToken;

    @AfterViews
    void init() {
        trustEveryone();    // SSL 우회! 꼭 지울 것!

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
                moveToMainActivity();
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
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
