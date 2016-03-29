package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.utils.TokenUtil;

import retrofit2.Call;

public class ApiTemplate<API> {

    private final API api;

    public ApiTemplate(Class<API> clazz) {
        api = RetrofitAdapterBuilder
                .newInstance()
                .create(clazz);
    }

    public String getToken() {
        return TokenUtil.getRequestAuthentication();
    }

    public <RESPONSE> RESPONSE call(Action0<RESPONSE> action0) throws RetrofitException {
        return PoolableRequestApiExecutor.obtain().execute(() -> action0.call().execute());
    }

    protected API getApi() {
        return api;
    }


    public interface Action0<RESULT> {
        Call<RESULT> call();
    }
}
