package com.tosslab.jandi.lib.sprinkler.io;

import com.tosslab.jandi.lib.sprinkler.SprinklerTestApplication;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import retrofit.RetrofitError;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 7. 27..
 */
@Config(
        application = SprinklerTestApplication.class,
        manifest = "src/main/AndroidManifest.xml",
        emulateSdk = 18
)
public class RequestManagerTest {

    private RequestManager requestManager;

    @Before
    public void setup() throws Exception {
        requestManager = RequestManager.get();
    }

    @Test
    public void testRequestPost() throws Exception {
        RequestManager.Request<ResponseBody> request = new RequestManager.Request<ResponseBody>() {
            @Override
            public ResponseBody performRequest() throws RetrofitError {
                RequestClient client = requestManager.getClient(RequestClient.class);
                return client.post(new RequestBody(0, null, 0, null));
            }
        };
        requestManager.requestWithRetry(request);
    }

    @Test
    public void testPing() throws Exception {
        RequestManager.Request<ResponseBody> request = new RequestManager.Request<ResponseBody>() {
            @Override
            public ResponseBody performRequest() throws RetrofitError {
                RequestClient client = requestManager.getClient(RequestClient.class);
                return client.ping();
            }
        };
        ResponseBody responseBody = requestManager.request(request);

        System.out.println(responseBody != null ? responseBody.toString() : "empty");

        boolean alive = responseBody != null && responseBody.isSuccess();

        assertTrue(alive);
    }
}