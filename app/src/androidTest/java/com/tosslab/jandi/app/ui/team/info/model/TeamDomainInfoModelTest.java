package com.tosslab.jandi.app.ui.team.info.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TeamDomainInfoModelTest {

    private TeamDomainInfoModel teamDomainInfoModel;

    @Before
    public void setUp() throws Exception {
        teamDomainInfoModel = TeamDomainInfoModel_.getInstance_(JandiApplication.getContext());

    }

    @Test
    public void testValidDomain() throws Exception {
        {
            ResValidation validation = teamDomainInfoModel.validDomain("tosslab");
            assertThat(validation.isValidate(), is(false));
        }
        {
            ResValidation validation = teamDomainInfoModel.validDomain("tosslabsjeyw");
            assertThat(validation.isValidate(), is(true));
        }
    }
}