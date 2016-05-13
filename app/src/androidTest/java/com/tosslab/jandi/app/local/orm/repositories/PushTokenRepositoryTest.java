package com.tosslab.jandi.app.local.orm.repositories;

import android.support.test.runner.AndroidJUnit4;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.PushToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(AndroidJUnit4.class)
public class PushTokenRepositoryTest {

    private PushTokenRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = PushTokenRepository.getInstance();
        OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        TableUtils.clearTable(helper.getConnectionSource(), PushToken.class);

    }

    @Test
    public void testRepository() throws Exception {
        List<PushToken> pushTokenList;
        {
            repository.upsertPushToken(new PushToken("xxx", "asd"));
            pushTokenList = repository.getPushTokenList();
            assertThat(pushTokenList)
                    .isNotNull()
                    .isEmpty();
        }

        {
            repository.upsertPushToken(new PushToken("gcm","gcm1"));
            pushTokenList = repository.getPushTokenList();

            assertThat(pushTokenList)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(1);
        }

        {
            repository.upsertPushToken(new PushToken("baidu","qwe1"));
            pushTokenList = repository.getPushTokenList();

            assertThat(pushTokenList)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(2);
        }

        {
            repository.deletePushToken();
            pushTokenList = repository.getPushTokenList();

            assertThat(pushTokenList)
                    .isNotNull()
                    .isEmpty();
        }
    }
}