package com.tosslab.jandi.app.utils.parse;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class ParseUpdateUtil {


    public static void updateParseWithoutSelectedTeam(Context context) {
        List<ResAccountInfo.UserTeam> userTeams = getUnselectedTeam(context);

        Observable.from(userTeams)
                .map(new Func1<ResAccountInfo.UserTeam, ResLeftSideMenu>() {
                    @Override
                    public ResLeftSideMenu call(ResAccountInfo.UserTeam userTeam) {

                        JandiRestClient jandiRestClient = new JandiRestClient_(context);
                        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                        return jandiRestClient.getInfosForSideMenu(userTeam.getTeamId());
                    }
                })
                .map(new Func1<ResLeftSideMenu, List<String>>() {
                    @Override
                    public List<String> call(ResLeftSideMenu resLeftSideMenu) {
                        int myId = resLeftSideMenu.user.id;

                        List<String> subscribeList = new ArrayList<String>();
                        String parseChannel;
                        for (ResLeftSideMenu.Entity joinEntity : resLeftSideMenu.joinEntities) {
                            if (joinEntity instanceof ResLeftSideMenu.Channel) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id;
                            } else if (joinEntity instanceof ResLeftSideMenu.PrivateGroup) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id;
                            } else if (joinEntity instanceof ResLeftSideMenu.User) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id + "-" + myId;
                            } else {
                                parseChannel = "";
                            }

                            if (!TextUtils.isEmpty(parseChannel)) {
                                subscribeList.add(parseChannel);
                            }
                        }

                        return subscribeList;
                    }

                })
                .collect(new Func0<List<String>>() {
                    @Override
                    public List<String> call() {
                        return new ArrayList<String>();
                    }
                }, new Action2<List<String>, List<String>>() {
                    @Override
                    public void call(List<String> collector, List<String> values) {

                    }
                })
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> subscriber) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtil.e("Parse Error", throwable);
                    }
                });

    }

    private static List<ResAccountInfo.UserTeam> getUnselectedTeam(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getUserTeams();
    }

}
