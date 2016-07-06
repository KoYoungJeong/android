package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import setup.BaseInitUtil;

import static org.mockito.Mockito.mock;

/**
 * Created by tee on 16. 3. 17..
 */
public class SetProfileSecondPagePresenterTest {
    private InsertProfileSecondPagePresenterImpl presenter;
    private InsertProfileSecondPagePresenterImpl.View mockView;
    private ModifyProfileModel mockModel;

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
        mockModel = mock(ModifyProfileModel.class);
        presenter = new InsertProfileSecondPagePresenterImpl(mockModel, mockView);
    }


    @Test
    public void testOnRequestProfile() {
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
        Mockito.verify(mockView).displayProfileInfos(Mockito.anyObject());
        Mockito.verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testChooseEmail() {
        presenter.chooseEmail("dummy");
        Mockito.verify(mockView).showEmailChooseDialog(Mockito.anyObject(), Mockito.anyString());
    }

    @Test
    public void testSetEmail() {
        presenter.setEmail("dummy");
        Mockito.verify(mockView).setEmail(Mockito.anyObject(), Mockito.anyString());
    }

    @Test
    public void testUploadExtraInfo() {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.uploadExtraInfo("dummy", "dummy", "dummy", "dummy");

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(mockView).updateProfileSucceed();
    }

}