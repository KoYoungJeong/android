package com.tosslab.jandi.app.network.client.account.devices;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class DeviceApiTest {

    private static final String SAMPLE_TOKEN = "sdkjfhlakjdfhlkajsdhflkajshdf";

    private DeviceApi deviceApi;

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
        deviceApi = new DeviceApi(RetrofitBuilder.newInstance());

    }

}