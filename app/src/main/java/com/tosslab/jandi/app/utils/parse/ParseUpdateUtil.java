package com.tosslab.jandi.app.utils.parse;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class ParseUpdateUtil {

    public static void updateParseWithoutSelectedTeam(Context context) {
        List<ResAccountInfo.UserTeam> userTeams = getUserTeams(context);

        Observable.from(userTeams)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .map(userTeam -> {
                    LogUtil.d("UpdateParseWithoutSelectedTeam");
                    try {
                        return RequestApiManager.getInstance().getInfosForSideMenuByMainRest(userTeam.getTeamId());
                    } catch (RetrofitError retrofitError) {
                        retrofitError.printStackTrace();
                        return null;
                    }
                })
                .filter(resLeftSideMenu1 -> resLeftSideMenu1 != null)
                .map(resLeftSideMenu -> {
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
                })
                .collect(() -> new ArrayList<String>(), (collector, values) -> collector.addAll(values))
                .filter(subscribeList -> !subscribeList.isEmpty())
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
                            try {
                                currentInstallation.removeAll(JandiConstants.PARSE_CHANNELS, removeChannles);
                                currentInstallation.save();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            currentInstallation.addAllUnique(JandiConstants.PARSE_CHANNELS, subscriber);
                            currentInstallation.save();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    LogUtil.e("Parse Error", throwable);

                    Crashlytics.log(Log.ERROR
                            , "Parse Push"
                            , "Push Regist Fail" + throwable.getMessage());
                });

    }

    public static void deleteChannelOnServer() {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.remove(JandiConstants.PARSE_CHANNELS);
        try {
            currentInstallation.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static List<ResAccountInfo.UserTeam> getUserTeams(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getUserTeams();
    }

}
