package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor;

import retrofit2.Retrofit;

/**
 * Created by Steve SeongUg Jung on 15. 6. 23..
 */
public interface RestAdapterDecor {
    Retrofit.Builder addRestAdapterProperty(Retrofit.Builder builder);


}
