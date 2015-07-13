package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 6. 23..
 */
public interface RestAdapterDecor {
    RestAdapter.Builder addRestAdapterProperty(RestAdapter.Builder builder);
}
