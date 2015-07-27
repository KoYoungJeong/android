package com.tosslab.jandi.app.ui.intro.viewmodel;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityPresenter {

    private static final long MAX_DELAY_MS = 1500l;

    @Bean
    IntroActivityModel model;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void checkNewVersion(Context context, boolean startForInvite) {

        long initTime = System.currentTimeMillis();
        try {
            ResConfig config = model.getConfigInfo();

            int installedAppVersion = model.getInstalledAppVersion(context);

            if (config.maintenance != null && config.maintenance.status) {
                view.showMaintenanceDialog();
            } else if (installedAppVersion < config.versions.android) {
                model.sleep(initTime, MAX_DELAY_MS);
                view.showUpdateDialog();
            } else {
                if (model.hasOldToken(context)) {
                    model.removeOldToken(context);
                }

                if (!model.isNeedLogin(context)) {
                    refreshTokenAndGoNextActivity(context, initTime, startForInvite);
                } else {
                    model.sleep(initTime, MAX_DELAY_MS);
                    view.moveToIntroTutorialActivity();
                }
            }

        } catch (RetrofitError e) {
            model.sleep(initTime, MAX_DELAY_MS);
            if (e.getCause() instanceof ConnectionNotFoundException) {
                view.showCheckNetworkDialog();
            } else {
                view.showMaintenanceDialog();
            }
        } catch (Exception e) {
            view.showCheckNetworkDialog();
        }

    }

    @Background
    void refreshTokenAndGoNextActivity(Context context, long initTime, final boolean startForInvite) {

        // like fork & join...but need to refactor

        Observable.combineLatest(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                new Thread(() -> {
                    try {
                        model.refreshAccountInfo(context);
                        subscriber.onNext(JandiConstants.NETWORK_SUCCESS);
                    } catch (RetrofitError e) {
                        if (e.getResponse() == null) {
                            subscriber.onNext(-1);
                        } else {
                            subscriber.onNext(e.getResponse().getStatus());
                        }
                    } catch (Exception e) {
                        subscriber.onNext(500);
                    }
                    subscriber.onCompleted();
                }).start();
            }
        }), Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                new Thread(() -> {
                    subscriber.onNext(model.refreshEntityInfo(context) ? 1 : -1);
                    subscriber.onCompleted();
                }).start();

            }
        }), (o, o2) -> o).subscribe(o -> {
            if (o == JandiConstants.NETWORK_SUCCESS) {
                model.sleep(initTime, MAX_DELAY_MS);
                if (model.hasSelectedTeam(context) && !startForInvite) {
                    ParseUpdateUtil.updateParseWithoutSelectedTeam(context);

                    // Track Auto Sign In (with flush)
                    model.trackAutoSignInSuccessAndFlush(context);

                    view.moveToMainActivity();
                } else {
                    view.moveTeamSelectActivity();
                }
            } else if (o == JandiConstants.NetworkError.UNAUTHORIZED) {
                model.trackSignInFailAndFlush(context, JandiConstants.NetworkError.UNAUTHORIZED);

                model.clearTokenInfo();
                model.clearAccountInfo(context);

                model.sleep(initTime, MAX_DELAY_MS);
                view.moveToIntroTutorialActivity();
            } else {
                model.trackSignInFailAndFlush(context, o);

                view.showWarningToast(context.getResources().getString(R.string.err_network));
                view.finishOnUiThread();
            }
        });
    }

    public interface View {
        void moveToIntroTutorialActivity();

        void moveToMainActivity();

        void moveTeamSelectActivity();

        void showCheckNetworkDialog();

        void showMaintenanceDialog();

        void showUpdateDialog();

        void showWarningToast(String message);

        void finishOnUiThread();
    }

}
