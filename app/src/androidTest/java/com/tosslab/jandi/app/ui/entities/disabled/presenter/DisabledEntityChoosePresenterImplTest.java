package com.tosslab.jandi.app.ui.entities.disabled.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.entities.disabled.model.DisabledEntityChooseModel;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class DisabledEntityChoosePresenterImplTest {

    private DisabledEntityChoosePresenterImpl presenter;
    private DisabledEntityChoosePresenter.View mockView;

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
        mockView = mock(DisabledEntityChoosePresenter.View.class);
        presenter = new DisabledEntityChoosePresenterImpl(mockView, new DisabledEntityChooseModel());
    }



    @Test
    public void testInitDisabledMembers() throws Exception {
        presenter.initDisabledMembers();
        verify(mockView).setDisabledMembers(anyList());
    }
}