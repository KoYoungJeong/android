package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import setup.BaseInitUtil;

/**
 * Created by tee on 16. 3. 17..
 */
public class SetProfileFirstPagePresenterTest {

    private SetProfileFirstPagePresenter presenter;
    private SetProfileFirstPagePresenter.View mockView;

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
        presenter = SetProfileFirstPagePresenter_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(SetProfileFirstPagePresenter.View.class);
        presenter.setView(mockView);
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