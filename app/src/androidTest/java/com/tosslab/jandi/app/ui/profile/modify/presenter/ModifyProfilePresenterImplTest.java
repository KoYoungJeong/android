package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class ModifyProfilePresenterImplTest {

    private ModifyProfilePresenter presenter;
    private ModifyProfilePresenter.View mockView;
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }
    @Before
    public void setUp() throws Exception {
        presenter = ModifyProfilePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(ModifyProfilePresenter.View.class);
        presenter.setView(mockView);
    }

    @Test
    public void testOnRequestProfile() throws Exception {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.onRequestProfile();

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).dismissProgressWheel();
        Mockito.verify(mockView).displayProfile(Mockito.anyObject());
    }

    @Test
    public void testOnUpdateProfileExtraInfo() throws Exception {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.onUpdateProfileExtraInfo(null);

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).updateProfileFailed();
        Mockito.verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testUpdateProfileName() throws Exception {
        String originName = EntityManager.getInstance().getMe().getName();

        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.updateProfileName("hello");

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).updateProfileSucceed();
        Mockito.verify(mockView).successUpdateNameColor();
        Mockito.verify(mockView).dismissProgressWheel();

        presenter.updateProfileName(originName);
    }

    @Test
    public void testOnUploadEmail() throws Exception {

        {
            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).updateProfileFailed();


            presenter.onUploadEmail("hello@hello.com");

            Awaitility.await().until(() -> finish[0]);

            Mockito.verify(mockView).updateProfileFailed();
        }

        {
            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).updateProfileSucceed();

            String userEmail = EntityManager.getInstance().getMe().getUserEmail();
            presenter.onUploadEmail(userEmail);

            Awaitility.await().until(() -> finish[0]);

            Mockito.verify(mockView).updateProfileSucceed();
            Mockito.verify(mockView).successUpdateEmailColor();
        }

    }

    @Test
    public void testOnProfileChange() throws Exception {

        {
            presenter.onProfileChange(null);
            Mockito.verifyZeroInteractions(mockView);
        }

        {
            ResLeftSideMenu.User user = new ResLeftSideMenu.User();
            presenter.onProfileChange(user);
            Mockito.verifyZeroInteractions(mockView);
        }

        {
            ResLeftSideMenu.User user = EntityManager.getInstance().getMe().getUser();
            presenter.onProfileChange(user);
            Mockito.verify(mockView).displayProfile(Mockito.eq(user));
            Mockito.verify(mockView).closeDialogFragment();
        }
    }

    @Test
    public void testOnEditEmailClick() throws Exception {
        String email = "hello@hello.com";
        presenter.onEditEmailClick(email);

        Mockito.verify(mockView).showEmailChooseDialog(Mockito.any(), Mockito.eq(email));

    }

}