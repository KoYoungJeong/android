package com.tosslab.jandi.app.ui.team.select.model;

///**
// * Created by Steve SeongUg Jung on 14. 12. 18..
// */
//@Deprecated
//public class IgnoreInviteRequest implements Request<ResTeamDetailInfo> {
//
//    private final Context context;
//    //    private final InvitationApiClient invitationApiClient;
//    private final Team team;
//
//    RestAdapter restAdapter;
//
//    private IgnoreInviteRequest(Context context, InvitationApiClient invitationApiClient, Team team) {
//        this.context = context;
////        this.invitationApiClient = invitationApiClient;
//        this.team = team;
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
//
//    }
//
//    public static IgnoreInviteRequest create(Context context, Team team) {
//        return new IgnoreInviteRequest(context, new InvitationApiClient_(context), team);
//    }
//
//
//    @Override
//    public ResTeamDetailInfo request() throws JandiNetworkException {
//
////        invitationApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//
//        ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore = new ReqInvitationAcceptOrIgnore(ReqInvitationAcceptOrIgnore.Type.DECLINE.getType());
////        return invitationApiClient.acceptOrDeclineInvitation(team.getInvitationId(), reqInvitationAcceptOrIgnore);
//        return restAdapter.create(InvitationApiV2Client.class).acceptOrDeclineInvitation(team.getInvitationId(), reqInvitationAcceptOrIgnore);
//    }
//}
