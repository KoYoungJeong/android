package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory;//package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
//import com.tosslab.jandi.app.utils.TokenUtil;
//
//import org.codehaus.jackson.map.ObjectMapper;
//
//import retrofit.RestAdapter;
//
///**
// * Created by tee on 15. 6. 18..
// */
//@Deprecated
//public class RestAdapterFactory {
//
//    private RestAdapterFactory() {
//    }
//
//    private static RestAdapter.Builder getDefaultBuilder() {
//        return new RestAdapter.Builder()
//                .setEndpoint(JandiConstantsForFlavors.SERVICE_INNER_API_URL);
//    }
//
//    private static RestAdapter.Builder getJacksonConvertedBuilder() {
//        return getDefaultBuilder().setConverter(new JacksonConverter(new ObjectMapper()));
//    }
//
//    public static RestAdapter getJacksonConvertedSimpleRestAdapter() {
//        return getJacksonConvertedBuilder()
//                .build();
//    }
//
//    public static RestAdapter getJacksonConvertedAuthRestAdapter() {
//        return getJacksonConvertedBuilder()
//                .setRequestInterceptor(request -> {
//                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
//                })
//                .build();
//    }
//}