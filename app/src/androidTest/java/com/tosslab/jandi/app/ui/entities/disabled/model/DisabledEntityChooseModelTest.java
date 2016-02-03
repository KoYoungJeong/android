package com.tosslab.jandi.app.ui.entities.disabled.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DisabledEntityChooseModelTest {

    private DisabledEntityChooseModel model;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        model = DisabledEntityChooseModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testGetDisabledMembers() throws Exception {
        List<ChatChooseItem> disabledMembers = model.getDisabledMembers();
        assertThat(disabledMembers.size(), is(greaterThanOrEqualTo(0)));
    }
}