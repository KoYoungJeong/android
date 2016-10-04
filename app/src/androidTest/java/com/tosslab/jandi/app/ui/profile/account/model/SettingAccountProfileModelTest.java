package com.tosslab.jandi.app.ui.profile.account.model;

import android.support.test.espresso.core.deps.dagger.Component;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;

import setup.BaseInitUtil;

/**
 * Created by tee on 2016. 10. 4..
 */
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

    }

    @Component(modules = ApiClientModule.class)
    public interface SettingAccountProfileModelTestModule {
        void inject(SettingAccountProfileModelTest test);
    }

}