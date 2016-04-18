package com.tosslab.jandi.lib.sprinkler.io;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 7. 27..
 */
@RunWith(AndroidJUnit4.class)
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
            public ResponseBody performRequest() throws Exception {
                RequestClient client = requestManager.getClient(RequestClient.class);
                return client.post(new RequestBody(0, null, 0, null)).execute().body();
            }
        };

        ResponseBody responseBody = requestManager.requestWithRetry(request);

        System.out.println(responseBody != null ? responseBody.toString() : "empty");

        assertNotNull(responseBody);
    }

    @Test
    public void testPing() throws Exception {
        RequestManager.Request<ResponseBody> request = new RequestManager.Request<ResponseBody>() {
            @Override
            public ResponseBody performRequest() throws Exception {
                RequestClient client = requestManager.getClient(RequestClient.class);
                return client.ping().execute().body();
            }
        };

        ResponseBody responseBody = requestManager.request(request);

        boolean alive = responseBody != null;

        System.out.println(responseBody != null ? responseBody.toString() : "empty");

        assertTrue(alive);
    }
}