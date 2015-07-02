package com.tosslab.jandi.app.network.manager;//package com.tosslab.jandi.app.network.manager;
//
//import android.content.Context;
//
//import com.tosslab.jandi.app.JandiApplication;
//import com.tosslab.jandi.app.network.models.ResAccessToken;
//import com.tosslab.jandi.app.services.socket.JandiSocketService;
//import com.tosslab.jandi.app.utils.JandiNetworkException;
//import com.tosslab.jandi.app.utils.logger.LogUtil;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.client.HttpStatusCodeException;
//
//import retrofit.RetrofitError;
//
///**
// * Created by Steve SeongUg Jung on 14. 12. 16..
// */
//@Deprecated
//public class RequestManager<ResponseObject> {
//
//    int selectedTeamId;
//    private Request<ResponseObject> request;
//
//    private RequestManager(Context context, Request<ResponseObject> request) {
//        this.request = request;
//    }
//
//    public RequestManager(Context context, int selectedTeamId) {
//        this.selectedTeamId = selectedTeamId;
//    }
//
//    public static <ResponseObject> RequestManager<ResponseObject> newInstance(Context context, Request<ResponseObject> request) {
//        return new RequestManager<ResponseObject>(context, request);
//    }
//
//    private static ResAccessToken refreshToken() {
//        ResAccessToken accessToken = null;
//        int loginRetryCount = 0;
//        while (accessToken == null && loginRetryCount <= 3) {
//            ++loginRetryCount;
//            try {
//                // Request Access token, and save token
////                JandiRestV2Client jandiRestClient = RestAdapterBuilder.newInstance(JandiRestV2Client.class).create();
////                accessToken = jandiRestClient.getAccessToken(ReqAccessToken.createRefreshReqToken(JandiPreference.getRefreshToken(JandiApplication.getContext())));
////                TokenUtil.saveTokenInfoByRefresh(accessToken);
//            } catch (HttpStatusCodeException e) {
//                LogUtil.e("Refresh Token Fail : " + e.getStatusCode().value() + " : " + e.getResponseBodyAsString());
//                if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
//                    return null;
//                }
//            }
//        }
//        return accessToken;
//    }
//
//    public ResponseObject request() throws JandiNetworkException {
//        try {
//            return request.request();
//            //return
//        } catch (RetrofitError e) {
//            if (e.getResponse().getStatus() == 401) {
//                ResAccessToken accessToken = refreshToken();
//                if (accessToken != null) {
//                    try {
//                        return request.request();
//                    } catch (RetrofitError e1) {
//                        // unknown exception
//                        LogUtil.e("Retry Fail" + request.getClass() + " : " + e.getResponse().getStatus() + " : " + e.getResponse().getBody().toString(), e1);
//                        throw new JandiNetworkException(e1);
//                    }
//                } else {
//                    // unauthorized exception
//                    LogUtil.e("Refresh Token Fail : " + request.getClass() + " : " + e.getResponse().getStatus() + " : " + e.getResponse().getBody().toString(), e);
//                    throw new JandiNetworkException(e);
//                }
//            } else {
//                // exception, not unauthorized
//                JandiSocketService.stopSocketServiceIfRunning(JandiApplication.getContext());
//                LogUtil.e("Request Fail : " + request.getClass() + " : " + e.getResponse().getStatus() + " : " + e.getResponse().getBody().toString(), e);
//                throw new JandiNetworkException(e);
//            }
//        } catch (Exception e) {
//            LogUtil.e("Unknown Request Error : " + request.getClass() + " : " + e.getMessage(), e);
//            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
//        }
//    }
//
//}
