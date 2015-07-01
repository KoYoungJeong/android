package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.converter;

/**
 * Created by tee on 15. 6. 20..
 */
public class ResponseLogger {

    private static final ResponseLogger responseLogger = new ResponseLogger();

    private ResponseLogger() {
    }

    public static final ResponseLogger getInstance() {
        return responseLogger;
    }

}
