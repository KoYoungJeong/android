package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 9. 24..
 */
@Fullscreen
@EActivity
public class IntroStartActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroStartActivity.class);

    @RestService
    JandiRestClient jandiRestClient;
    private JandiAuthClient mJandiAuthClient;

    @AfterInject
    void checkHasToken() {
        NewRelic.withApplicationToken("AAa432b3c57008e42b2a3fa8bc9a7542fa13aace93")
                .start(this.getApplication());
        mJandiAuthClient = new JandiAuthClient(jandiRestClient);
        checkVersionInBackground();
    }

    /************************************************************
     * 최신 버전 체크
     ************************************************************/
    @Background
    public void checkVersionInBackground() {

        boolean isLatestVersion = false;
        try {
            isLatestVersion = isLatestVersion();
        } catch (PackageManager.NameNotFoundException e) {
        } catch (JandiNetworkException e) {
            // TODO 지금은 네트워크 통신 에러 등이면 일단 최신버전으로 취급하자.
            isLatestVersion = true;
        }

        // 만약 최신 업데이트 앱이 존재한다면 다운로드 안내 창이 뜬다.
        if (isLatestVersion) {
            // 자동 로그인 과정.
            // 토큰이 저장되어 있으면 로그인 과정을 건너뛴다.
            String myToken = JandiPreference.getMyToken(this);
            if (myToken != null && myToken.length() > 0) {
                // LoginFinalActivity로 이동
                moveToIntroFinalActivity(myToken);
            } else {
                // LoginStartActivity로 이동
                moveToLoginInputIdActivity();
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

    private boolean isLatestVersion() throws PackageManager.NameNotFoundException, JandiNetworkException {
        String packageName = getPackageName();
        int curVersion = getPackageManager().getPackageInfo(packageName, 0).versionCode;
        ResConfig resConfig = mJandiAuthClient.getConfig();
        int newVersion = resConfig.versions.android;
        return (curVersion >= newVersion) ? true : false;
    }

    /************************************************************
     * Activity 이동
     ************************************************************/
    @UiThread
    public void moveToIntroFinalActivity(String myToken) {
        log.debug("moveToIntroFinalActivity");
        IntroFinalActivity_.intent(this).myToken(myToken).start();
        finish();
    }

    @UiThread
    public void moveToLoginInputIdActivity() {
        log.debug("moveToLoginInputIdActivity");
        IntroSelectTeamActivity_.intent(this).start();
        finish();
    }
}
