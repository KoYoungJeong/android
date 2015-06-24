package com.tosslab.jandi.app.ui.team.select.model;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
//@Deprecated
//public class PendingTeamListRequest implements Request<List<ResPendingTeamInfo>> {
//
//
//    private final Context context;
////    private final InvitationApiClient invitationApiClient;
//
//    RestAdapter restAdapter;
//
//    private PendingTeamListRequest(Context context, InvitationApiClient invitationApiClient) {
//        this.context = context;
////        this.invitationApiClient = invitationApiClient;
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
//    public static PendingTeamListRequest create(Context context) {
//        return new PendingTeamListRequest(context, new InvitationApiClient_(context));
//    }
//
//    @Override
//    public List<ResPendingTeamInfo> request() throws JandiNetworkException {
////        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
////
////        return invitationApiClient.getPedingTeamInfo();
//        return restAdapter.create(InvitationApiV2Client.class).getPedingTeamInfo();
//    }
//}
