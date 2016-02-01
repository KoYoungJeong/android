package com.tosslab.jandi.app.utils.parse;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
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

    public static final String CHANNEL_ID_PREFIX = "accountId_";

    public static void removeFileAndCacheIfNeed(Context context) {
        File parseDir = context.getDir("Parse", 0);
        if (parseDir != null && parseDir.exists()) {
            LogUtil.e(TAG, "delete Parse dir");

            File[] files = parseDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    LogUtil.d(TAG, file.getName());
                    file.delete();
                }
            }
            parseDir.delete();
        }

        File parseCacheDir = new File(context.getCacheDir(), "com.parse");
        if (parseCacheDir.exists()) {
            LogUtil.e(TAG, "delete com.parse cache dir");
            parseCacheDir.delete();
        }

        File parseFileDir = new File(context.getFilesDir(), "com.parse");
        if (parseFileDir.exists()) {
            LogUtil.e(TAG, "delete com.parse files dir");
            parseFileDir.delete();
        }
    }

    public static void addChannelOnServer() {
        Observable.OnSubscribe<String> subscribe = subscriber -> {
            String accountId = AccountUtil.getAccountId(JandiApplication.getContext());
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
                    long memberId = AccountRepository.getRepository().getSelectedTeamInfo().getMemberId();
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
                        List<String> channels = new ArrayList<>();
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

    public static void refreshChannelOnServer() {
        new Thread(() -> {
            ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();

            try {
                LogUtil.e(TAG, "remove start !");
                currentInstallation.remove(PARSE_CHANNELS);
                currentInstallation.save();
                LogUtil.e(TAG, "remove end !");
            } catch (ParseException e) {
                LogUtil.e(TAG, Log.getStackTraceString(e));
            }

            try {
                String accountId = AccountUtil.getAccountId(JandiApplication.getContext());
                if (!TextUtils.isEmpty(accountId)) {
                    accountId = CHANNEL_ID_PREFIX + accountId;

                    List<String> channels = new ArrayList<>();
                    channels.add(accountId);

                    LogUtil.e(TAG, "add start - " + accountId);
                    currentInstallation.addAllUnique(PARSE_CHANNELS, channels);
                    currentInstallation.save();
                    LogUtil.e(TAG, "add end - " + accountId);
                } else {
                    LogUtil.e(TAG, "add fail - accountId is empty.");
                }
            } catch (ParseException e) {
                LogUtil.e(TAG, Log.getStackTraceString(e));
            }

        }).start();
    }

}
