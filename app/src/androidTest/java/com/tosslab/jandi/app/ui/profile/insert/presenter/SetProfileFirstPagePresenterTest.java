package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import setup.BaseInitUtil;

/**
 * Created by tee on 16. 3. 17..
 */
public class SetProfileFirstPagePresenterTest {

    private SetProfileFirstPagePresenter presenter;
    private SetProfileFirstPagePresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        presenter = SetProfileFirstPagePresenter_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(SetProfileFirstPagePresenter.View.class);
        presenter.setView(mockView);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testRequestProfile() {
        // given
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        // when
        presenter.requestProfile();

        Awaitility.await().until(() -> finish[0]);

        // then
        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).displayProfileName(Mockito.anyString());
        Mockito.verify(mockView).setTeamName(Mockito.anyString());
        Mockito.verify(mockView).displayProfileImage(Mockito.anyObject());
        Mockito.verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testUpdateProfileName() {
        // given
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        //when
        presenter.updateProfileName("dummyname");

        Awaitility.await().until(() -> finish[0]);

        //then
        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).dismissProgressWheel();
        Mockito.verify(mockView).displayProfileName(Mockito.anyString());
    }

    @Test
    public void testOnProfileImageChange() {
        // given
        ResLeftSideMenu resLeftSideMenu = new ResLeftSideMenu();
        resLeftSideMenu.user = new ResLeftSideMenu.User();
        resLeftSideMenu.user.id = EntityManager.getInstance().getMe().getId();

        // when
        presenter.onProfileImageChange(resLeftSideMenu.user);

        //then
        Mockito.verify(mockView).displayProfileImage(Mockito.anyObject());
    }

}