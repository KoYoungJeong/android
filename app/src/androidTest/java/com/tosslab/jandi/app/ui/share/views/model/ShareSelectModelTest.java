package com.tosslab.jandi.app.ui.share.views.model;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShareSelectModelTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        AccountRepository.getRepository().updateSelectedTeamInfo(accountTeams.get(0).getTeamId());

        Observable.from(accountTeams)
                .subscribe(userTeam -> {
                    try {
                        long teamId = userTeam.getTeamId();
                        String initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
                        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void testInitFormattedEntities() throws Exception {

        long teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();


        RawInitialInfo rawInitialInfo = InitialInfoRepository.getInstance().getRawInitialInfo(teamId);
        InitialInfo initialInfo = JacksonMapper.getInstance().getObjectMapper().readValue(rawInitialInfo.getRawValue(), InitialInfo.class);

        TeamInfoLoader instance = TeamInfoLoader.getInstance(teamId);
        User entity = instance.getUser(initialInfo.getSelf().getId());

        assertThat(entity, is(notNullValue()));
        assertThat(entity.getName(), is(equalTo(Observable.from(initialInfo.getMembers())
                .takeFirst(human -> human.getId() == initialInfo.getSelf().getId())
                .map(Human::getName)
                .toBlocking()
                .firstOrDefault(""))));

    }
}