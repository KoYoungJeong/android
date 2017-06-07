package com.tosslab.jandi.app.network.client.conference_call;

import com.tosslab.jandi.app.network.models.ReqGooroomeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeOtp;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.junit.Test;

/**
 * Created by tee on 2017. 6. 7..
 */
public class ConferenceCallApiTest {
    @Test
    public void getGooroomeOtp() throws Exception {
        ReqGooroomeOtp reqGooroomeOtp = new ReqGooroomeOtp();
        reqGooroomeOtp.setRoomId("72988b0f5f3b414da9a6714967f22fc4");
        reqGooroomeOtp.setUserName("hahahaha");
        reqGooroomeOtp.setRoleId("participant");
        ResGooroomeOtp resGooroomeOtp = ConferenceCallApi.get().getGooroomeOtp(reqGooroomeOtp);
        LogUtil.e("res", resGooroomeOtp.toString());
    }
}