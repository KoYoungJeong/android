package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityModel {

    private final Logger log = Logger.getLogger(IntroActivityModel.class);

    @RootContext
    Context context;

    @RestService
    JandiRestClient mJandiRestClient;

    private JandiAuthClient mJandiAuthClient;
    private Callback callback;

    private static final long MAX_DELAY_MS = 1500l;

    // check for Splash time (1500ms)
    private final long initTime = System.currentTimeMillis();

    @AfterInject
    void initObject() {
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * Check new app version
     */
    @Background
    public void checkNewVersion() {
        // 예외가 발생할 경우에도 그저 업데이트 안내만 무시한다.
        boolean isLatestVersion = true;
        try {
            // get current app version
            int thisVersion = retrieveThisAppVersion(context);

            // get stored app version at server
            int latestVersion = getLatestVersionInBackground();
            if (thisVersion < latestVersion) {
                isLatestVersion = false;
                log.info("A new version of JANDI is available.");
            }
        } catch (JandiNetworkException e) {
        } catch (Exception e) {
        } finally {
            checkWhetherUpdating(isLatestVersion);
        }
    }


    protected int retrieveThisAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    @SupposeBackground
    int getLatestVersionInBackground() throws JandiNetworkException {
        ResConfig resConfig = mJandiAuthClient.getConfig();
        return resConfig.versions.android;
    }

    @SupposeBackground
    void checkWhetherUpdating(boolean isLatestVersion) {
        // 만약 최신 업데이트 앱이 존재한다면 다운로드 안내 창이 뜬다.

        if (callback == null) {
            return;
        }

        if (!isLatestVersion) {
            callback.onUpdateDialog();
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            long currentTimeGap = currentTimeMillis - initTime;
            long sleepTime = MAX_DELAY_MS - currentTimeGap;

            if (sleepTime > 0) {
                try {
                    // delay for splash
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    callback.onIntroFinish();
                }
            }


        }
    }


    /**
     * Created by Steve SeongUg Jung on 14. 12. 3..
     */
    public interface Callback {

        void onUpdateDialog();

        void onIntroFinish();
    }
}
