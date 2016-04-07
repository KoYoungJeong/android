package com.tosslab.jandi.app.ui.maintab.team.view;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public interface TeamView {
    void showProgress();

    void hideProgress();

    void initTeamInfo(Team team);

    void showTeamLayout();

    void clearMembers();

    void initTeamMembers(List<FormattedEntity> members);

    void notifyDataSetChanged();

    void setEmptySearchedMember(String query);

    void setSearchedMembers(List<FormattedEntity> searchedMembers);

    void doSearchIfNeed();
}
