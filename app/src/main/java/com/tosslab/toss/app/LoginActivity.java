package com.tosslab.toss.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.TossRestLogin;
import com.tosslab.toss.app.network.entities.TossRestToken;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
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
    @ViewById(R.id.et_login_email)
    EditText edtxtLoginId;
    @ViewById(R.id.et_login_password)
    EditText edtxtLoginPassword;

    @RestService
    TossRestClient tossRestClient;

    private ProgressWheel mProgressWheel;

    @AfterViews
    void init() {
        trustEveryone();    // TODO : SSL 우회! 꼭 지울 것!

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

    @Click(R.id.btn_login)
    void pressLoginButton() {
        mProgressWheel.show();
        doLogin();
//        moveToMainActivity();
    }

    @Background
    void doLogin() {
        TossRestLogin tossRestLogin = new TossRestLogin();
        tossRestLogin.email = edtxtLoginId.getText().toString();
        tossRestLogin.password = edtxtLoginPassword.getText().toString();

        TossRestToken tossRestToken = null;
        try {
            tossRestToken = tossRestClient.loginAndReturnToken(tossRestLogin);
            Log.e("OK", "Login Success : " + tossRestToken.token);
        } catch (RestClientException e) {
            Log.e("HI", "Login Fail", e);
        }
        mProgressWheel.dismiss();

        if (tossRestToken != null && tossRestToken.token != null) {
            moveToMainActivity(tossRestToken.token);
        }
    }

    public void moveToMainActivity(String token) {
        MainActivity_.intent(this).myToken(token).start();
        finish();
    }

    // TODO : remove this
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
