package com.tosslab.jandi.app.ui.entities.disabled.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;

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

    @Before
    public void setUp() throws Exception {
        presenter = DisabledEntityChoosePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = mock(DisabledEntityChoosePresenter.View.class);
        presenter.setView(mockView);
    }



    @Test
    public void testInitDisabledMembers() throws Exception {
        presenter.initDisabledMembers();
        verify(mockView).setDisabledMembers(anyList());
    }
}