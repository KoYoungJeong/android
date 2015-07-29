package com.tosslab.jandi.app.ui.intro.viewmodel;

import android.content.Context;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
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

        if (!NetworkCheckUtil.isConnected()) {
            // 네트워크 연결 상태 아니면 로그인 여부만 확인하고 넘어감
            if (!model.isNeedLogin(context)) {

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

                if (!model.isNeedLogin(context)) {
                    if (model.hasMigration()) {
                        refreshAccountInfo(context);
                        moveNextActivity(context, initTime, startForInvite);
                    } else {
                        migrationAccountInfos(context, initTime, startForInvite);
                        moveNextActivity(context, initTime, startForInvite);
                    }
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

    private void migrationAccountInfos(Context context, long initTime, final boolean startForInvite) {
        // v1.0.7 이전 설치자가 넘어온 경우

        model.refreshAccountInfo(context);
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

    @Background
    void refreshAccountInfo(Context context) {
        model.refreshAccountInfo(context);
    }

    void moveNextActivity(Context context, long initTime, final boolean startForInvite) {

        // like fork & join...but need to refactor

        model.sleep(initTime, MAX_DELAY_MS);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo != null && !startForInvite) {
            ParseUpdateUtil.updateParseWithoutSelectedTeam(context);
            view.moveToMainActivity();
        } else {
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
