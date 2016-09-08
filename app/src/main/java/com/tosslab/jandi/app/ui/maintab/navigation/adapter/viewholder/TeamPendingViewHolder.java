package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 22..
 */
public class TeamPendingViewHolder extends BaseViewHolder<Team> {

    @Bind(R.id.v_team_pending_metaphor)
    View vMetaphor;

    @Bind(R.id.tv_team_pending_name)
    TextView tvName;

    private TeamPendingViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static TeamPendingViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_pending, parent, false);
        return new TeamPendingViewHolder(itemView);
    }

    @Override
    public void onBindView(final Team team) {
        tvName.setText(team.getName());

        if (vMetaphor.getTag() != null) {
            return;
        }

        ValueAnimator whiteToRedAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        whiteToRedAnim.setDuration(1000);
        whiteToRedAnim.setRepeatMode(ValueAnimator.REVERSE);
        whiteToRedAnim.setRepeatCount(ValueAnimator.INFINITE);
        whiteToRedAnim.addUpdateListener(animation -> {
            Float alpha = (Float) animation.getAnimatedValue();
            vMetaphor.setAlpha(alpha);
        });
        vMetaphor.setTag(whiteToRedAnim);
        whiteToRedAnim.start();
    }

}
