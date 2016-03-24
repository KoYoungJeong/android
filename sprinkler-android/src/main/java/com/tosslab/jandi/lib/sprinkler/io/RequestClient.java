package com.tosslab.jandi.lib.sprinkler.io;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by tonyjs on 15. 7. 21..
 */
interface RequestClient {
    @GET("ping")
    Call<ResponseBody> ping();

    @POST("log/android")
    Call<ResponseBody> post(@Body RequestBody body);
}
