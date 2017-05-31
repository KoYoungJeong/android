package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

/**
 * Created by tee on 2017. 5. 30..
 */

public interface RetrofitBuilder {

    <CLIENT> CLIENT create(Class<CLIENT> clazz);
}
