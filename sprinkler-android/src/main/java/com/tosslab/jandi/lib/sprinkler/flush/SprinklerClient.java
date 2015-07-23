package com.tosslab.jandi.lib.sprinkler.flush;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public interface SprinklerClient {
    @POST("/android")
    ResponseBody post(@Body RequestBody body);
}
