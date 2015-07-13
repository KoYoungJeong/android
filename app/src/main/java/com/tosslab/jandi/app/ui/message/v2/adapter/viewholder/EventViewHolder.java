package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.ProfileSpannable;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 2. 9..
 */
public class EventViewHolder implements BodyViewHolder {

    public static final String KEY_PARSING_DATA = "parsing_data";
    private TextView eventContentView;
    private Context context;

    @Override
    public void initView(View rootView) {
        eventContentView = ((TextView) rootView.findViewById(R.id.txt_message_event_title));
        context = rootView.getContext();
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        Observable.from(Arrays.asList(link.info))
                .subscribeOn(Schedulers.io())
                .map(stringObjectMap -> {
                    Map<String, Object> info = link.info;
                    if (link.info.containsKey(KEY_PARSING_DATA)) {
                        return link.info.get(KEY_PARSING_DATA);
                    }

                    ObjectMapper objectMapper = JacksonMapper.getInstance().getObjectMapper();
                    try {

                        String s = objectMapper.writeValueAsString(info);
                        ResMessages.EventInfo eventInfo =
                                objectMapper.readValue(s, ResMessages.EventInfo.class);
                        link.info.put(KEY_PARSING_DATA, eventInfo);
                        return eventInfo;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(eventInfo -> eventInfo != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventInfo -> {

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    EntityManager entityManager = EntityManager.getInstance(context);

                    if (eventInfo instanceof ResMessages.AnnouncementCreateEvent) {
                        buildAnnouncementCreateEvent((ResMessages.AnnouncementCreateEvent) eventInfo,
                                link.fromEntity, builder, entityManager);

                    } else if (eventInfo instanceof ResMessages.AnnouncementUpdateEvent) {
                        buildAnnouncementUpdateEvent((ResMessages.AnnouncementUpdateEvent) eventInfo,
                                link.fromEntity, builder, entityManager);

                    } else if (eventInfo instanceof ResMessages.AnnouncementDeleteEvent) {
                        buildAnnouncementDeleteEvent(link.fromEntity, builder, entityManager);

                    } else if (eventInfo instanceof ResMessages.CreateEvent) {
                        buildCreateEvent((ResMessages.CreateEvent) eventInfo, builder, entityManager);

                    } else if (eventInfo instanceof ResMessages.InviteEvent) {

                        buildInviteEvent((ResMessages.InviteEvent) eventInfo, builder, entityManager);

                    } else {
                        int fromEntity = link.fromEntity;
                        if (eventInfo instanceof ResMessages.JoinEvent) {
                            buildJoinEvent(builder, fromEntity);

                        } else if (eventInfo instanceof ResMessages.LeaveEvent) {
                            buildLeaveEvent(builder, fromEntity);
                        }
                    }

                    builder.append(" ");
                    int eventTextSize = context.getResources()
                            .getDimensionPixelSize(R.dimen.jandi_messages_content);
                    ColorStateList eventTextColor = ColorStateList.valueOf(
                            context.getResources().getColor(R.color.jandi_messages_date));
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
                    builder.setSpan(spannable, startIndex, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    eventContentView.setText(builder);
                    eventContentView.setMovementMethod(LinkMovementMethod.getInstance());
                });

    }

    private void buildCreateEvent(ResMessages.CreateEvent eventInfo,
                                  SpannableStringBuilder builder, EntityManager entityManager) {
        if (eventInfo.createInfo instanceof ResMessages.PublicCreateInfo) {
            ResMessages.PublicCreateInfo publicCreateInfo =
                    (ResMessages.PublicCreateInfo) eventInfo.createInfo;
            int creatorId = publicCreateInfo.creatorId;
            FormattedEntity creatorEntity = entityManager.getEntityById(creatorId);

            ProfileSpannable profileSpannable = new ProfileSpannable(creatorId);
            int beforeLength = builder.length();
            builder.append(creatorEntity.getName());
            int afterLength = builder.length();
            builder.setSpan(profileSpannable,
                    beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(context.getString(R.string.jandi_created_this_topic, ""));

        } else if (eventInfo.createInfo instanceof ResMessages.PrivateCreateInfo) {
            ResMessages.PrivateCreateInfo privateCreateInfo =
                    (ResMessages.PrivateCreateInfo) eventInfo.createInfo;

            int creatorId = privateCreateInfo.creatorId;
            FormattedEntity creatorEntity = entityManager.getEntityById(creatorId);

            ProfileSpannable profileSpannable = new ProfileSpannable(creatorId);
            int beforeLength = builder.length();
            builder.append(creatorEntity.getName());
            int afterLength = builder.length();
            builder.setSpan(profileSpannable,
                    beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(context.getString(R.string.jandi_created_this_topic, ""));

        }
    }

    private void buildInviteEvent(ResMessages.InviteEvent eventInfo,
                                  SpannableStringBuilder builder, EntityManager entityManager) {
        int invitorId = eventInfo.invitorId;
        FormattedEntity invitorEntity = entityManager.getEntityById(invitorId);

        String invitorName = invitorEntity.getName();

        ProfileSpannable profileSpannable = new ProfileSpannable(invitorId);
        int beforeLength = builder.length();
        builder.append(invitorName);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_invited_topic, "", "{-}"));

        int nameIndexOf = builder.toString().indexOf("{-}");
        builder.delete(nameIndexOf, nameIndexOf + 3);

        int tempIndex = nameIndexOf;

        FormattedEntity tempEntity;
        List<Integer> inviteUsers = eventInfo.inviteUsers;
        int size = inviteUsers.size();
        for (int idx = 0; idx < size; idx++) {
            tempEntity = entityManager.getEntityById(inviteUsers.get(idx));
            if (tempEntity != null) {
                if (idx > 0) {
                    builder.insert(tempIndex, ", ");
                    tempIndex += 2;
                }

                ProfileSpannable profileSpannable1 =
                        new ProfileSpannable(tempEntity.getId());
                builder.insert(tempIndex, tempEntity.getName());

                builder.setSpan(profileSpannable1,
                        tempIndex, tempIndex + tempEntity.getName().length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tempIndex += tempEntity.getName().length();
            }
        }
    }

    private void buildJoinEvent(SpannableStringBuilder builder, int fromEntity) {
        FormattedEntity entity =
                EntityManager.getInstance(context).getEntityById(fromEntity);
        String name = entity.getName();

        ProfileSpannable profileSpannable = new ProfileSpannable(fromEntity);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_has_joined, ""));
    }

    private void buildLeaveEvent(SpannableStringBuilder builder, int fromEntity) {
        FormattedEntity entity =
                EntityManager.getInstance(context).getEntityById(fromEntity);
        String name = entity.getName();

        ProfileSpannable profileSpannable = new ProfileSpannable(fromEntity);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getString(R.string.jandi_left_topic, ""));
    }

    private void buildAnnouncementCreateEvent(ResMessages.AnnouncementCreateEvent event, int creatorId,
                                              SpannableStringBuilder builder, EntityManager entityManager) {

        FormattedEntity creatorEntity = entityManager.getEntityById(creatorId);
        String creator = creatorEntity.getName();

        int writerId = event.getEventInfo().getWriterId();
        FormattedEntity entity = entityManager.getEntityById(writerId);
        String writer = entity.getName();

        String format = context.getResources().getString(R.string.jandi_announcement_created, creator, writer);

        builder.append(format);

        int creatorStartIndex = format.indexOf(creator);
        int creatorLastIndex = creatorStartIndex + creator.length();
        ProfileSpannable creatorSpannable = new ProfileSpannable(creatorId);
        builder.setSpan(creatorSpannable,
                creatorStartIndex, creatorLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int writerStartIndex = format.lastIndexOf(writer);
        int writerLastIndex = writerStartIndex + writer.length();
        ProfileSpannable writerSpannable = new ProfileSpannable(writerId);
        builder.setSpan(writerSpannable,
                writerStartIndex, writerLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void buildAnnouncementUpdateEvent(ResMessages.AnnouncementUpdateEvent event, int creatorId,
                                              SpannableStringBuilder builder, EntityManager entityManager) {
        FormattedEntity creatorEntity = entityManager.getEntityById(creatorId);
        String creator = creatorEntity.getName();

        int writerId = event.getEventInfo().getWriterId();
        FormattedEntity entity = entityManager.getEntityById(writerId);
        String writer = entity.getName();

        String format = context.getResources().getString(R.string.jandi_announcement_created, creator, writer);

        builder.append(format);

        int creatorStartIndex = format.indexOf(creator);
        int creatorLastIndex = creatorStartIndex + creator.length();
        ProfileSpannable creatorSpannable = new ProfileSpannable(creatorId);
        builder.setSpan(creatorSpannable,
                creatorStartIndex, creatorLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int writerStartIndex = format.lastIndexOf(writer);
        int writerLastIndex = writerStartIndex + writer.length();
        ProfileSpannable writerSpannable = new ProfileSpannable(writerId);
        builder.setSpan(writerSpannable,
                writerStartIndex, writerLastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void buildAnnouncementDeleteEvent(int from,
                                              SpannableStringBuilder builder, EntityManager entityManager) {
        FormattedEntity entity = entityManager.getEntityById(from);
        String name = entity.getName();

        ProfileSpannable profileSpannable = new ProfileSpannable(from);
        int beforeLength = builder.length();
        builder.append(name);
        int afterLength = builder.length();
        builder.setSpan(profileSpannable,
                beforeLength, afterLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(context.getResources().getString(R.string.jandi_announcement_deleted, ""));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_event_v2;
    }

}
