package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 2. 9..
 */
public class EventViewHolder implements BodyViewHolder {

    private TextView eventContentView;
    private TextView eventDateView;

    @Override
    public void initView(View rootView) {

        eventContentView = ((TextView) rootView.findViewById(R.id.txt_message_event_title));
        eventDateView = ((TextView) rootView.findViewById(R.id.txt_message_event_date));

    }

    @Override
    public void bindData(ResMessages.Link link) {

        eventContentView.setText(" ");
        eventDateView.setText(" ");

        Observable.from(Arrays.asList(link.info))
                .subscribeOn(Schedulers.io())
                .map(stringObjectMap -> {
                    Map<String, Object> info = link.info;
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {

                        String s = objectMapper.writeValueAsString(info);
                        return objectMapper.readValue(s, ResMessages.EventInfo.class);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(eventInfo -> eventInfo != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventInfo -> {

                    EntityManager entityManager = EntityManager.getInstance(eventContentView.getContext());
                    if (eventInfo instanceof ResMessages.CreateEvent) {
                        ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) eventInfo;

                        if (createEvent.createInfo instanceof ResMessages.PublicCreateInfo) {
                            ResMessages.PublicCreateInfo publicCreateInfo = (ResMessages.PublicCreateInfo) createEvent.createInfo;
                            FormattedEntity creatorEntity = entityManager.getEntityById(publicCreateInfo.creatorId);

                            eventContentView.setText(eventContentView.getContext().getString(R.string.jandi_created_this_topic, creatorEntity.getName()));

                        } else if (createEvent.createInfo instanceof ResMessages.PrivateCreateInfo) {
                            ResMessages.PrivateCreateInfo privateCreateInfo = (ResMessages.PrivateCreateInfo) createEvent.createInfo;

                            FormattedEntity creatorEntity = entityManager.getEntityById(privateCreateInfo.creatorId);

                            eventContentView.setText(eventContentView.getContext().getString(R.string.jandi_created_this_topic, creatorEntity.getName()));

                        }

                    } else if (eventInfo instanceof ResMessages.InviteEvent) {
                        ResMessages.InviteEvent inviteEvent = (ResMessages.InviteEvent) eventInfo;
                        FormattedEntity invitorEntity = entityManager.getEntityById(inviteEvent.invitorId);

                        String invitorName = invitorEntity.getName();

                        StringBuffer buffer = new StringBuffer();
                        FormattedEntity tempEntity;

                        int size = inviteEvent.inviteUsers.size();
                        for (int idx = 0; idx < size; idx++) {

                            tempEntity = entityManager.getEntityById(inviteEvent.inviteUsers.get(idx));
                            if (tempEntity != null) {
                                if (idx > 0) {
                                    buffer.append(", ");
                                }
                                buffer.append(tempEntity.getName());
                            }
                        }

                        eventContentView.setText(eventContentView.getContext().getString(R.string.jandi_invited_topic, invitorName, buffer.toString()));

                    } else if (eventInfo instanceof ResMessages.JoinEvent) {

                        FormattedEntity entity = EntityManager.getInstance(eventContentView.getContext()).getEntityById(link.fromEntity);

                        String name = entity.getName();
                        eventContentView.setText(eventContentView.getContext().getString(R.string.jandi_has_joined, name));


                    } else if (eventInfo instanceof ResMessages.LeaveEvent) {
                        FormattedEntity entity = EntityManager.getInstance(eventContentView.getContext()).getEntityById(link.fromEntity);

                        String name = entity.getName();
                        eventContentView.setText(eventContentView.getContext().getString(R.string.jandi_left_topic, name));
                    }

                    eventDateView.setText(DateTransformator.getTimeStringForSimple(link.time));
                });

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_event_v2;
    }

}
