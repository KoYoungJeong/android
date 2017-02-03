package com.tosslab.jandi.app;


import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

public class ValidationUtil {
    public static boolean isDeprecated(Call call) throws IOException {

        Request request = call.request();
        String method = request.method();
        HttpUrl url = request.url();
        Response response = call.execute();
        Headers headers = response.headers();
        Map<String, String> headerMap = new HashMap<>();
        for (String name : headers.names()) {
            headerMap.put(name, headers.get(name));
        }
        String json = new GsonBuilder().create().toJson(headerMap);

        StringBuilder builder = new StringBuilder();
        builder.append(method).append(" - ").append(url)
                .append("\nResponse Header : ").append(json);

        String deprecated = headers.get("X-API-Deprecated");
        boolean find = deprecated != null && deprecated.length() > 0;

        if (find) {
            builder.insert(0, "\n:::::::::::::::\n")
                    .append("\nit has \"X-API-Deprecated\"\n")
                    .append(":::::::::::::::");
            OkHttpClientTestFactory.logger.log(Level.WARNING, builder.toString());
        } else {
            builder.insert(0, "\n");
            OkHttpClientTestFactory.logger.log(Level.INFO, builder.toString());
        }

        return find;

    }
}
