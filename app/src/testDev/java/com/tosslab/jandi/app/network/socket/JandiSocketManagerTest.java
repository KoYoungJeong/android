package com.tosslab.jandi.app.network.socket;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

@Ignore
@RunWith(RobolectricGradleTestRunner.class)
public class JandiSocketManagerTest {

    private JandiSocketManager socketManager;

    @Before
    public void setUp() throws Exception {
        socketManager = JandiSocketManager.getInstance();

        BaseInitUtil.initData(Robolectric.application);
        ResAccountInfo.UserTeam userTeam = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0);
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(userTeam.getTeamId());
    }

    @Test
    public void testLegacyConnect() throws Exception {

        final boolean[] isConnected = new boolean[1];

        Socket socket = IO.socket("https://websocket-elb-1241156836.ap-northeast-1.elb.amazonaws.com/");

        socket.connect();

        socket.on("check_connect_team", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println(args[0]);
                JandiAccountDatabaseManager accountDatabaseManager = JandiAccountDatabaseManager.getInstance(Robolectric.application);
                ResAccountInfo.UserTeam userTeam = accountDatabaseManager.getSelectedTeamInfo();
                String name = accountDatabaseManager.getAccountInfo().getName();

                ConnectTeam connectTeam = new ConnectTeam("", userAgent, userTeam.getTeamId(), userTeam.getName(), userTeam.getMemberId(), name);
                System.out.println("Connect Team Name : " + userTeam.getName());

                try {
                    socket.emit("connect_team", JacksonMapper.getInstance().getObjectMapper().writeValueAsString(connectTeam));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("connect_team", args -> {
            System.out.println(args[0]);
            isConnected[0] = true;
        });


        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isConnected[0];
            }
        });
    }

    @Test
    public void testConnect() throws Exception {

        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];

        connect();

        socketManager.register("connect_team", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;
                for (Object object : objects) {
                    System.out.println(object);
                }
            }
        });

        socketManager.register("error_connect_team", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = false;
                for (Object object : objects) {
                    System.out.println(object);
                }
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
                fail("Team Connect Fail");
            }
        } finally {
            socketManager.disconnect();
        }
    }

    @Test
    public void testMember() throws Exception {


        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];

        connect();

        socketManager.register("member_email_updated", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "Member Email Updated");
            }
        });
        socketManager.register("member_name_updated", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "Member Name Updated");
            }
        });
        socketManager.register("member_profile_updated", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "Member Profile Updated");
            }
        });


        Awaitility.setDefaultTimeout(30000, TimeUnit.MILLISECONDS);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ok[0];
            }
        });


        if (!success[0]) {
            fail("File Event Fail");
        }

        socketManager.disconnect();

    }

    @Ignore
    @Test
    public void testFile() throws Exception {
        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];

        connect();

        socketManager.register("file_created", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "File Created");
            }
        });

        socketManager.register("file_shared", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "File Shared");
            }
        });

        socketManager.register("file_unshared", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "File Unshared");
            }
        });

        socketManager.register("file_comment_created", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "File Comment Created");
            }
        });

        socketManager.register("file_comment_deleted", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;

                printEventObject(objects, "File Comment Deleted");
            }
        });

        Awaitility.setDefaultTimeout(30000, TimeUnit.MILLISECONDS);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ok[0];
            }
        });


        if (!success[0]) {
            fail("File Event Fail");
        }

        socketManager.disconnect();

    }

    @Ignore
    @Test
    public void testFileDelete() throws Exception {

        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];

        connect();

        socketManager.register("file_deleted", new EventListener() {
            @Override
            public void callback(Object... objects) {
                ok[0] = true;
                success[0] = true;
                printEventObject(objects, "File Deleted");
            }
        });

        Awaitility.setDefaultTimeout(30000, TimeUnit.MILLISECONDS);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ok[0];
            }
        });


        if (!success[0]) {
            fail("File Event Fail");
        }

        socketManager.disconnect();

    }

    private void printEventObject(Object[] objects, String tag) {
        for (Object object : objects) {
            System.out.println(tag + " : " + object);
        }
    }

    private void connect() {
        socketManager.connect(null);

        socketManager.register("check_connect_team", new EventListener() {
            @Override
            public void callback(Object... objects) {

                printEventObject(objects, "Check Connect");
                JandiAccountDatabaseManager accountDatabaseManager = JandiAccountDatabaseManager.getInstance(Robolectric.application);
                ResAccountInfo.UserTeam userTeam = accountDatabaseManager.getSelectedTeamInfo();
                String name = accountDatabaseManager.getAccountInfo().getName();

                ConnectTeam connectTeam = new ConnectTeam("", userAgent, userTeam.getTeamId(), userTeam.getName(), userTeam.getMemberId(), name);
                System.out.println("Connect Team Name : " + userTeam.getName());

                socketManager.sendByJson("connect_team", connectTeam);

            }
        });
    }
}