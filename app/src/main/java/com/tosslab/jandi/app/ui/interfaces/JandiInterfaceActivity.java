package com.tosslab.jandi.app.ui.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..<br/>
 * It's for {tosslabjandi://xxx?yyy=zzz} Intent
 */
@EActivity(R.layout.activity_temp_test)
public class JandiInterfaceActivity extends Activity {

    private final Logger log = Logger.getLogger(JandiInterfaceActivity.class);

    @RestService
    JandiRestClient jandiRestClient;


    @ViewById(R.id.tttttt)
    TextView textView;

    @AfterViews
    void initView() {

        Intent intent = getIntent();
        Uri data = intent.getData();
        String host = data.getHost();

        log.debug("Host : " + host + " , querys : " + data.getQuery());

        ColoredToast.showWarning(JandiInterfaceActivity.this, data.toString());

        getTempAccountInfo(data);

    }

    @Background
    void getTempAccountInfo(Uri data) {
        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(data.getQueryParameter("access_token"));
        accessToken.setRefreshToken(data.getQueryParameter("refresh_token"));
        accessToken.setTokenType("bearer");


        try {
            jandiRestClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
            ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
            showAccountInfo(accountInfo);
        } catch (HttpStatusCodeException e) {
            showAccountInfo(null);

        }
    }

    @UiThread
    void showAccountInfo(ResAccountInfo accountInfo) {

        if (accountInfo != null) {
            textView.setText(accountInfo.getName() + " : " + accountInfo.getEmails());
        } else {
            textView.setText("으흠...로그인 정보가 이상하네요?");
        }

    }

}
