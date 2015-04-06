package com.tosslab.jandi.app.services.socket;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketServiceModel {
    private final Context context;

    public JandiSocketServiceModel(Context context) {

        this.context = context;
    }


    public ConnectTeam getConnectTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity me = entityManager.getMe();
        return new ConnectTeam(selectedTeamInfo.getTeamId(), selectedTeamInfo.getName(), selectedTeamInfo.getMemberId(), me.getName());

    }
}
