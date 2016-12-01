package com.tosslab.jandi.app.ui.profile.account.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.junit.Assert.fail;

/**
 * Created by tee on 2016. 10. 4..
 */
@RunWith(AndroidJUnit4.class)
public class SettingAccountProfileModelTest {

    @Inject
    SettingAccountProfileModel settingAccountProfileModel;

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
        DaggerSettingAccountProfileModelTest_SettingAccountProfileModelTestComponent
                .builder().build().inject(this);
    }

    @Test
    public void testGetName() {
        //When
        String name = settingAccountProfileModel.getName();
        //Then
        Assert.assertNotNull(name);
    }

    @Test
    public void testGetPrimaryEmail() {
        String primaryEmail = settingAccountProfileModel.getPrimaryEmail();
        List<ResAccountInfo.UserEmail> emails =
                AccountRepository.getRepository().getAccountEmails();
        boolean assertResult = false;
        for (ResAccountInfo.UserEmail email : emails) {
            if (email.isPrimary()) {
                if (email.getId().equals(primaryEmail)) {
                    assertResult = true;
                }
            }
        }
        Assert.assertTrue(assertResult);
    }

    @Test
    public void testGetAccountEmails() {
        //Given
        List<ResAccountInfo.UserEmail> userEmails =
                AccountRepository.getRepository().getAccountEmails();

        //When
        String[] emailArray = settingAccountProfileModel.getAccountEmails();

        //Then
        Assert.assertTrue(userEmails.size() >= emailArray.length);
    }

    @Test
    public void testUpdateProfileEmail() {

        //Given
        String primaryEmail = getPrimaryEmail();

        //When
        settingAccountProfileModel.updateProfileEmail("a@a.com");

        //Then
        if (getPrimaryEmail().equals("a@a.com")) {
            fail("it cannot be");
        }

        //Restore
        settingAccountProfileModel.updateProfileEmail(primaryEmail);
    }

    private String getPrimaryEmail() {
        String primaryEmail = AccountRepository.getRepository().getAccountEmails().get(0).getId();
        List<ResAccountInfo.UserEmail> emails = AccountRepository.getRepository().getAccountEmails();
        for (ResAccountInfo.UserEmail email : emails) {
            if (email.isPrimary()) {
                primaryEmail = email.getId();
            }
        }
        return primaryEmail;
    }


    @Component(modules = ApiClientModule.class)
    public interface SettingAccountProfileModelTestComponent {
        void inject(SettingAccountProfileModelTest test);
    }

}