package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import retrofit2.Call;

public class ApiTemplate<API> {

    private final RetrofitBuilder retrofitBuilder;
    private final Class<API> clazz;

    public ApiTemplate(Class<API> clazz, RetrofitBuilder retrofitBuilder) {
        this.clazz = clazz;
        this.retrofitBuilder = retrofitBuilder;
    }

    public <RESPONSE> RESPONSE call(Action0<RESPONSE> action0) throws RetrofitException {
        return PoolableRequestApiExecutor.obtain().execute(() -> {
            Call<RESPONSE> call = action0.call();
            if (!call.isExecuted()) {
                return call.execute();
            } else {
                return call.clone().execute();
            }
        });
    }

    protected API getApi() {
        return retrofitBuilder.create(clazz);
    }


    public interface Action0<RESULT> {
        Call<RESULT> call();
    }
}
