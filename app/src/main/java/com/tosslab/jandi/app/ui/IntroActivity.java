package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 이번 실행이 업데이트 이후의 최초 실행인지 체크하여 Push notification token 을 갱신한다.
 *    갱신된 push notification 은 토큰이 있을 경우 업데이트되고 없으면 새로 로그인할 때 업데이트 된다.
 * 2. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 3. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro)
public class IntroActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroActivity.class);

    @RestService
    JandiRestClient mJandiRestClient;

    private JandiAuthClient mJandiAuthClient;

    static class ResultHolder {
        boolean doneWaiting = false;
        boolean doneOtherElse = false;
    }

    @AfterInject
    void init() {
        registerNewRelicToken();
        initAuthClient();
    }

    @AfterViews
    void startOn() {
        int thisVersion = retrieveThisAppVersion();
        ResultHolder resultHolder = new ResultHolder();
        checkNewerVersionInBackground(thisVersion, resultHolder);
        waitForSplash(resultHolder);
    }

    private int retrieveThisAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    private void registerNewRelicToken() {
        NewRelic.withApplicationToken(JandiConstantsForFlavors.NEWRELIC_TOKEN_ID)
                .start(this.getApplication());
    }

    private void initAuthClient() {
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);
    }

    @Background(delay = 1500)
    void waitForSplash(ResultHolder resultHolder) {
        // 로딩 화면을 보여주기 위해 기본적으로 1.5초 대기
        joinWork(resultHolder, true, false);    // done waiting
    }


    /************************************************************
     * 최신 버전 체크
     ************************************************************/
    @Background
    void checkNewerVersionInBackground(int thisVersion, ResultHolder resultHolder) {
        // 예외가 발생할 경우에도 그저 업데이트 안내만 무시한다.
        boolean isLatestVersion = true;
        try {
            int latestVersion = getLatestVersionInBackground();
            if (thisVersion < latestVersion) {
                isLatestVersion = false;
                log.info("A new version of JANDI is available.");
            }
        } catch (JandiNetworkException e) {
        } catch (Exception e) {
        } finally {
            checkWhetherUpdating(isLatestVersion, resultHolder);
        }
    }

    @SupposeBackground
    int getLatestVersionInBackground() throws JandiNetworkException {
        ResConfig resConfig = mJandiAuthClient.getConfig();
        return resConfig.versions.android;
    }

    @SupposeBackground
    void checkWhetherUpdating(boolean isLatestVersion, ResultHolder resultHolder) {
        // 만약 최신 업데이트 앱이 존재한다면 다운로드 안내 창이 뜬다.
        if (!isLatestVersion) {
            showUpdateDialog();
        } else {
            joinWork(resultHolder, false, true);    // done other else
        }
    }

    @UiThread
    void showUpdateDialog() {
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
                                } finally {
                                    finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                                }
                            }
                        }
                )
                .create()
                .show();
    }

    /************************************************************
     * 이동
     ************************************************************/
    @UiThread
    void joinWork(ResultHolder resultHolder, boolean doneWaiting, boolean doneOtherElse) {

        if (doneWaiting) {
            resultHolder.doneWaiting = doneWaiting;
        }
        if (doneOtherElse) {
            resultHolder.doneOtherElse = doneOtherElse;
        }

        // 두가지의 sing task 모두 완료된 경우 다음으로 넘어간다.
        if (resultHolder.doneWaiting && resultHolder.doneOtherElse) {
            checkSignInAndRegister();
        }
    }

    // 자동 로그인 유무에 따른 분기.
    @SupposeUiThread
    void checkSignInAndRegister() {
        String myToken = JandiPreference.getMyToken(this);
        if (myToken != null && myToken.length() > 0) {
            moveToMainActivity();
        } else {
            moveToIntroTutorialActivity();
        }
    }

    @SupposeUiThread
    void moveToMainActivity() {
        // MainActivity 이동
        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        finish();
    }

    @SupposeUiThread
    void moveToIntroTutorialActivity() {
        IntroMainActivity_.intent(this).start();
        finish();
    }
}
