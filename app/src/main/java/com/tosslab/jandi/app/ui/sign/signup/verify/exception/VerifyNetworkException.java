package com.tosslab.jandi.app.ui.sign.signup.verify.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 6. 1..
 */
public class VerifyNetworkException extends Exception {
    public static final String DATA = "data";
    public static final int NONE_TRY_COUNT = -1;

    public int errCode;
    public String errReason;
    private int tryCount = NONE_TRY_COUNT;

    public VerifyNetworkException(RetrofitException e) {
        errCode = e.getResponseCode();
        errReason = e.getResponseMessage();
        try {
            ExceptionData exceptionData = JsonMapper.getInstance().getObjectMapper().readValue(e.getRawBody(), ExceptionData.class);
            LogUtil.d(e.getRawBody());
            ExceptionData.TryData tryData = exceptionData.getData();
            tryCount = tryData != null ? tryData.getTryCount() : NONE_TRY_COUNT;
        } catch (Exception ex) {
            ex.printStackTrace();
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
