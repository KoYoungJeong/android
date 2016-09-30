package com.tosslab.jandi.app.ui.team.select.adapter.viewmodel;

import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamCreateViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamItemViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamPendingViewHolder;

/**
 * Created by tee on 2016. 9. 28..
 */

public interface TeamSelectListAdapterViewModel {
    void justRefresh();

    void setOnTeamCreateClickListener(
            TeamCreateViewHolder.OnTeamCreateClickListener onTeamCreateClickListener);

    void setOnTeamItemClickListener(
            TeamItemViewHolder.OnTeamItemClickListener onTeamItemClickListener);

    void setOnClickTeamJoinIgnoreListener(
            TeamPendingViewHolder.OnClickTeamJoinIgnoreListener onClickTeamJoinIgnoreListener);

    void setOnClickTeamJoinAcceptListener(
            TeamPendingViewHolder.OnClickTeamJoinAcceptListener onClickTeamJoinAcceptListener);
}
