package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.network.spring.JacksonMapper;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class JandiNetworkException extends Exception {
    // ERROR
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;

    public static final int DATA_NOT_FOUND = 1839;
    public static final int INVALID_PASSWD = 1818;
    public static final int EXPIRED_SESSION = 2000;
    public static final int DUPLICATED_NAME = 4000;

    private static final String ERR_CODE = "code";
    private static final String ERR_MSG = "msg";

    public int errCode;
    public String errReason;
    public int httpStatusCode;
    public String httpStatusMessage;
    public String httpBody;

    public JandiNetworkException(RetrofitError e) {
        Response r = e.getResponse();
        this.httpStatusCode = r.getStatus();
        this.httpStatusMessage = r.getReason();

        ObjectMapper om = JacksonMapper.getInstance().getObjectMapper();
        try {
            Map<String, Object> m = om.readValue(
                    e.getBody().toString(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            this.errCode = m.containsKey(ERR_CODE) ? (Integer) m.get(ERR_CODE) : httpStatusCode;
            this.errReason = m.containsKey(ERR_MSG) ? (String) m.get(ERR_MSG) : httpStatusMessage;
            this.httpBody = e.getBody().toString();
        } catch (IOException e1) {
            this.errCode = -1;
            this.errReason = null;
        }
    }

    public String getErrorInfo() {
        return "[" + errCode + ":" + errReason + "]";
    }
}
