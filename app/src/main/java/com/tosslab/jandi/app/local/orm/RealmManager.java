package com.tosslab.jandi.app.local.orm;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.local.orm.upgrade.RealmUpgradeChecker;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.TeamPlan;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Rank;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import rx.Observable;

public class RealmManager {
    private static long REALM_VERSION_MEMBER_AUTHORITY = 2;
    private static long REALM_VERSION_READ_ONLY_OF_TOPIC = 3;

    public static void init(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (Process.myPid() == processInfo.pid) {
                if (TextUtils.equals(processInfo.processName, BuildConfig.APPLICATION_ID)) {
                    if (JandiPreference.getRealmInitiateStamp() < 246) {
                        // 2.5.1.6 이전 버전 사용자, 설치 후 처음 사용자를 대상으로 realm 데이터 초기화
                        Realm.init(context);
                        Realm.deleteRealm(new RealmConfiguration.Builder().build());
                        JandiPreference.setRealmInitiateStamp();
                    }

                    // proccess 선언이 되어 있지 않은 것에 한해서 동작하도록 함
                    Realm.init(context);
                    Realm.setDefaultConfiguration(realmConfiguration());
                }
                break;
            }
        }
    }

    private static RealmConfiguration realmConfiguration() {
        return new RealmConfiguration.Builder()
                .schemaVersion(REALM_VERSION_MEMBER_AUTHORITY)
                .migration(migration())
                .build();
    }

    private static RealmMigration migration() {
        return (realm, oldVersion, newVersion) -> {
            if (oldVersion < newVersion) {
                RealmSchema schema = realm.getSchema();
                List<RealmUpgradeChecker> upgradeCheckers = Arrays.asList(
                        RealmUpgradeChecker.create(() -> REALM_VERSION_MEMBER_AUTHORITY, () -> {
                            schema.get(TeamPlan.class.getSimpleName())
                                    .addField("messageCount", long.class);

                            schema.get(Human.class.getSimpleName())
                                    .addField("rankId", long.class);

                            schema.create(Rank.class.getSimpleName())
                                    .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                                    .addField("name", String.class)
                                    .addField("level", int.class)
                                    .addField("teamId", long.class)
                                    .addField("updatedAt", Date.class)
                                    .addField("createdAt", Date.class)
                                    .addField("status", String.class);

                        }),
                        RealmUpgradeChecker.create(() -> REALM_VERSION_READ_ONLY_OF_TOPIC, () -> {
                            schema.get(Topic.class.getSimpleName())
                                    .addField("isAnnouncement", boolean.class);
                        })
                );

                Observable.from(upgradeCheckers)
                        .subscribe(upgradeChecker -> upgradeChecker.run(oldVersion), Throwable::printStackTrace);

            }
        };
    }
}
