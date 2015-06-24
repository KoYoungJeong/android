//package com.tosslab.jandi.app.ui.maintab.file.model;
//
//import android.content.Context;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.client.JandiRestV2Client;
//import com.tosslab.jandi.app.network.manager.Request;
//import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.converter.JacksonConverter;
//import com.tosslab.jandi.app.network.models.ReqSearchFile;
//import com.tosslab.jandi.app.network.models.ResSearchFile;
//import com.tosslab.jandi.app.utils.JandiNetworkException;
//import com.tosslab.jandi.app.utils.TokenUtil;
//
//import org.codehaus.jackson.map.ObjectMapper;
//
//import retrofit.RestAdapter;
//
///**
// * Created by Steve SeongUg Jung on 14. 12. 22..
// */
//@Deprecated
//public class FileSearchRequest implements Request<ResSearchFile> {
//
//    private final Context context;
//    //    private final JandiRestClient jandiRestClient;
//    private final ReqSearchFile reqSearchFile;
//
//    RestAdapter restAdapter;
//
//    private FileSearchRequest(Context context, ReqSearchFile reqSearchFile) {
//        this.context = context;
//        this.reqSearchFile = reqSearchFile;
//
//        JacksonConverter converter = new JacksonConverter(new ObjectMapper());
//
//        restAdapter = new RestAdapter.Builder()
//                .setRequestInterceptor(request -> {
//                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
//                })
//                .setConverter(converter)
//                .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api")
//                .build();
//    }
//
//    public static FileSearchRequest create(Context context, ReqSearchFile reqSearchFile) {
//        return new FileSearchRequest(context, reqSearchFile);
//    }
//
//
//    @Override
//    public ResSearchFile request() throws JandiNetworkException {
//
////        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
////        return jandiRestClient.searchFile(reqSearchFile);
//        return restAdapter.create(JandiRestV2Client.class).searchFile(reqSearchFile);
//    }
//}
