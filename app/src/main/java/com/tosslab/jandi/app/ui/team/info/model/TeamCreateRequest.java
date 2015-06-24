//package com.tosslab.jandi.app.ui.team.info.model;
//
//import android.content.Context;
//
//import com.tosslab.jandi.app.JandiConstantsForFlavors;
//import com.tosslab.jandi.app.network.client.teams.TeamApiV2Client;
//import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
//import com.tosslab.jandi.app.network.client.teams.TeamsApiClient_;
//import com.tosslab.jandi.app.network.manager.Request;
//import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
//import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
//import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
//import com.tosslab.jandi.app.utils.JandiNetworkException;
//import com.tosslab.jandi.app.utils.TokenUtil;
//
//import org.codehaus.jackson.map.ObjectMapper;
//
//import retrofit.RestAdapter;
//
///**
// * Created by Steve SeongUg Jung on 14. 12. 18..
// */
//public class TeamCreateRequest implements Request<ResTeamDetailInfo> {
//
//    private final Context context;
//    //    private final TeamsApiClient teamsApiClient;
//    private final ReqCreateNewTeam reqCreateNewTeam;
//
//    RestAdapter restAdapter;
//
//    private TeamCreateRequest(Context context, TeamsApiClient teamsApiClient, ReqCreateNewTeam reqCreateNewTeam) {
//        this.context = context;
////        this.teamsApiClient = teamsApiClient;
//        this.reqCreateNewTeam = reqCreateNewTeam;
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
//    public static TeamCreateRequest create(Context context, ReqCreateNewTeam reqCreateNewTeam) {
//        return new TeamCreateRequest(context, new TeamsApiClient_(context), reqCreateNewTeam);
//    }
//
//
//    @Override
//    public ResTeamDetailInfo request() throws JandiNetworkException {
//
//        return restAdapter.create(TeamApiV2Client.class).createNewTeam(reqCreateNewTeam);
//    }
//}
