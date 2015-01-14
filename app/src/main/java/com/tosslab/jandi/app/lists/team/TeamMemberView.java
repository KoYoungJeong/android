package com.tosslab.jandi.app.lists.team;

/**
 * Created by justinygchoi on 2014. 9. 22..
 */

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.FormattedDummyEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EViewGroup(R.layout.item_team_member)
public class TeamMemberView extends LinearLayout {

    @ViewById(R.id.team_member_layout)
    RelativeLayout relativeLayoutTeamMember;
    @ViewById(R.id.team_member_icon)
    ImageView imageViewTeamMemberIcon;
    @ViewById(R.id.team_member_name)
    TextView textViewTeamMemberName;
    @ViewById(R.id.team_member_email)
    TextView textViewTeamMemberEmail;

    private Context mContext;

    public TeamMemberView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(FormattedEntity formattedEntity) {

        imageViewTeamMemberIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ProfileDetailEvent(formattedEntity.getId()));
            }
        });

        if (formattedEntity instanceof FormattedDummyEntity) {
            drawInvitedUserDummyLayout(((FormattedDummyEntity) formattedEntity).getEmailForInvitation());
            return;
        } else if (formattedEntity.type == FormattedEntity.TYPE_REAL_USER) {
            drawTeamMemberLayout(formattedEntity);
            return;
        }
    }

    private void drawInvitedUserDummyLayout(String email) {
        imageViewTeamMemberIcon.setImageResource(R.drawable.jandi_profile_invite);
        textViewTeamMemberName.setText("초대 대기 중인 사용자");
        textViewTeamMemberEmail.setText(email);
        drawBackgroundAsInactive();
    }

    private void drawBackgroundAsInactive() {
        relativeLayoutTeamMember.setBackgroundResource(R.color.jandi_team_member_invite_bg);
        textViewTeamMemberName.setTextColor(getResources().getColor(R.color.jandi_team_member_invite_font));
        textViewTeamMemberEmail.setTextColor(getResources().getColor(R.color.jandi_team_member_invite_font));
    }

    private void drawTeamMemberLayout(FormattedEntity teamMember) {
        Glide.with(mContext)
                .load(teamMember.getUserSmallProfileUrl())
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(mContext))
                .into(imageViewTeamMemberIcon);
        textViewTeamMemberName.setText(teamMember.getName());
        textViewTeamMemberEmail.setText(teamMember.getUserEmail());
        drawBackgroundAsActive();
    }

    private void drawBackgroundAsActive() {
        relativeLayoutTeamMember.setBackgroundResource(R.color.white);
        textViewTeamMemberName.setTextColor(getResources().getColor(R.color.jandi_text));
        textViewTeamMemberEmail.setTextColor(getResources().getColor(R.color.jandi_text_medium));
    }
}
