package com.tosslab.jandi.lib.sprinkler.io;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by tonyjs on 15. 7. 21..
 */
interface RequestClient {
    @POST("/android")
    ResponseBody post(@Body RequestBody body);
}
