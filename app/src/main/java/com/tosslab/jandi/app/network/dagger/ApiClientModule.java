package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.BaseRetrofitBuilder;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.GooroomeeRetrofitBuilder;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {

    @Provides
    InnerApiRetrofitBuilder provideInnerApiRetrofitAdapterBuilder() {
        return InnerApiRetrofitBuilder.getInstance();
    }

    @Provides
    BaseRetrofitBuilder provideBaseRetrofitAdapterBuilder() {
        return BaseRetrofitBuilder.getInstance();
    }

    @Provides
    GooroomeeRetrofitBuilder provideGooroomyRetrofitBuilder() {
        return GooroomeeRetrofitBuilder.getInstance();
    }

}