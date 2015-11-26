package com.tosslab.jandi.app.ui.intro.presenter;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityPresenter {

    private static final long MAX_DELAY_MS = 500l;

    @Bean
    IntroActivityModel model;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void checkNewVersion(Context context, boolean startForInvite) {

        long initTime = System.currentTimeMillis();

        if (!model.isNetworkConnected()) {
            // 네트워크 연결 상태 아니면 로그인 여부만 확인하고 넘어감
            if (!model.isNeedLogin()) {

                if (model.hasMigration()) {
                    moveNextActivity(context, initTime, startForInvite);
                } else {
                    // 오프라인모드 첫 접근 하는 유저인 경우
                    // 네트워크 체크 처리
                    view.showCheckNetworkDialog();
                }

            } else {
                view.moveToIntroTutorialActivity();
            }

            return;
        }

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
                    // 0.4.xx 이하 버전 토큰 삭제 로직
                    model.removeOldToken(context);
                }

                // 메세지 캐시를 삭제함. 딱 1회에 한해서만 동작함
                clearLinkRepositoryIfFirstTime();

                if (!model.isNeedLogin()) {
                    if (model.hasMigration()) {
                        try {
                            refreshAccountInfo(context);
                            moveNextActivity(context, initTime, startForInvite);
                        } catch (RetrofitError retrofitError) {
                            retrofitError.printStackTrace();
                            if (retrofitError.getKind() != RetrofitError.Kind.HTTP
                                    || retrofitError.getResponse().getStatus() != JandiConstants.NetworkError.UNAUTHORIZED) {
                                moveNextActivity(context, initTime, startForInvite);
                            }
                        }
                    } else {
                        try {
                            migrationAccountInfos(context, initTime, startForInvite);
                            moveNextActivity(context, initTime, startForInvite);
                        } catch (RetrofitError retrofitError) {
                            retrofitError.printStackTrace();
                            if (retrofitError.getKind() != RetrofitError.Kind.HTTP
                                    || retrofitError.getResponse().getStatus() != JandiConstants.NetworkError.UNAUTHORIZED) {
                                moveNextActivity(context, initTime, startForInvite);
                            }
                        }
                    }
                } else {
                    model.sleep(initTime, MAX_DELAY_MS);
                    view.moveToIntroTutorialActivity();
                }
            }

        } catch (RetrofitError e) {
            model.sleep(initTime, MAX_DELAY_MS);

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            model.trackSignInFailAndFlush(errorCode);

            if (e.getCause() instanceof ConnectionNotFoundException) {
                view.showCheckNetworkDialog();
            } else {
                view.showMaintenanceDialog();
            }
        } catch (Exception e) {
            model.trackSignInFailAndFlush(-1);
            view.showCheckNetworkDialog();
        }

    }

    private void clearLinkRepositoryIfFirstTime() {
        /*
        발생하는 경우의 수
        1. 앱 설치/업데이트 후 첫 실행
        2. 로그아웃 직후
         */
        if (!JandiPreference.isClearedLink()) {
            model.clearLinkRepository();
            JandiPreference.setClearedLink();
            LogUtil.d("clearLinkRepository()");
        }
    }

    private void migrationAccountInfos(Context context, long initTime, final boolean startForInvite) throws RetrofitError {
        // v1.0.7 이전 설치자가 넘어온 경우

        model.refreshAccountInfo();
        int selectedTeamId = model.getSelectedTeamInfoByOldData(context);

        if (selectedTeamId > 0) {
            // 선택된 팀 정보가 있는 경우 : account 정보 > select team 설정 > Main Activity
            model.setSelectedTeamId(selectedTeamId);
            model.refreshEntityInfo(context);
            moveNextActivity(context, initTime, startForInvite);
        } else {
            // 선택된 팀 정보가 없는 경우 : account 정보 > TeamSelectActivity
            moveNextActivity(context, initTime, true);
        }

    }

    void refreshAccountInfo(Context context) throws RetrofitError {
        model.refreshAccountInfo();
    }

    void moveNextActivity(Context context, long initTime, final boolean startForInvite) {

        // like fork & join...but need to refactor

        model.sleep(initTime, MAX_DELAY_MS);

        if (model.hasSelectedTeam() && !startForInvite) {
            ParseUpdateUtil.addChannelOnServer();

            if (!model.hasLeftSideMenu() && NetworkCheckUtil.isConnected()) {
                // LeftSideMenu 가 없는 경우 대비
                model.refreshEntityInfo(context);
            }

            // Track Auto Sign In (with flush)
            model.trackAutoSignInSuccessAndFlush(true);

            view.moveToMainActivity();
        } else {
            model.trackAutoSignInSuccessAndFlush(false);

            view.moveTeamSelectActivity();
        }
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
