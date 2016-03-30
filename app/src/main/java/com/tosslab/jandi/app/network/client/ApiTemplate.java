package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;

import retrofit2.Call;

public class ApiTemplate<API> {

    private final RetrofitAdapterBuilder retrofitAdapterBuilder;
    private final Class<API> clazz;

    public ApiTemplate(Class<API> clazz, RetrofitAdapterBuilder retrofitAdapterBuilder) {
        this.clazz = clazz;
        this.retrofitAdapterBuilder = retrofitAdapterBuilder;
    }

    public <RESPONSE> RESPONSE call(Action0<RESPONSE> action0) throws RetrofitException {
        return PoolableRequestApiExecutor.obtain().execute(() -> action0.call().execute());
    }

    protected API getApi() {
        return retrofitAdapterBuilder.create(clazz);
    }


    public interface Action0<RESULT> {
        Call<RESULT> call();
    }
}
