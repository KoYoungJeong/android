package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.network.models.ResMyTeam;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
public class SelectMyTeam {
    public ResMyTeam.Team myTeam;
    public SelectMyTeam(ResMyTeam.Team myTeam) {
        this.myTeam = myTeam;
    }
}
