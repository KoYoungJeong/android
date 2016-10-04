package com.tosslab.jandi.app.ui.profile.account.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

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

    @Component(modules = ApiClientModule.class)
    public interface SettingAccountProfileModelTestComponent {
        void inject(SettingAccountProfileModelTest test);
    }

}