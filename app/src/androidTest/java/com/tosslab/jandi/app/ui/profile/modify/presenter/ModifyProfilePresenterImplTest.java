package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.dagger.ModifyProfileModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import setup.BaseInitUtil;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(AndroidJUnit4.class)
public class ModifyProfilePresenterImplTest {

    @Inject
    ModifyProfilePresenter presenter;
    private ModifyProfilePresenter.View mockView;

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
        mockView = mock(ModifyProfilePresenter.View.class);
        DaggerModifyProfileTestComponent.builder()
                .modifyProfileModule(new ModifyProfileModule(mockView, TeamInfoLoader.getInstance().getMyId()))
                .build()
                .inject(this);

    }

    @Test
    public void testOnRequestProfile() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.onRequestProfile();

        Awaitility.await().until(() -> finish[0]);

        verify(mockView).showProgressWheel();
        verify(mockView).dismissProgressWheel();
        verify(mockView).displayProfile(anyObject());
    }

    @Test
    public void testOnUpdateProfileExtraInfo() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.onUpdateProfile(null);

        Awaitility.await().until(() -> finish[0]);

        verify(mockView).showProgressWheel();
        verify(mockView).updateProfileFailed();
        verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testUpdateProfileName() throws Exception {
        String originName = TeamInfoLoader.getInstance().getName(TeamInfoLoader.getInstance().getMyId());

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.name = "hello";
        presenter.onUpdateProfile(reqUpdateProfile);

        Awaitility.await().until(() -> finish[0]);

        verify(mockView).showProgressWheel();
        verify(mockView).updateProfileSucceed();
        verify(mockView).dismissProgressWheel();

        reqUpdateProfile.name = originName;
        presenter.onUpdateProfile(reqUpdateProfile);
    }

    @Test
    public void testOnUploadEmail() throws Exception {

        {
            final boolean[] finish = {false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).updateProfileFailed();


            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.email = "hello@hello.com";
            presenter.onUpdateProfile(reqUpdateProfile);

            Awaitility.await().until(() -> finish[0]);

            verify(mockView).updateProfileFailed();
        }

        {
            final boolean[] finish = {false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).updateProfileSucceed();

            String userEmail = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).getEmail();
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.email = userEmail;
            presenter.onUpdateProfile(reqUpdateProfile);

            Awaitility.await().until(() -> finish[0]);

            verify(mockView).updateProfileSucceed();
        }

    }

    @Test
    public void testOnProfileChange() throws Exception {

        {
            presenter.onProfileChange(null);
            verifyZeroInteractions(mockView);
        }

        {
            reset(mockView);
            User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
            presenter.onProfileChange(user);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            verify(mockView).displayProfile(eq(user));
            verify(mockView).closeDialogFragment();
        }
    }

    @Test
    public void testOnEditEmailClick() throws Exception {
        presenter.onEditEmailClick();

        verify(mockView).showEmailChooseDialog(any(), any());

    }

}