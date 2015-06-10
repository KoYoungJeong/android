package com.tosslab.jandi.app.utils.parse;

import android.content.Context;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseInstallation;
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
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class ParseUpdateUtil {


    public static void updateParseWithoutSelectedTeam(Context context) {
        List<ResAccountInfo.UserTeam> userTeams = getUserTeams(context);

        Observable.from(userTeams)
                .observeOn(Schedulers.io())
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
                            } else {
                                parseChannel = "";
                            }

                            if (!TextUtils.isEmpty(parseChannel)) {
                                subscribeList.add(parseChannel);
                            }
                        }

                        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
                            if (entity instanceof ResLeftSideMenu.User) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + entity.id + "-" + myId;
                                subscribeList.add(parseChannel);
                            }

                        }

                        return subscribeList;
                    }

                })
                .collect(() -> new ArrayList<String>(), (collector, values) -> collector.addAll(values))
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> subscriber) {
                        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
                        int memberId = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getMemberId();
                        currentInstallation.put(JandiConstants.PARSE_MY_ENTITY_ID, memberId);
                        if (currentInstallation.containsKey(JandiConstants.PARSE_CHANNELS)) {
                            List<String> savedChannels = (List<String>) currentInstallation.get(JandiConstants.PARSE_CHANNELS);
                            List<String> removeChannles = new ArrayList<String>();
                            for (int idx = savedChannels.size() - 1; idx >= 0; idx--) {
                                String savedChannel = savedChannels.get(idx);
                                if (!subscriber.contains(savedChannel)) {
                                    removeChannles.add(savedChannel);
                                }
                            }
                            currentInstallation.removeAll(JandiConstants.PARSE_CHANNELS, removeChannles);
                            try {
                                currentInstallation.save();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        currentInstallation.addAllUnique(JandiConstants.PARSE_CHANNELS, subscriber);
                        try {
                            currentInstallation.save();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> LogUtil.e("Parse Error", throwable));

    }

    private static List<ResAccountInfo.UserTeam> getUserTeams(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getUserTeams();
    }

}
