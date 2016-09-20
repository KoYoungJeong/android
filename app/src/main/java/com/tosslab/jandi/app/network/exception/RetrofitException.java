package com.tosslab.jandi.app.network.exception;

public class RetrofitException extends Exception {
    private final int statusCode;
    private final int responseCode;
    private final String responseMessage;
    private final String rawBody;

    private RetrofitException(int statusCode, Throwable e) {
        super("Network Exception", e);
        this.statusCode = statusCode;
        this.responseCode = statusCode * 100;
        this.responseMessage = "";
        this.rawBody = "";
    }

    private RetrofitException(int statusCode, int responseCode, String responseMessage, String rawBody, Throwable e) {
        super("Network Exception", e);
        this.statusCode = statusCode;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.rawBody = rawBody;
    }

    public static RetrofitException create(int statusCode, Throwable e) {
        return new RetrofitException(statusCode, e);
    }

    public static RetrofitException create(int statusCode, int responseCode, String responseMessage, String rawBody, Throwable e) {
        return new RetrofitException(statusCode, responseCode, responseMessage, rawBody, e);
    }

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
                ", rawBody='" + rawBody + '\'' +
                "} " + super.toString();
    }

    public String getRawBody() {
        return rawBody;
    }
}
