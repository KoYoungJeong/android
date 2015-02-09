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
import java.util.Map;

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

        Map<String, Object> info = link.info;

        ObjectMapper objectMapper = new ObjectMapper();

        EntityManager entityManager = EntityManager.getInstance(eventContentView.getContext());

        try {
            String s = objectMapper.writeValueAsString(info);
            ResMessages.EventInfo eventInfo = objectMapper.readValue(s, ResMessages.EventInfo.class);

            if (eventInfo instanceof ResMessages.CreateEvent) {
                ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) eventInfo;

                if (createEvent.createInfo instanceof ResMessages.PublicCreateInfo) {
                    ResMessages.PublicCreateInfo publicCreateInfo = (ResMessages.PublicCreateInfo) createEvent.createInfo;
                    FormattedEntity creatorEntity = entityManager.getEntityById(publicCreateInfo.creatorId);

                    StringBuffer buffer = new StringBuffer();
                    buffer.append(creatorEntity.getName()).append(" : 공개 토픽 개설\n");

                    for (Integer member : publicCreateInfo.members) {
                        creatorEntity = entityManager.getEntityById(publicCreateInfo.creatorId);
                        buffer.append(creatorEntity.getName()).append(",");
                    }

                    buffer.append("참석");
                    eventContentView.setText(buffer);

                } else if (createEvent.createInfo instanceof ResMessages.PrivateCreateInfo) {
                    ResMessages.PrivateCreateInfo privateCreateInfo = (ResMessages.PrivateCreateInfo) createEvent.createInfo;

                    FormattedEntity creatorEntity = entityManager.getEntityById(privateCreateInfo.creatorId);

                    StringBuffer buffer = new StringBuffer();
                    buffer.append(creatorEntity.getName()).append(" : 비공개 토픽 개설\n");

                    for (Integer member : privateCreateInfo.members) {
                        creatorEntity = entityManager.getEntityById(privateCreateInfo.creatorId);
                        buffer.append(creatorEntity.getName()).append(",");
                    }

                    buffer.append("참석");
                    eventContentView.setText(buffer);

                }

            } else if (eventInfo instanceof ResMessages.InviteEvent) {
                ResMessages.InviteEvent inviteEvent = (ResMessages.InviteEvent) eventInfo;
                FormattedEntity invitorEntity = entityManager.getEntityById(inviteEvent.invitorId);

                StringBuffer buffer = new StringBuffer();
                buffer.append(invitorEntity.getName()).append(" 님이 ");

                for (Integer inviteUser : inviteEvent.inviteUsers) {
                    invitorEntity = entityManager.getEntityById(inviteUser);
                    buffer.append(invitorEntity.getName()).append(",");
                }
                buffer.append("을 초대하였습니다.");
                eventContentView.setText(buffer);

            } else if (eventInfo instanceof ResMessages.JoinEvent) {
                ResMessages.JoinEvent joinEvent = (ResMessages.JoinEvent) eventInfo;

                String name = link.fromEntity.name;
                eventContentView.setText(name + " 님이 참여하였습니다.");


            } else if (eventInfo instanceof ResMessages.LeaveEvent) {
                ResMessages.LeaveEvent leaveEvent = (ResMessages.LeaveEvent) eventInfo;

                String name = link.fromEntity.name;
                eventContentView.setText(name + " 님이 떠났습니다.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        eventDateView.setText(DateTransformator.getTimeStringForSimple(link.time));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_event_v2;
    }

}
