package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class MessageRepositoryTest {

    private OrmDatabaseHelper helper;

    @After
    public void tearDown() throws Exception {

        String path = helper.getReadableDatabase().getPath();
        System.out.println("cp " + path + " ~/Desktop/");

        System.out.println("Finish");

    }

    @Test
    public void testUpsertMessages() throws Exception {
        List<ResMessages.Link> messages = getMessages();
        MessageRepository.getRepository().upsertMessages(messages);

        helper = OpenHelperManager.getHelper(Robolectric.application, OrmDatabaseHelper.class);
        assertThat(helper, is(notNullValue()));

        List<ResMessages.Link> savedLinks = helper.getDao(ResMessages.Link.class).queryBuilder().query();

        assertThat(savedLinks.size(), is(equalTo(messages.size())));

    }

    private List<ResMessages.Link> getMessages() {

        List<ResMessages.Link> messages = new ArrayList<>();
        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 1;
            link.teamId = 10;
            link.fromEntity = 10;
            link.time = new Date();
            link.status = "created";

            ResMessages.CreateEvent createEvent = new ResMessages.CreateEvent();
            ResMessages.PublicCreateInfo createInfo = new ResMessages.PublicCreateInfo();
            createInfo.createTime = new Date();
            createInfo.creatorId = 10;

            ResMessages.PublicCreateInfo.IntegerWrapper integerWrapper = new ResMessages.PublicCreateInfo.IntegerWrapper();
            integerWrapper.setMemberId(1);
            ResMessages.PublicCreateInfo.IntegerWrapper integerWrapper1 = new ResMessages.PublicCreateInfo.IntegerWrapper();
            integerWrapper1.setMemberId(2);
            ResMessages.PublicCreateInfo.IntegerWrapper integerWrapper2 = new ResMessages.PublicCreateInfo.IntegerWrapper();
            integerWrapper2.setMemberId(3);

            createInfo.members = Arrays.asList(integerWrapper, integerWrapper1, integerWrapper2);
            createEvent.createInfo = createInfo;

            link.info = createEvent;

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 2;
            link.teamId = 10;
            link.fromEntity = 11;
            link.time = new Date();
            link.status = "created";

            ResMessages.CreateEvent createEvent = new ResMessages.CreateEvent();
            ResMessages.PrivateCreateInfo createInfo = new ResMessages.PrivateCreateInfo();
            createInfo.createTime = new Date();
            createInfo.creatorId = 11;

            ResMessages.PrivateCreateInfo.IntegerWrapper integerWrapper = new ResMessages.PrivateCreateInfo.IntegerWrapper();
            integerWrapper.setMemberId(4);
            ResMessages.PrivateCreateInfo.IntegerWrapper integerWrapper1 = new ResMessages.PrivateCreateInfo.IntegerWrapper();
            integerWrapper1.setMemberId(5);
            ResMessages.PrivateCreateInfo.IntegerWrapper integerWrapper2 = new ResMessages.PrivateCreateInfo.IntegerWrapper();
            integerWrapper2.setMemberId(6);

            createInfo.members = Arrays.asList(integerWrapper, integerWrapper1, integerWrapper2);
            createEvent.createInfo = createInfo;

            link.info = createEvent;

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 3;
            link.teamId = 10;
            link.fromEntity = 13;
            link.time = new Date();
            link.status = "created";

            ResMessages.InviteEvent inviteEvent = new ResMessages.InviteEvent();
            inviteEvent.invitorId = 10;

            ResMessages.InviteEvent.IntegerWrapper integerWrapper = new ResMessages.InviteEvent
                    .IntegerWrapper();
            integerWrapper.setInviteUserId(7);
            ResMessages.InviteEvent.IntegerWrapper integerWrapper1 = new ResMessages.InviteEvent
                    .IntegerWrapper();
            integerWrapper1.setInviteUserId(8);
            ResMessages.InviteEvent.IntegerWrapper integerWrapper2 = new ResMessages.InviteEvent
                    .IntegerWrapper();
            integerWrapper2.setInviteUserId(9);

            inviteEvent.inviteUsers = Arrays.asList(integerWrapper, integerWrapper1, integerWrapper2);

            link.info = inviteEvent;

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 4;
            link.teamId = 10;
            link.fromEntity = 14;
            link.time = new Date();
            link.status = "created";

            link.info = new ResMessages.LeaveEvent();

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 5;
            link.teamId = 10;
            link.fromEntity = 15;
            link.time = new Date();
            link.status = "created";

            link.info = new ResMessages.JoinEvent();

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 6;
            link.teamId = 10;
            link.fromEntity = 16;
            link.time = new Date();
            link.status = "created";

            ResMessages.AnnouncementCreateEvent announcementCreateEvent = new ResMessages.AnnouncementCreateEvent();
            ResMessages.AnnouncementCreateEvent.Info eventInfo = new ResMessages.AnnouncementCreateEvent.Info();
            eventInfo.setWriterId(10);
            announcementCreateEvent.setEventInfo(eventInfo);

            link.info = announcementCreateEvent;

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 7;
            link.teamId = 10;
            link.fromEntity = 17;
            link.time = new Date();
            link.status = "created";

            link.info = new ResMessages.AnnouncementDeleteEvent();

            messages.add(link);
        }

        {
            ResMessages.Link link = new ResMessages.Link();
            link.id = 8;
            link.teamId = 10;
            link.fromEntity = 18;
            link.time = new Date();
            link.status = "created";

            ResMessages.AnnouncementUpdateEvent announcementUpdateEvent = new ResMessages.AnnouncementUpdateEvent();
            ResMessages.AnnouncementUpdateEvent.Info eventInfo = new ResMessages.AnnouncementUpdateEvent.Info();
            eventInfo.setWriterId(10);
            announcementUpdateEvent.setEventInfo(eventInfo);

            link.info = announcementUpdateEvent;

            messages.add(link);
        }

        return messages;
    }


}