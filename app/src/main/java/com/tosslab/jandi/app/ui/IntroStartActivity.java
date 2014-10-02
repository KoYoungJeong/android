package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

/**
 * Created by justinygchoi on 2014. 9. 24..
 */
@Fullscreen
@EActivity
public class IntroStartActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroStartActivity.class);

    @AfterInject
    void checkHasToken() {
        checkVersionInBackground();
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

    private boolean isLatestVersion() {
        try {
            log.debug("isLatestVersion : Get version from Play Store");
            String packageName = getPackageName();
            String curVersion = getPackageManager().getPackageInfo(packageName, 0).versionName;
            String newVersion = curVersion;
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
            return (value(curVersion) >= value(newVersion)) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private long value(String string) {
        string = string.trim();
        if( string.contains( "." )){
            final int index = string.lastIndexOf( "." );
            return value( string.substring( 0, index ))* 100 + value( string.substring( index + 1 ));
        }
        else {
            return Long.valueOf( string );
        }
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
