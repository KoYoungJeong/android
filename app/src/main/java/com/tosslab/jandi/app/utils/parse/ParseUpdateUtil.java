package com.tosslab.jandi.app.utils.parse;

import android.content.Context;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class ParseUpdateUtil {
    public static final String TAG = "JANDI.ParseUpdateUtils";

    public static final String PARSE_MY_ENTITY_ID = "myEntityId";
    public static final String PARSE_CHANNELS = "channels";
    public static final String PARSE_ACTIVATION = "activate";
    public static final String PARSE_ACTIVATION_ON = "on";
    public static final String PARSE_ACTIVATION_OFF = "off";

    private static final String CHANNEL_ID_PREFIX = "accountId_";

    public static void addChannelOnServer(Context context) {
        Observable.OnSubscribe<String> subscribe = subscriber -> {
            ResAccountInfo accountInfo =
                    JandiAccountDatabaseManager.getInstance(context).getAccountInfo();
            String accountId = accountInfo != null ? accountInfo.getId() : null;
            if (TextUtils.isEmpty(accountId)) {
                subscriber.onError(new NullPointerException("accountId"));
            } else {
                subscriber.onNext(CHANNEL_ID_PREFIX + accountId);
            }
            subscriber.onCompleted();
        };
        Observable.create(subscribe)
                .observeOn(Schedulers.io())
                .subscribe(accountId -> {
                    ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
                    int memberId = JandiAccountDatabaseManager.getInstance(context)
                            .getSelectedTeamInfo().getMemberId();
                    currentInstallation.put(PARSE_MY_ENTITY_ID, memberId);

                    // 이전 채널 중 accountId 와 맞지 않는 채널이 있으면 지운다.
                    if (currentInstallation.containsKey(PARSE_CHANNELS)) {
                        List<String> savedChannels = (List<String>) currentInstallation.get(PARSE_CHANNELS);
                        if (savedChannels != null && !savedChannels.isEmpty()) {
                            List<String> listForRemove = new ArrayList<String>();
                            for (String channel : savedChannels) {
                                if (!channel.contains(accountId)) {
                                    listForRemove.add(channel);
                                }
                            }
                            if (!listForRemove.isEmpty()) {
                                try {
                                    currentInstallation.removeAll(PARSE_CHANNELS, listForRemove);
                                    currentInstallation.save();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    try {
                        List<String> channels = new ArrayList<String>();
                        channels.add(accountId);

                        currentInstallation.addAllUnique(PARSE_CHANNELS, channels);
                        currentInstallation.save();

                        LogUtil.i(TAG, accountId + " add channel success.");
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.getMessage());
                    }
                }, throwable -> {
                    LogUtil.e(TAG, throwable.toString());
                });
    }

    public static void deleteChannelOnServer() {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.remove(PARSE_CHANNELS);
        try {
            currentInstallation.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
