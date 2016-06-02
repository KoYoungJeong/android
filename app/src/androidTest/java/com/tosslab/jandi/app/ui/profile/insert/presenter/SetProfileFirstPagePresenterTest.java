package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        mockView = mock(SetProfileFirstPagePresenter.View.class);
        presenter.setView(mockView);
    }

    @Test
    public void testRequestProfile() {
        // given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        // when
        presenter.requestProfile();

        Awaitility.await().until(() -> finish[0]);

        // then
        verify(mockView).showProgressWheel();
        verify(mockView).displayProfileName(anyString());
        verify(mockView).setTeamName(anyString());
        verify(mockView).displayProfileImage(anyObject());
        verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testUpdateProfileName() {
        // given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        //when
        presenter.updateProfileName("dummyname");

        Awaitility.await().until(() -> finish[0]);

        //then
        verify(mockView).showProgressWheel();
        verify(mockView).dismissProgressWheel();
        verify(mockView).displayProfileName(anyString());
    }

    @Test
    public void testOnProfileImageChange() {
        // given
        User user = mock(User.class);
        doReturn(TeamInfoLoader.getInstance().getMyId())
                .when(user)
                .getId();

        // when
        presenter.onProfileImageChange(user);

        //then
        verify(mockView).displayProfileImage(anyObject());
    }

}