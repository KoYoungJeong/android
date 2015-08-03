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
import rx.Subscriber;
import rx.functions.Action1;
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

    public static void subscribeChannelFromAccountId(Context context) {
        Observable.OnSubscribe<String> subscribe = new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                ResAccountInfo accountInfo =
                        JandiAccountDatabaseManager.getInstance(context).getAccountInfo();
                String accountId = accountInfo != null ? accountInfo.getId() : null;
                if (TextUtils.isEmpty(accountId)) {
                    subscriber.onError(new NullPointerException("accountId"));
                } else {
                    subscriber.onNext(CHANNEL_ID_PREFIX + accountId);
                }
                subscriber.onCompleted();
            }
        };
        Observable.create(subscribe)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String accountId) {
                        LogUtil.i(TAG, "call - " + accountId);

                        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
                        int memberId = JandiAccountDatabaseManager.getInstance(context)
                                .getSelectedTeamInfo().getMemberId();
                        currentInstallation.put(PARSE_MY_ENTITY_ID, memberId);
                        if (currentInstallation.containsKey(PARSE_CHANNELS)) {
                            List<String> channels = new ArrayList<String>();
                            channels.add(accountId);

                            try {
                                currentInstallation.addAllUnique(PARSE_CHANNELS, channels);
                                currentInstallation.save();
                            } catch (ParseException e) {
                                LogUtil.e(TAG, e.getMessage());
                            }
                        }
                    }
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
