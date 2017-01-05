package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredCommentViewHolder extends BaseViewHolder<StarredMessage> {

    private ImageView ivProfile;
    private TextView tvWriter;
    private TextView tvDate;

    private TextView tvComment;
    private TextView tvFileName;
    private ImageView ivContentIcon;
    private TextView tvRoomName;
    private View vSemiDivider;
    private View vFullDivider;
    private View vProfileCover;

    private StarredCommentViewHolder(View itemView) {
        super(itemView);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_starred_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_starred_name);
        tvDate = (TextView) itemView.findViewById(R.id.tv_starred_date);

        tvComment = (TextView) itemView.findViewById(R.id.tv_starred_comment);
        tvFileName = (TextView) itemView.findViewById(R.id.tv_starred_file_name);
        ivContentIcon = (ImageView) itemView.findViewById(R.id.iv_icon_message_content_icon);
        tvRoomName = (TextView) itemView.findViewById(R.id.tv_room_name);
        vSemiDivider = itemView.findViewById(R.id.v_semi_divider);
        vFullDivider = itemView.findViewById(R.id.v_full_divider);
        vProfileCover = itemView.findViewById(R.id.v_starred_profile_cover);
    }

    public static StarredCommentViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_starred_comment, parent, false);
        return new StarredCommentViewHolder(itemView);
    }

    @Override
    public void onBindView(StarredMessage starredMessage) {
        Member member = TeamInfoLoader.getInstance().getMember(starredMessage.getMessage().writerId);
        StarredMessageProfileBinder.newInstance(tvWriter, ivProfile, vProfileCover)
                .bind(member);

        tvFileName.setText(starredMessage.getMessage().feedbackTitle);

        String body = starredMessage.getMessage().content.body;
        SpannableStringBuilder commentStringBuilder = new SpannableStringBuilder(body);

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

//        long roomId = starredMessage.getRoom().id;
//        String roomName = getRoomName(roomId);
//        tvRoomName.setText(roomName);

        long myId = teamInfoLoader.getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, starredMessage.getMessage().mentions)
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
                        .build();

        SpannableLookUp.text(commentStringBuilder)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvFileName.getContext());

        // for single spannable
        commentStringBuilder.append(" ");
        tvComment.setText(commentStringBuilder);

        if ("poll".equals(starredMessage.getMessage().feedbackType)) {
            ivContentIcon.setImageResource(R.drawable.icon_message_poll);
        } else {
            ivContentIcon.setImageResource(R.drawable.icon_message_file);
        }
        ivContentIcon.setVisibility(View.VISIBLE);

        String date = DateTransformator.getTimeString(starredMessage.getMessage().createdAt);
        tvDate.setText(date);

        if (starredMessage.hasSemiDivider()) {
            vSemiDivider.setVisibility(View.VISIBLE);
            vFullDivider.setVisibility(View.GONE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vSemiDivider.setVisibility(View.GONE);
        }
    }

    private String getRoomName(long roomId) {
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        if (teamInfoLoader.isChat(roomId)) {
            String companionName =
                    teamInfoLoader.getMemberName(teamInfoLoader.getChat(roomId).getCompanionId());
            return companionName;
        } else if (teamInfoLoader.isTopic(roomId)) {
            return teamInfoLoader.getTopic(roomId).getName();
        }
        return "";
    }
}
