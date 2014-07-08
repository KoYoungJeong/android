package com.tosslab.toss.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import com.tosslab.toss.app.lists.CdpItemManager;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResLeftSideMenu;
import com.tosslab.toss.app.network.models.ResLogin;
import com.tosslab.toss.app.network.models.TossRestToken;
import com.tosslab.toss.app.utils.ColoredToast;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import de.greenrobot.event.EventBus;

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

        SharedPreferences pref = getSharedPreferences(TossConstants.PREF_NAME, 0);
        myToken = pref.getString(TossConstants.PREF_TOKEN, "");

        // 저장돼 있는 토큰으로 LeftSideMenu 를 호출한다.
        // 실패하면 토큰이 만료되었다고 판단하여 다시 로그인을 실행한다.
        if (myToken.length() > 0) {
            getCdpItemFromServer();
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
                getCdpItemFromServer();
            }
        } else {
            ColoredToast.showError(this, message);
        }
    }

    /************************************************************
     * CDP List
     ************************************************************/
    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @UiThread
    public void getCdpItemFromServer() {
        mProgressWheel.show();
        getCdpItemInBackground();
    }

    @Background
    public void getCdpItemInBackground() {
        ResLeftSideMenu resLeftSideMenu = null;
        try {
            tossRestClient.setHeader("Authorization", myToken);
            resLeftSideMenu = tossRestClient.getInfosForSideMenu();
            getCdpItemEnd(true, resLeftSideMenu, null);
        } catch (RestClientException e) {
            getCdpItemEnd(false, null, "사용자 정보 획득에 실패하였습니다. 다시 로그인하세요.");
        } catch (HttpMessageNotReadableException e) {
            getCdpItemEnd(false, null, "사용자 정보 획득에 실패하였습니다. 다시 로그인하세요.");
        } catch (Exception e) {
            getCdpItemEnd(false, null, "사용자 정보 획득에 실패하였습니다. 다시 로그인하세요.");
        }
    }

    @UiThread
    public void getCdpItemEnd(boolean isOk, ResLeftSideMenu resLeftSideMenu, String errMessage) {
        mProgressWheel.dismiss();

        if (isOk) {
            moveToMainActivity(resLeftSideMenu);
        } else {
            ColoredToast.showError(this, errMessage);
        }
    }

    public void moveToMainActivity(ResLeftSideMenu resLeftSideMenu) {
        // Preference 저장 - Token
        SharedPreferences pref = getSharedPreferences(TossConstants.PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TossConstants.PREF_TOKEN, myToken);
        editor.commit();

        // MainActivity 이동
        CdpItemManager cdpItemManager = new CdpItemManager(resLeftSideMenu);
        MainActivity_.intent(this).myToken(myToken).start();
        EventBus.getDefault().postSticky(cdpItemManager);
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
