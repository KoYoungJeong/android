package com.tosslab.toss.app.network;

import android.util.Log;

import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * Spring Android 의 로그 출력을 위한 Interceptor
 */
public class LoggerInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Log.d("", "Request body: " + new String(body));

        ClientHttpResponse response = execution.execute(request, body);

        Log.d("", "Response Headers: " + response.getHeaders());

        return response;
    }
}
