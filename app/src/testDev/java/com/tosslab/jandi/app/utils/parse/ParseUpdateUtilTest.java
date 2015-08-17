package com.tosslab.jandi.app.utils.parse;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import static org.junit.Assert.assertTrue;

/**
 * Created by Steve SeongUg Jung on 15. 6. 10..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class ParseUpdateUtilTest {
    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }


    @Test
    public void testRxJavaCollect() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        Observable.from(userTeams)
                .map(new Func1<ResAccountInfo.UserTeam, ResLeftSideMenu>() {
                    @Override
                    public ResLeftSideMenu call(ResAccountInfo.UserTeam userTeam) {
                        return RequestApiManager.getInstance().getInfosForSideMenuByMainRest(userTeam.getTeamId());
                    }
                })
                .map(new Func1<ResLeftSideMenu, List<String>>() {
                    @Override
                    public List<String> call(ResLeftSideMenu resLeftSideMenu) {

                        int myId = resLeftSideMenu.user.id;

                        List<String> subscribeList = new ArrayList<String>();
                        String parseChannel;
                        for (ResLeftSideMenu.Entity joinEntity : resLeftSideMenu.joinEntities) {
                            if (joinEntity instanceof ResLeftSideMenu.Channel) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id;
                            } else if (joinEntity instanceof ResLeftSideMenu.PrivateGroup) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id;
                            } else if (joinEntity instanceof ResLeftSideMenu.User) {
                                parseChannel = JandiConstants.PUSH_CHANNEL_PREFIX + joinEntity.id + "-" + myId;
                            } else {
                                parseChannel = "";
                            }

                            if (!TextUtils.isEmpty(parseChannel)) {
                                subscribeList.add(parseChannel);
                            }
                        }

                        return subscribeList;

                    }
                })
                .collect(new Func0<List<String>>() {
                    @Override
                    public List<String> call() {
                        return new ArrayList<String>();
                    }
                }, new Action2<List<String>, List<String>>() {
                    @Override
                    public void call(List<String> strings, List<String> strings2) {
                        strings.addAll(strings2);
                    }
                })
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        assertTrue(strings.size() > 0);
                    }
                });
    }
}