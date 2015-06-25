//package com.tosslab.jandi.app.ui.intro.model;
//
///**
// * Created by tee on 15. 6. 15..
// */
//
//import com.tosslab.jandi.app.network.models.ResAccountInfo;
//import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
//import com.tosslab.jandi.app.utils.JandiNetworkException;
//
//import org.androidannotations.annotations.rest.Get;
//import org.androidannotations.annotations.rest.RequiresAuthentication;
//
//import java.util.List;
//import retrofit.Callback;
//import retrofit.http.GET;
//import retrofit.http.Header;
//import retrofit.http.Headers;
//import retrofit.http.Path;
//import retrofit.http.Query;
//
//
//public interface TestAccountInfoService {
//    @GET("/account")
//    ResAccountInfo getAccountInfo() throws JandiNetworkException;
//
//    @GET("/leftSideMenu")
//    ResLeftSideMenu getInfosForSideMenu(@Query("teamId") int teamId) throws JandiNetworkException;
//
//}
