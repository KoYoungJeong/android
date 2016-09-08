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
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredMessageViewHolder extends BaseViewHolder<StarredMessage> {

    private ImageView ivProfile;
    private TextView tvWriter;
    private TextView tvDate;
    private TextView tvMentionContent;
    private TextView tvMentionTopicName;

    private StarredMessageViewHolder(View itemView) {
        super(itemView);
        tvMentionContent = (TextView) itemView.findViewById(R.id.tv_starred_content);
        tvMentionTopicName = (TextView) itemView.findViewById(R.id.tv_starred_topic_name);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_starred_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_starred_name);
        tvDate = (TextView) itemView.findViewById(R.id.tv_starred_date);
    }

    public static StarredMessageViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_starred_message, parent, false);
        return new StarredMessageViewHolder(itemView);
    }

    @Override
    public void onBindView(StarredMessage starredMessage) {
        Member member = TeamInfoLoader.getInstance().getMember(starredMessage.getMessage().writerId);
        StarredMessageProfileBinder.newInstance(tvWriter, ivProfile)
                .bind(member);

        tvMentionTopicName.setText(starredMessage.getRoom().name);

        String body = starredMessage.getMessage().content.body;
        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder(body);

        long myId = TeamInfoLoader.getInstance().getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, starredMessage.getMessage().mentions)
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
                        .build();

        SpannableLookUp.text(messageStringBuilder)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvMentionContent.getContext());

        // for single spannable
        messageStringBuilder.append(" ");
        tvMentionContent.setText(messageStringBuilder);

        String date = DateTransformator.getTimeString(starredMessage.getMessage().createdAt);
        tvDate.setText(date);
    }
}
