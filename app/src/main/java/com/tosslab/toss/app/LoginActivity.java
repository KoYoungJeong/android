package com.tosslab.toss.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResLogin;
import com.tosslab.toss.app.network.models.TossRestToken;
import com.tosslab.toss.app.utils.ColoredToast;
import com.tosslab.toss.app.utils.ProgressWheel;

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

    @AfterViews
    void init() {
        trustEveryone();    // SSL 우회! 꼭 지울 것!

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

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
            log.debug("Login Success : " + tossRestToken.token);
        } catch (RestClientException e) {
            log.error("Login Fail", e);
            showErrorOnUiThread(this, "Login failed");
        }
        mProgressWheel.dismiss();

        if (tossRestToken != null && tossRestToken.token != null) {
            moveToMainActivity(tossRestToken.token);
        }
    }

    // Error 메시지 출력
    @UiThread
    public void showErrorOnUiThread(Context context, String message) {
        ColoredToast.showError(this, "Login failed");
    }

    public void moveToMainActivity(String token) {
        MainActivity_.intent(this).myToken(token).start();
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
