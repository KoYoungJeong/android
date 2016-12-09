package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {

    @Provides
    RetrofitBuilder provideRetrofitAdapterBuilder() {
        return RetrofitBuilder.getInstance();
    }

}
