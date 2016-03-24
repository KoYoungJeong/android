package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.Test;

import retrofit.RestAdapter;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Steve SeongUg Jung on 15. 6. 27..
 */
public class MainRestApiClientTest {

    @Test
    public void testGetConfigByInterface() throws Exception {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api");

        MainRestApiClient mainRestApiClient = builder.build().create(MainRestApiClient.class);
        ResConfig config = mainRestApiClient.getConfig();

        assertNotNull(config);


    }

    @Test
    public void testGetConfigByImpl() throws Exception {


        ResConfig config = new MainRestApiClientImpl().getConfig();

        assertNotNull(config);

    }

    private static class MainRestApiClientImpl implements MainRestApiClient {

        @Override
        public ResConfig getConfig() {

            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api");

            MainRestApiClient mainRestApiClient = builder.build().create(MainRestApiClient.class);

            return mainRestApiClient.getConfig();
        }

        @Override
        public ResMyTeam getTeamId(@Body String userEmail) {
            return null;
        }

        @Override
        public ResAccessToken getAccessToken(@Body ReqAccessToken login) {
            return null;
        }

        @Override
        public ResAccountInfo getAccountInfo() {
            return null;
        }

        @Override
        public ResCommon signUpAccount(@Body ReqSignUpInfo signUpInfo) {
            return null;
        }

        @Override
        public ResAccountInfo updatePrimaryEmail(@Body ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
            return null;
        }

        @Override
        public ResAccountActivate activateAccount(@Body ReqAccountActivate reqAccountActivate) {
            return null;
        }

        @Override
        public ResCommon accountVerification(@Body ReqAccountVerification reqAccountVerification) {
            return null;
        }

        @Override
        public ResLeftSideMenu getInfosForSideMenu(@Query("teamId") int teamId) {
            return null;
        }

        @Override
        public ResCommon setMarker(@Path("entityId") int entityId, @Body ReqSetMarker reqSetMarker) {
            return null;
        }

        @Override
        public ResSearchFile searchFile(@Body ReqSearchFile reqSearchFile) {
            return null;
        }
    }
}