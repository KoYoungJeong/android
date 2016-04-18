package com.tosslab.jandi.app.ui.maintab.team.view;

import com.tosslab.jandi.app.ui.maintab.team.vo.Team;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public interface TeamView {

    void showProgress();

    void hideProgress();

    void initTeamInfo(Team team);

    void notifyDataSetChanged();

}
