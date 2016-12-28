package com.tosslab.jandi.app.ui.profile.insert.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.insert.dagger.InsertProfileSecondPageModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tee on 16. 3. 17..
 */
@RunWith(AndroidJUnit4.class)
public class InsertProfileSecondPagePresenterTest {
    @Inject
    InsertProfileSecondPagePresenter presenter;
    private InsertProfileSecondPagePresenterImpl.View mockView;

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
        mockView = mock(InsertProfileSecondPagePresenterImpl.View.class);
        DaggerInsertProfileSecondPagePresenterTest_TestComponent.builder()
                .insertProfileSecondPageModule(new InsertProfileSecondPageModule(mockView))
                .build()
                .inject(this);
    }

    @Test
    public void testOnRequestProfile() {
        // given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).displayProfileInfos(any());

        // when
        presenter.requestProfile();

        await().until(() -> finish[0]);

        // then
        verify(mockView).displayProfileInfos(anyObject());
    }

    @Test
    public void testChooseEmail() {

        final boolean[] finish = new boolean[1];
        doAnswer(mock -> {
            finish[0] = true;
            return mock;
        }).when(mockView).showEmailChooseDialog(anyObject(), anyString());
        presenter.chooseEmail("dummy");
        await().until(() -> finish[0]);
        verify(mockView).showEmailChooseDialog(anyObject(), anyString());
    }

    @Test
    public void testSetEmail() {
        final boolean[] finish = new boolean[1];
        doAnswer(mock -> {
            finish[0] = true;
            return mock;
        }).when(mockView).setEmail(anyObject(), anyString());
        presenter.setEmail("dummy");
        await().until(() -> finish[0]);
        verify(mockView).setEmail(anyObject(), anyString());
    }

    @Test
    public void testUploadExtraInfo() {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).finish();

        presenter.uploadExtraInfo("dummy", "dummy", "dummy", "dummy");

        await().until(() -> finish[0]);

        verify(mockView).dismissProgressWheel();
    }

    @Component(modules = {ApiClientModule.class, InsertProfileSecondPageModule.class})
    interface TestComponent {
        void inject(InsertProfileSecondPagePresenterTest test);
    }

}