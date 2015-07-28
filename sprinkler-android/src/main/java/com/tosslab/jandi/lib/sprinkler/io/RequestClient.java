package com.tosslab.jandi.lib.sprinkler.io;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by tonyjs on 15. 7. 21..
 */
interface RequestClient {
    @GET("/ping")
    ResponseBody ping();

    @POST("/log/android")
    ResponseBody post(@Body RequestBody body);
}
