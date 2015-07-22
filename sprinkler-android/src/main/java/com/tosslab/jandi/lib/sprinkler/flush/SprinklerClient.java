package com.tosslab.jandi.lib.sprinkler.flush;

import retrofit.http.POST;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public interface SprinklerClient {
//    @POST("/android")
    @POST("/repo")
    String post();
}
