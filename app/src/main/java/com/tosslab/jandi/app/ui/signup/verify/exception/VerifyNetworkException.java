package com.tosslab.jandi.app.ui.signup.verify.exception;

import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tonyjs on 15. 6. 1..
 */
public class VerifyNetworkException extends Exception {
    public static final String DATA = "data";
    public static final int NONE_TRY_COUNT = -1;

    public int httpStatusCode;
    public String httpStatusMessage;
    public String httpBody;

    public int errCode;
    public String errReason;
    private int tryCount = NONE_TRY_COUNT;

    public VerifyNetworkException(RetrofitError e) {
        this.httpStatusCode = e.getResponse().getStatus();
        this.httpStatusMessage = e.getMessage();

        try {
            ExceptionData exceptionData = (ExceptionData) e.getBodyAs(ExceptionData.class);
            LogUtil.d(exceptionData.toString());
            errCode = exceptionData.code;
            errReason = exceptionData.msg;
            ExceptionData.TryData tryData = exceptionData.getData();
            tryCount = tryData != null ? tryData.getTryCount() : NONE_TRY_COUNT;
        } catch (RuntimeException runtimeException) {
            runtimeException.printStackTrace();
        }
    }

    public String getErrorInfo() {
        return "[" + errCode + ":" + errReason + "]";
    }

    public int getTryCount() {
        return tryCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class ExceptionData {
        private int code;
        private String msg;
        private TryData data;

        public TryData getData() {
            return data;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return msg;
        }

        @Override
        public String toString() {
            return "ExceptionData{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", data=" + data +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class TryData {
            private int tryCount;

            public int getTryCount() {
                return tryCount;
            }

            @Override
            public String toString() {
                return "tryCount = " + tryCount;
            }
        }
    }
}
