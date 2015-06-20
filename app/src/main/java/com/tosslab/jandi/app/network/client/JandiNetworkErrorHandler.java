package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.utils.JandiNetworkException;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 18..
 */
public class JandiNetworkErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        return new JandiNetworkException(cause);
    }
}