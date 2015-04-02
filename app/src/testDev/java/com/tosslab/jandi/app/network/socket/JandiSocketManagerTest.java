package com.tosslab.jandi.app.network.socket;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiSocketManagerTest {

    private JandiSocketManager socketManager;

    @Before
    public void setUp() throws Exception {
        socketManager = JandiSocketManager.getInstance();

        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    public void testConnect() throws Exception {

        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];


        socketManager.connect(new EventListener() {
            @Override
            public void callback(Object... objects) {
                JandiAccountDatabaseManager accountDatabaseManager = JandiAccountDatabaseManager.getInstance(Robolectric.application);
                List<ResAccountInfo.UserTeam> userTeams = accountDatabaseManager.getUserTeams();
                String name = accountDatabaseManager.getAccountInfo().getName();

                ResAccountInfo.UserTeam userTeam = userTeams.get(0);
                ConnectTeam connectTeam = new ConnectTeam(userTeam.getTeamId(), userTeam.getName(), userTeam.getMemberId(), name);

                socketManager.sendByJson("connect_team", connectTeam);

            }
        });

        socketManager.register("connect_team", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;
            }
        });

        socketManager.register("error_connect_team", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = false;
            }
        });

        Awaitility.setDefaultTimeout(30000, TimeUnit.MILLISECONDS);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ok[0];
            }
        });

        try {

            if (!success[0]) {
                fail("Access Fail");
            }
        } finally {

            socketManager.disconnect();
        }


    }
}