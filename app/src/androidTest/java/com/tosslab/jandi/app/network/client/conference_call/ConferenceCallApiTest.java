package com.tosslab.jandi.app.network.client.conference_call;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.GooroomeeRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqGooroomeeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeeOtp;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by tee on 2017. 6. 7..
 */
public class ConferenceCallApiTest {


    private ConferenceCallApi conferenceCallApi;

    @Before
    public void setUp() throws Exception {
        conferenceCallApi = new ConferenceCallApi(GooroomeeRetrofitBuilder.getInstance());
    }

    @Test
    public void getGooroomeOtp() throws Exception {
        ReqGooroomeeOtp reqGooroomeOtp = new ReqGooroomeeOtp();
        reqGooroomeOtp.roomId = "994e726e327048d99b5b95b0021fce81";
        reqGooroomeOtp.userName = "hahahaha";
        reqGooroomeOtp.roleId = "speaker";
        ResGooroomeeOtp resGooroomeOtp = conferenceCallApi.getGooroomeOtp(reqGooroomeOtp);
        LogUtil.e("res", resGooroomeOtp.toString());
    }


}