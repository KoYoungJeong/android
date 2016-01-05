package com.tosslab.jandi.app.ui.maintab.more.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

@EBean
public class MainMoreModel {

    private static final String SUPPORT_URL_KO = "https://jandi.zendesk.com/hc/ko";
    private static final String SUPPORT_URL_JA = "https://jandi.zendesk.com/hc/ja";
    private static final String SUPPORT_URL_ZH_CH = "https://jandi.zendesk.com/hc/zh-cn";
    private static final String SUPPORT_URL_ZH_TW = "https://jandi.zendesk.com/hc/zh-tw";
    private static final String SUPPORT_URL_EN = "https://jandi.zendesk.com/hc/en-us";


    public String getVersionName() {
        Context context = JandiApplication.getContext();
        String packageName = context.getPackageName();
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public int getOtherTeamBadge() {
        AccountRepository accountRepository = AccountRepository.getRepository();
        int selectedTeamId = accountRepository.getSelectedTeamId();
        final int badgeCount[] = {0};
        Observable.from(accountRepository.getAccountTeams())
                .filter(userTeam -> userTeam.getTeamId() != selectedTeamId)
                .subscribe(userTeam -> {
                    badgeCount[0] += userTeam.getUnread();
                    BadgeCountRepository.getRepository()
                            .upsertBadgeCount(userTeam.getTeamId(), userTeam.getUnread());
                });
        return badgeCount[0];
    }

    public int getEnabledUserCount() {
        List<FormattedEntity> formattedUsers = EntityManager.getInstance().getFormattedUsers();
        int enabledUserCount = Observable.from(formattedUsers)
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .count()
                .toBlocking()
                .firstOrDefault(0);
        return enabledUserCount;
    }

    public boolean isConnectedNetwork() {
        return NetworkCheckUtil.isConnected();
    }

    public Pair<Boolean, Integer> needToUpdate() {
        try {
            int currentVersion = getInstalledAppVersion();
            ResConfig config = RequestApiManager.getInstance().getConfigByMainRest();
            int latestVersion = config.latestVersions.android;
            return new Pair<>(currentVersion < latestVersion, latestVersion);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            return new Pair<>(false, -1);
        }
    }

    private int getInstalledAppVersion() {
        try {
            Context context = JandiApplication.getContext();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    public String getSupportUrlEachLanguage() {
        String language = LanguageUtil.getLanguage(JandiApplication.getContext());
        String supportUrl;
        if (TextUtils.equals(language, LanguageUtil.LANG_KO)) {
            supportUrl = SUPPORT_URL_KO;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_JA)) {
            supportUrl = SUPPORT_URL_EN; //일본어 컨텐츠가 없어서 영어버전 사용
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_CN)) {
            supportUrl = SUPPORT_URL_ZH_CH;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_TW)) {
            supportUrl = SUPPORT_URL_ZH_TW;
        } else {
            supportUrl = SUPPORT_URL_EN;
        }
        return supportUrl;
    }

    public boolean isIn3Seconds(long clickedTimes) {
        long diffTime = System.currentTimeMillis() - clickedTimes;
        return 0 < diffTime && diffTime < 3000;
    }

    public List<Pair<String, String>> getUserInfosForBugReport() {
        List<Pair<String, String>> userInfos = new ArrayList<>();

        userInfos.add(new Pair<>("디바이스 모델명", Build.MODEL));
        userInfos.add(new Pair<>("Android OS Version", Build.VERSION.RELEASE));
        userInfos.add(new Pair<>("Jandi App Version", getVersionName()));
        userInfos.add(new Pair<>("Member ID", String.valueOf(EntityManager.getInstance().getMe().getId())));
        userInfos.add(new Pair<>("Account Name", AccountRepository.getRepository().getAccountInfo().getName()));
        userInfos.add(new Pair<>("접속 고객 Email ID", EntityManager.getInstance().getMe().getUserEmail()));
        userInfos.add(new Pair<>("접속 고객 팀명", EntityManager.getInstance().getTeamName()));
        userInfos.add(new Pair<>("접속 고객 팀ID", String.valueOf(EntityManager.getInstance().getTeamId())));

        return userInfos;
    }

    public SpannableStringBuilder getUserInfoSpans(List<Pair<String, String>> userInfos) {
        SpannableStringBuilder userInfoSpans = new SpannableStringBuilder();
        Observable.from(userInfos)
                .doOnNext(pair -> {
                    if (userInfoSpans.length() > 0) {
                        userInfoSpans.append("\n\n");
                    }
                    int startIndex = userInfoSpans.length();
                    userInfoSpans.append(pair.first);
                    int endIndex = userInfoSpans.length();
                    userInfoSpans.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                })
                .doOnNext(pair -> userInfoSpans.append("\n").append(pair.second))
                .subscribe();
        return userInfoSpans;
    }
}
