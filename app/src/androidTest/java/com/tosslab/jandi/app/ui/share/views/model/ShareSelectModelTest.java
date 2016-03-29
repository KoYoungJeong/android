package com.tosslab.jandi.app.ui.share.views.model;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 2015. 11. 3..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShareSelectModelTest {

    ShareSelectModel shareSelectModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();

        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        AccountRepository.getRepository().updateSelectedTeamInfo(accountTeams.get(0).getTeamId());

        shareSelectModel = ShareSelectModel_.getInstance_(JandiApplication.getContext());

        Observable.from(accountTeams)
                .subscribe(userTeam -> {
                    try {
                        ResLeftSideMenu leftSideMenu = shareSelectModel.getLeftSideMenu(userTeam.getTeamId());
                        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void testInitFormattedEntities() throws Exception {

        long teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();

        ResLeftSideMenu currentLeftSideMenu = LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId);
        shareSelectModel.initFormattedEntities(currentLeftSideMenu);
        FormattedEntity entity = shareSelectModel.getEntityById(currentLeftSideMenu.user.id);

        assertThat(entity, is(notNullValue()));
        assertThat(entity.getName(), is(equalTo(currentLeftSideMenu.user.name)));

    }
}