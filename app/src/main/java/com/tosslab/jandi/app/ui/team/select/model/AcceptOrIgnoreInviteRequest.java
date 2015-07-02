package com.tosslab.jandi.app.ui.team.select.model;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
//@Deprecated
//public class AcceptOrIgnoreInviteRequest implements Request<ResTeamDetailInfo> {
//
//    private final Context context;
//    //    private final InvitationApiClient invitationApiClient;
//    private final String invitationId;
//    private final String type;
//
//    RestAdapter restAdapter;
//
//    private AcceptOrIgnoreInviteRequest(Context context, InvitationApiClient invitationApiClient, String invitationId, String type) {
//        this.context = context;
////        this.invitationApiClient = invitationApiClient;
//        this.invitationId = invitationId;
//        this.type = type;
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
//    public static AcceptOrIgnoreInviteRequest create(Context context, String invitationId, String type) {
//        return new AcceptOrIgnoreInviteRequest(context, new InvitationApiClient_(context), invitationId, type);
//    }
//
//
//    @Override
//    public ResTeamDetailInfo request() throws JandiNetworkException {
//
////        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
////
//        ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore = new ReqInvitationAcceptOrIgnore(type);
////        return invitationApiClient.acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore);
//        return restAdapter.create(InvitationApiV2Client.class).acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore);
//
//    }
//}
