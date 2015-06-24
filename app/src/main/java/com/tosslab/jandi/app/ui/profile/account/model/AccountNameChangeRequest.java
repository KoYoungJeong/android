package com.tosslab.jandi.app.ui.profile.account.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApiV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
public class AccountNameChangeRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final String name;

    RestAdapter restAdapter;

    private AccountNameChangeRequest(Context context, String name) {
        this.context = context;
        this.name = name;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api")
                .build();
    }

    public static AccountNameChangeRequest create(Context context, String name) {
        return new AccountNameChangeRequest(context, name);
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {

//        AccountProfileClient accountProfileClient = new AccountProfileClient_(context);
//        accountProfileClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//
//        return accountProfileClient.changeName(new ReqProfileName(name));
        return restAdapter.create(AccountProfileApiV2Client.class).changeName(new ReqProfileName(name));

    }
}
