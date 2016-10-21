package com.tosslab.jandi.app;


import retrofit2.Response;

public class ValidationUtil {
    public static boolean isDeprecated(Response response) {

        String deprecated = response.headers().get("X-API-Deprecated");
        return deprecated != null
                && deprecated.length() > 0;

    }
}
