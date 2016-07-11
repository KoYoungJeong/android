package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestVotePollEvent;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.widget.LinkedEllipsizeTextView;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.EntitySpannable;
import com.tosslab.jandi.app.views.spannable.MessageSpannable;

import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class PollSharedInViewHolder extends BaseViewHolder<TopicRoom> {

    private LinkedEllipsizeTextView tvSharedIn;

    private PollSharedInViewHolder(View itemView) {
        super(itemView);
        tvSharedIn = (LinkedEllipsizeTextView) itemView.findViewById(R.id.tv_poll_detail_shared_in);
    }

    public static PollSharedInViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_sharedin, parent, false);
        return new PollSharedInViewHolder(itemView);
    }

    @Override
    public void onBindView(TopicRoom room) {
        Resources resources = tvSharedIn.getResources();

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int sharedIndicatorSize = (int) resources.getDimension(R.dimen.jandi_text_size_small);
        int sharedIndicatorColor = resources.getColor(R.color.jandi_text_medium);

        MessageSpannable sharedIndicatorSpannable =
                new MessageSpannable(sharedIndicatorSize, sharedIndicatorColor);

        ssb.append(resources.getString(R.string.jandi_shared_in_room))
                .setSpan(sharedIndicatorSpannable,
                        0, ssb.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("  ");

        int entityType;
        if (room.isPublicTopic()) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        }

        EntitySpannable entitySpannable = new EntitySpannable(tvSharedIn.getContext(),
                room.getTeamId(), room.getId(), entityType, room.isStarred());
        entitySpannable.setColor(resources.getColor(R.color.jandi_accent_color));

        int length = ssb.length();
        String name = room.getName();
        ssb.append(room.getName());

        ssb.setSpan(entitySpannable,
                length, length + name.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSharedIn.setText(ssb);
        LinkifyUtil.setOnLinkClick(tvSharedIn);
    }

}
