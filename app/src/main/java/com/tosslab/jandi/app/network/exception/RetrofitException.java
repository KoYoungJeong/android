package com.tosslab.jandi.app.network.exception;

public class RetrofitException extends Exception {
    private final int statusCode;
    private final int responseCode;
    private final String responseMessage;
    private final String rawBody;

    private RetrofitException(int statusCode) {
        this(statusCode, statusCode * 100, "", "");
    }

    private RetrofitException(int statusCode, int responseCode, String responseMessage, String rawBody) {
        this.statusCode = statusCode;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.rawBody = rawBody;
    }

    public static RetrofitException create(int statusCode) {return new RetrofitException(statusCode);}

    public static RetrofitException create(int statusCode, int responseCode, String responseMessage, String rawBody) {return new RetrofitException(statusCode, responseCode, responseMessage, rawBody);}

    public int getStatusCode() {
        return statusCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public String toString() {
        return "RetrofitException{" +
                "statusCode=" + statusCode +
                ", responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }

    public String getRawBody() {
        return rawBody;
    }
}
