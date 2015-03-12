package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchModel {

    @RootContext
    Context context;

    public ResMessageSearch requestSearchQuery(int teamId, String query, int page, int perPage, int entityId, int writerId) throws JandiNetworkException {
        ReqMessageSearchQeury reqMessageSearchQeury = new ReqMessageSearchQeury(teamId, query, page, perPage);
        reqMessageSearchQeury.entityId(entityId).writerId(writerId);
        return RequestManager.newInstance(context, MessageSearchRequest.newInstance(context, reqMessageSearchQeury)).request();
    }

    public int getCurrentTeamId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }
}
