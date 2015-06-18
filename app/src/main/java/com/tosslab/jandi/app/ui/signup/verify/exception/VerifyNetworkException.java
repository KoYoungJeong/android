package com.tosslab.jandi.app.ui.signup.verify.exception;

import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;

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

    public VerifyNetworkException(HttpStatusCodeException e) {
        this.httpStatusCode = e.getStatusCode().value();
        this.httpStatusMessage = e.getStatusText();

        ObjectMapper objectMapper = JacksonMapper.getInstance().getObjectMapper();
        try {
            String responseBodyAsString = e.getResponseBodyAsString();
            LogUtil.e(responseBodyAsString);
            ExceptionData data = objectMapper.readValue(responseBodyAsString,
                    new TypeReference<ExceptionData>() {
                    });

            if (data != null) {
                LogUtil.e(data.toString());
            }
            errCode = data != null ? data.code : httpStatusCode;
            errReason = data != null ? data.msg : httpStatusMessage;
            ExceptionData.TryData tryData = data != null ? data.getData() : null;
            tryCount = tryData != null ? tryData.getTryCount() : NONE_TRY_COUNT;
        } catch (IOException e1) {
            e1.printStackTrace();
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
            return "tryData = " + data;
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
