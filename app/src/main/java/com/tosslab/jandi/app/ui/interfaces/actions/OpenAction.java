package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.UUID;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
@EBean
public class OpenAction implements Action {

    @RootContext
    Activity activity;

    ProgressWheel progressWheel;
    @Inject
    Lazy<AccountApi> accountApi;
    @Inject
    Lazy<LoginApi> loginApi;
    @Inject
    Lazy<DeviceApi> deviceApi;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(activity);
        DaggerApiClientComponent
                .create()
                .inject(this);
    }

    @Override
    public void execute(Uri data) {

        showProgress();

        if (data != null) {
            String access_token = data.getQueryParameter("access_token");
            String refresh_token = data.getQueryParameter("refresh_token");

            if (TextUtils.isEmpty(access_token) || TextUtils.isEmpty(refresh_token)) {
                startIntroActivity();
            } else {
                access_token = removeSpecialChar(access_token);
                refresh_token = removeSpecialChar(refresh_token);
                checkSession(access_token, refresh_token);
            }
        } else {
            startIntroActivity();
        }
    }

    String removeSpecialChar(String access_token) {
        access_token = access_token.replaceAll("[^a-zA-Z0-9]", "");
        return access_token;
    }

    void checkSession(final String accessToken, final String refreshToken) {
        ResAccessToken originToken = TokenUtil.getTokenObject();

        String previousRefreshToken = null;

        if (originToken != null && !TextUtils.isEmpty(originToken.getAccessToken())) {
            previousRefreshToken = originToken.getRefreshToken();
        }

        ResAccessToken resAccessToken = new ResAccessToken();
        resAccessToken.setAccessToken(accessToken);
        resAccessToken.setRefreshToken(refreshToken);
        TokenUtil.saveTokenInfoByRefresh(resAccessToken);
        signIn(refreshToken);
        deletePreviousToken(previousRefreshToken);
    }

    private void signIn(String refreshToken) {
        Observable.just(refreshToken)
                .subscribeOn(Schedulers.io())
                .concatMap(token -> {
                    ReqAccessToken newAccessToken = ReqAccessToken.createRefreshReqToken(token);
                    try {
                        ResAccessToken resAccessToken = loginApi.get().getAccessToken(newAccessToken);
                        TokenUtil.saveTokenInfoByRefresh(resAccessToken);
                        return Observable.just(resAccessToken);
                    } catch (RetrofitException e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return Observable.error(e);
                    }
                })
                .concatMap(resAccessToken -> {
                    try {
                        ResAccountInfo accountInfo = accountApi.get().getAccountInfo();

                        ReqSubscribeToken subscibeToken = new ReqSubscribeToken(true);
                        deviceApi.get().updateSubscribe(resAccessToken.getDeviceId(), subscibeToken);

                        return Observable.just(accountInfo);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                        return Observable.error(e);
                    }
                })
                .doOnNext(resAccountInfo -> {
                    AccountUtil.removeDuplicatedTeams(resAccountInfo);
                    AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::successAccessToken,
                        e -> {
                            TokenUtil.clearTokenInfo();
                            AccountRepository.getRepository().clearAccountData();
                            failAccessToken();
                        }, this::startIntroActivity);

    }

    private void deletePreviousToken(String previousRefreshToken) {
        if (!TextUtils.isEmpty(previousRefreshToken)) {
            final String deviceIdOrigin = TextUtils.isEmpty(TokenUtil.getTokenObject().getDeviceId())
                    ? UUID.randomUUID().toString()
                    : TokenUtil.getTokenObject().getDeviceId();
            try {
                loginApi.get().deleteToken(previousRefreshToken, deviceIdOrigin);
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showProgress() {

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissProgress() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    @UiThread
    void failAccessToken() {
        ColoredToast.showWarning(activity.getString(R.string.jandi_error_web_token));
    }

    @UiThread
    void successAccessToken(ResAccountInfo accountInfo) {
        ColoredToast.show(activity.getString(R.string.jandi_success_web_token));
    }

    void startIntroActivity() {
        dismissProgress();
        IntroActivity.startActivity(activity, true);
        activity.finish();
    }
}
