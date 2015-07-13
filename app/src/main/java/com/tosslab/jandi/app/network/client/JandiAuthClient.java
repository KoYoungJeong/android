//package com.tosslab.jandi.app.network.client;
//
//import android.content.Context;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.models.ResConfig;
//import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
//import com.tosslab.jandi.app.utils.JandiNetworkException;
//
//import org.androidannotations.annotations.AfterInject;
//import org.androidannotations.annotations.EBean;
//import org.androidannotations.annotations.RootContext;
//import org.codehaus.jackson.map.ObjectMapper;
//
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//
///**
// * Created by justinygchoi on 2014. 7. 16..
// */
//@EBean
//@Deprecated
//public class JandiAuthClient {
//
//    @RootContext
//    Context context;
//
//    @AfterInject
//    void initAuthentication() {
//    }
//
//    public ResConfig getConfig() throws JandiNetworkException {
//        try {
//            JacksonConverter converter = new JacksonConverter(new ObjectMapper());
//
//            RestAdapter restAdapter = new RestAdapter.Builder()
//                    .setConverter(converter)
//                    .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api")
//                    .build();
//
//            return restAdapter.create(JandiRestV2Client.class).getConfig();
//
//        } catch (RetrofitError e) {
//            throw new JandiNetworkException(e);
//        } catch (Exception e) {
//            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
//        }
//    }
//}