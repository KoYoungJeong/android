package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.ClickableNameSpannable;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

import java.util.Collection;
import java.util.Iterator;

public class EventMessageViewHolder implements BodyViewHolder {

    private final int textSize11sp;
    private TextView tvEvent;
    private Context context;
    private View vLastRead;
    private View vMargin;
    private boolean hasBottomMargin = false;

    private EventMessageViewHolder() {
        textSize11sp = JandiApplication.getContext().getResources().getDimensionPixelSize(R.dimen.jandi_system_message_content);

    }

    @Override
    public void initView(View rootView) {
        tvEvent = ((TextView) rootView.findViewById(R.id.tv_message_event_title));
        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
        vMargin = rootView.findViewById(R.id.v_margin);
        if (hasBottomMargin) {
            vMargin.setVisibility(View.VISIBLE);
        } else {
            vMargin.setVisibility(View.GONE);
        }
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

        ResMessages.EventInfo eventInfo = link.info;

        SpannableStringBuilder builder = new SpannableStringBuilder();

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        if (eventInfo instanceof ResMessages.AnnouncementCreateEvent) {
            buildAnnouncementCreateEvent((ResMessages.AnnouncementCreateEvent) eventInfo,
                    link.fromEntity, builder, teamInfoLoader);

        } else if (eventInfo instanceof ResMessages.AnnouncementUpdateEvent) {
            buildAnnouncementUpdateEvent((ResMessages.AnnouncementUpdateEvent) eventInfo,
                    link.fromEntity, builder, teamInfoLoader);

        } else if (eventInfo instanceof ResMessages.AnnouncementDeleteEvent) {
            buildAnnouncementDeleteEvent(link.fromEntity, builder, teamInfoLoader);

        } else if (eventInfo instanceof ResMessages.CreateEvent) {
            buildCreateEvent((ResMessages.CreateEvent) eventInfo, builder, teamInfoLoader);

        } else if (eventInfo instanceof ResMessages.InviteEvent) {
            buildInviteEvent((ResMessages.InviteEvent) eventInfo, builder, teamInfoLoader);

        } else {
            long fromEntity = link.fromEntity;
            if (eventInfo instanceof ResMessages.JoinEvent) {
                buildJoinEvent(builder, fromEntity);

            } else if (eventInfo instanceof ResMessages.LeaveEvent) {
                buildLeaveEvent(builder, fromEntity);
            }
        }

        builder.append(" ");
        int eventTextSize = context.getResources()
                .getDimensionPixelSize(R.dimen.jandi_system_message_content);
        ColorStateList eventTextColor = ColorStateList.valueOf(
                context.getResources().getColor(R.color.white));

        int eventLength = builder.length();
        TextAppearanceSpan eventTextAppearance =
                new TextAppearanceSpan(null, Typeface.NORMAL,
                        eventTextSize, eventTextColor, eventTextColor);
        builder.setSpan(eventTextAppearance,
                0, eventLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int startIndex = builder.length();
        builder.append(" ");
        DateViewSpannable spannable =
                new DateViewSpannable(context, DateTransformator.getTimeStringForSimple(link.time));
        spannable.setTextSize(tvEvent.getContext().getResources().getDimensionPixelSize(R.dimen.jandi_system_message_content_time));
        builder.setSpan(spannable, startIndex, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvEvent.setText(builder);
        LinkifyUtil.setOnLinkClick(tvEvent);


    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    private void buildCreateEvent(ResMessages.CreateEvent eventInfo,
                                  SpannableStringBuilder builder, TeamInfoLoader teamInfoLoader) {
        if (eventInfo.createInfo instanceof ResMessages.PublicCreateInfo) {
            ResMessages.PublicCreateInfo publicCreateInfo =
                    (ResMessages.PublicCreateInfo) eventInfo.createInfo;
            long creatorId = publicCreateInfo.creatorId;
            User creator = teamInfoLoader.getUser(creatorId);

            ClickableNameSpannable profileSpannable = new ClickableNameSpannable(creatorId, textSize11sp, Color.WHITE);
            int beforeLength = builder.length();
            builder.append(creator.getName());
            int afterLength = builder.length();
            builder.setSpan(profileSpannable,
                    beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(context.getString(R.string.jandi_created_this_topic, ""));

        } else if (eventInfo.createInfo instanceof ResMessages.PrivateCreateInfo) {
            ResMessages.PrivateCreateInfo privateCreateInfo =
                    (ResMessages.PrivateCreateInfo) eventInfo.createInfo;

            long creatorId = privateCreateInfo.creatorId;
            User creatorEntity = teamInfoLoader.getUser(creatorId);

            ClickableNameSpannable profileSpannable = new ClickableNameSpannable(creatorId, textSize11sp, Color.WHITE);
            int beforeLength = builder.length();
            builder.append(creatorEntity.getName());
            int afterLength = builder.length();
            builder.setSpan(profileSpannable,
                    beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(context.getString(R.string.jandi_created_this_topic, ""));

        }
    }

    private void buildInviteEvent(ResMessages.InviteEvent eventInfo,
                                  SpannableStringBuilder builder, TeamInfoLoader teamInfoLoader) {
        long invitorId = eventInfo.invitorId;
        User invitorEntity = teamInfoLoader.getUser(invitorId);

        String invitorName = invitorEntity.getName();

        ClickableNameSpannable profileSpannable = new ClickableNameSpannable(invitorId, textSize11sp, Color.WHITE);
        int beforeLength = builder.length();
        builder.append(invitorName);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_invited_topic, "", "{-}"));

        int nameIndexOf = builder.toString().indexOf("{-}");
        builder.delete(nameIndexOf, nameIndexOf + 3);

        int tempIndex = nameIndexOf;

        User tempEntity;
        Collection<ResMessages.InviteEvent.IntegerWrapper> inviteUsers = eventInfo.inviteUsers;
        int size = inviteUsers.size();
        Iterator<ResMessages.InviteEvent.IntegerWrapper> iterator = inviteUsers.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            tempEntity = teamInfoLoader.getUser(iterator.next().getInviteUserId());
            if (tempEntity != null) {
                if (!first) {
                    builder.insert(tempIndex, ", ");
                    tempIndex += 2;
                }
                first = false;

                ClickableNameSpannable profileSpannable1 =
                        new ClickableNameSpannable(tempEntity.getId(), textSize11sp, Color.WHITE);
                builder.insert(tempIndex, tempEntity.getName());

                builder.setSpan(profileSpannable1,
                        tempIndex, tempIndex + tempEntity.getName().length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tempIndex += tempEntity.getName().length();
            }
        }
    }

    private void buildJoinEvent(SpannableStringBuilder builder, long fromEntity) {
        User entity =
                TeamInfoLoader.getInstance().getInstance().getUser(fromEntity);
        String name;
        if (entity != null) {
            name = entity.getName();
        } else {
            name = " ";
        }

        ClickableNameSpannable profileSpannable = new ClickableNameSpannable(fromEntity, textSize11sp, Color.WHITE);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_has_joined, ""));
    }

    private void buildLeaveEvent(SpannableStringBuilder builder, long fromEntity) {
        User entity =
                TeamInfoLoader.getInstance().getUser(fromEntity);
        String name = entity.getName();

        ClickableNameSpannable profileSpannable = new ClickableNameSpannable(fromEntity, textSize11sp, Color.WHITE);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_left_topic, ""));
    }

    private void buildAnnouncementCreateEvent(ResMessages.AnnouncementCreateEvent event, long creatorId,
                                              SpannableStringBuilder builder, TeamInfoLoader teamInfoLoader) {

        User creatorEntity = teamInfoLoader.getUser(creatorId);
        String creator = creatorEntity.getName();

        long writerId = event.getEventInfo().getWriterId();
        User entity = teamInfoLoader.getUser(writerId);
        String writer = entity.getName();

        String format = context.getResources().getString(R.string.jandi_announcement_created, creator, writer);
        builder.append(format);

        int creatorStartIndex = format.indexOf(creator);
        int creatorLastIndex = creatorStartIndex + creator.length();
        ClickableNameSpannable creatorSpannable = new ClickableNameSpannable(creatorId, textSize11sp, Color.WHITE);
        builder.setSpan(creatorSpannable,
                creatorStartIndex, creatorLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int writerStartIndex = format.lastIndexOf(writer);
        int writerLastIndex = writerStartIndex + writer.length();
        ClickableNameSpannable writerSpannable = new ClickableNameSpannable(writerId, textSize11sp, Color.WHITE);
        builder.setSpan(writerSpannable,
                writerStartIndex, writerLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void buildAnnouncementUpdateEvent(ResMessages.AnnouncementUpdateEvent event, long creatorId,
                                              SpannableStringBuilder builder, TeamInfoLoader entityManager) {
        User creatorEntity = entityManager.getUser(creatorId);
        String creator = creatorEntity.getName();

        long writerId = event.getEventInfo().getWriterId();
        User entity = entityManager.getUser(writerId);
        String writer = entity.getName();

        String format = context.getResources().getString(R.string.jandi_announcement_created, creator, writer);

        builder.append(format);

        int creatorStartIndex = format.indexOf(creator);
        int creatorLastIndex = creatorStartIndex + creator.length();
        ClickableNameSpannable creatorSpannable = new ClickableNameSpannable(creatorId, textSize11sp, Color.WHITE);
        builder.setSpan(creatorSpannable,
                creatorStartIndex, creatorLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int writerStartIndex = format.lastIndexOf(writer);
        int writerLastIndex = writerStartIndex + writer.length();
        ClickableNameSpannable writerSpannable = new ClickableNameSpannable(writerId, textSize11sp, Color.WHITE);
        builder.setSpan(writerSpannable,
                writerStartIndex, writerLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void buildAnnouncementDeleteEvent(long from,
                                              SpannableStringBuilder builder, TeamInfoLoader teamInfoLoader) {
        User entity = teamInfoLoader.getUser(from);
        String name = entity.getName();

        ClickableNameSpannable profileSpannable = new ClickableNameSpannable(from, textSize11sp, Color.WHITE);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getResources().getString(R.string.jandi_announcement_deleted, ""));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_event_v3;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public EventMessageViewHolder build() {
            EventMessageViewHolder eventViewHolder = new EventMessageViewHolder();
            eventViewHolder.setHasBottomMargin(hasBottomMargin);
            return eventViewHolder;
        }

    }

}
