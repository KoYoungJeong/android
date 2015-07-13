package com.tosslab.jandi.app.network;

///**
// * Created by Steve SeongUg Jung on 14. 12. 17..
// */
//@Deprecated
//public class ResultObject<RESULT> {
//    private final int statusCode;
//    private final String errorMsg;
//    private final RESULT result;
//
//    private ResultObject(int statusCode, String errorMsg, RESULT result) {
//        this.statusCode = statusCode;
//        this.errorMsg = errorMsg;
//        this.result = result;
//    }
//
//    public static <RESULT> ResultObject<RESULT> createFailResult(int statusCode, String errorMsg, RESULT result) {
//        return new ResultObject<RESULT>(statusCode, errorMsg, result);
//    }
//
//    public static <RESULT> ResultObject<RESULT> createSuccessResult(RESULT result) {
//        return new ResultObject<RESULT>(200, "", result);
//    }
//
//    public int getStatusCode() {
//        return statusCode;
//    }
//
//    public RESULT getResult() {
//        return result;
//    }
//
//    public String getErrorMsg() {
//        return errorMsg;
//    }
//}
