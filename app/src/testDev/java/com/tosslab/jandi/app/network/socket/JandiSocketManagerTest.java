package com.tosslab.jandi.app.network.socket;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static junit.framework.Assert.fail;

@Ignore
@RunWith(JandiRobolectricGradleTestRunner.class)
public class JandiSocketManagerTest {

    private JandiSocketManager socketManager;

    @Before
    public void setUp() throws Exception {
        socketManager = JandiSocketManager.getInstance();

        BaseInitUtil.initData(RuntimeEnvironment.application);
        ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getAccountTeams().get(0);
        AccountRepository.getRepository().updateSelectedTeamInfo(userTeam.getTeamId());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();


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
                ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getSelectedTeamInfo();
                String name = AccountRepository.getRepository().getAccountInfo().getName();

                ConnectTeam connectTeam = new ConnectTeam("", UserAgentUtil.getDefaultUserAgent
                        (RuntimeEnvironment.application), userTeam.getTeamId(),
                        userTeam.getName(), userTeam.getMemberId(), name);
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
                ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getSelectedTeamInfo();
                String name = AccountRepository.getRepository().getAccountInfo().getName();

                ConnectTeam connectTeam = new ConnectTeam("", UserAgentUtil.getDefaultUserAgent
                        (RuntimeEnvironment.application), userTeam.getTeamId(),
                        userTeam.getName(), userTeam.getMemberId(), name);
                System.out.println("Connect Team Name : " + userTeam.getName());

                socketManager.sendByJson("connect_team", connectTeam);

            }
        });
    }
}