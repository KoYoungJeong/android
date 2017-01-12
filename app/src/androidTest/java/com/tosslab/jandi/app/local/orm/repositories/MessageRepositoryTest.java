package com.tosslab.jandi.app.local.orm.repositories;

import android.support.test.runner.AndroidJUnit4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 15. 11. 9..
 */
@RunWith(AndroidJUnit4.class)
public class MessageRepositoryTest {

    public static final int ROOM_ID = 11162328;
    public static final int TEAM_ID = 279;
    private static final String MESSAGE_JSON = "{\"entityId\":11162328,\"globalLastLinkId\":430456," +
            "\"firstLinkId\":430437,\"lastLinkId\":430456,\"records\":[{\"id\":430437,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"entityType\":\"privateGroup\",\"eventType\":\"create\",\"inviteUsers\":[],\"createInfo\":{\"pg_creatorId\":6098,\"pg_createTime\":1437551059830,\"pg_members\":[6098],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551059844,\"toEntity\":[11162328]},{\"id\":430438,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"invitorId\":6098,\"eventType\":\"invite\",\"inviteUsers\":[11159859],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551068951,\"toEntity\":[11162328]},{\"id\":430439,\"teamId\":279,\"fromEntity\":11159859,\"info\":{\"eventType\":\"leave\",\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551079716,\"toEntity\":[11162328]},{\"id\":430440,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"created\",\"messageId\":379012,\"time\":1437551086763,\"toEntity\":[11162328],\"message\":{\"id\":379012,\"teamId\":279,\"writerId\":6098,\"contentType\":\"text\",\"permission\":740,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"body\":\"test\"},\"updateTime\":1437551086755,\"createTime\":1437551086755,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430442,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"created\",\"messageId\":379014,\"time\":1437551092435,\"toEntity\":[11162328],\"message\":{\"id\":379014,\"permission\":750,\"contentType\":\"sticker\",\"writerId\":6098,\"teamId\":279,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"stickerId\":\"1\",\"groupId\":100,\"url\":\"http://jandi.io:8888/files-sticker/100/1\"},\"updateTime\":1437551092433,\"createTime\":1437551092432,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430443,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"shared\",\"messageId\":379015,\"time\":1437551125545,\"toEntity\":[11162328],\"message\":{\"id\":379015,\"permission\":750,\"contentType\":\"file\",\"writerId\":6098,\"teamId\":279,\"info\":{\"mention\":[]},\"commentCount\":2,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"title\":\"sprinkler-android.db\",\"name\":\"sprinkler-android.db\",\"filename\":\"80ece39ac975b8360f0ee2bd11677aac.db\",\"type\":\"application/octet-stream\",\"icon\":\"etc\",\"size\":24576,\"ext\":\"db\",\"serverUrl\":\"s3\",\"fileUrl\":\"files-private/279/80ece39ac975b8360f0ee2bd11677aac.db\",\"filterType\":\"etc\"},\"updateTime\":1437551125542,\"createTime\":1437551125536,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430444,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":379015,\"status\":\"created\",\"messageId\":379016,\"time\":1437551125578,\"toEntity\":[11162328],\"message\":{\"id\":379016,\"permission\":744,\"contentType\":\"comment\",\"writerId\":6098,\"teamId\":279,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":379015,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"body\":\"1234\"},\"updateTime\":1437551125564,\"createTime\":1437551125550,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}},\"feedback\":{\"id\":379015,\"permission\":750,\"contentType\":\"file\",\"writerId\":6098,\"teamId\":279,\"info\":{\"mention\":[]},\"commentCount\":2,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"title\":\"sprinkler-android.db\",\"name\":\"sprinkler-android.db\",\"filename\":\"80ece39ac975b8360f0ee2bd11677aac.db\",\"type\":\"application/octet-stream\",\"icon\":\"etc\",\"size\":24576,\"ext\":\"db\",\"serverUrl\":\"s3\",\"fileUrl\":\"files-private/279/80ece39ac975b8360f0ee2bd11677aac.db\",\"filterType\":\"etc\"},\"updateTime\":1437551125542,\"createTime\":1437551125536,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430445,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":379015,\"status\":\"created\",\"messageId\":379017,\"time\":1437551131948,\"toEntity\":[11162328],\"message\":{\"id\":379017,\"teamId\":279,\"writerId\":6098,\"contentType\":\"comment_sticker\",\"permission\":744,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":379015,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"stickerId\":\"1\",\"groupId\":100,\"url\":\"http://jandi.io:8888/files-sticker/100/1\"},\"updateTime\":1437551131944,\"createTime\":1437551131944,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}},\"feedback\":{\"id\":379015,\"permission\":750,\"contentType\":\"file\",\"writerId\":6098,\"teamId\":279,\"info\":{\"mention\":[]},\"commentCount\":2,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"title\":\"sprinkler-android.db\",\"name\":\"sprinkler-android.db\",\"filename\":\"80ece39ac975b8360f0ee2bd11677aac.db\",\"type\":\"application/octet-stream\",\"icon\":\"etc\",\"size\":24576,\"ext\":\"db\",\"serverUrl\":\"s3\",\"fileUrl\":\"files-private/279/80ece39ac975b8360f0ee2bd11677aac.db\",\"filterType\":\"etc\"},\"updateTime\":1437551125542,\"createTime\":1437551125536,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430446,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"eventType\":\"announcement_created\",\"eventInfo\":{\"writerId\":6098},\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551135772,\"toEntity\":[11162328]},{\"id\":430447,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"created\",\"messageId\":379018,\"time\":1437551147510,\"toEntity\":[11162328],\"message\":{\"id\":379018,\"teamId\":279,\"writerId\":6098,\"contentType\":\"text\",\"permission\":740,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"body\":\"qaz\"},\"updateTime\":1437551147506,\"createTime\":1437551147506,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430448,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"eventType\":\"announcement_created\",\"eventInfo\":{\"writerId\":6098},\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551150503,\"toEntity\":[11162328]},{\"id\":430449,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"eventType\":\"announcement_deleted\",\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551169721,\"toEntity\":[11162328]},{\"id\":430450,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"invitorId\":6098,\"eventType\":\"invite\",\"inviteUsers\":[11159859],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551209852,\"toEntity\":[11162328]},{\"id\":430451,\"fromEntity\":11159859,\"teamId\":279,\"info\":{\"eventType\":\"announcement_created\",\"eventInfo\":{\"writerId\":6098},\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551219880,\"toEntity\":[11162328]},{\"id\":430452,\"fromEntity\":6098,\"teamId\":279,\"info\":{\"eventType\":\"announcement_created\",\"eventInfo\":{\"writerId\":6098},\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551225645,\"toEntity\":[11162328]},{\"id\":430453,\"teamId\":279,\"fromEntity\":6098,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"created\",\"messageId\":379019,\"time\":1437551234804,\"toEntity\":[11162328],\"message\":{\"id\":379019,\"teamId\":279,\"writerId\":6098,\"contentType\":\"text\",\"permission\":740,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"body\":\"qweasdzxc\"},\"updateTime\":1437551234799,\"createTime\":1437551234799,\"writer\":{\"id\":6098,\"type\":\"user\",\"name\":\"Steve Jung\",\"u_photoUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg\",\"u_statusMessage\":\"개발은 근성!!\",\"u_extraData\":{\"phoneNumber\":\"+821050409730\",\"department\":\"부서닷\",\"position\":\"직책이닷!\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=80\",\"mediumThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=360\",\"largeThumbnailUrl\":\"files-profile/c9464bba5d62977eed5cb4d432d743dc.jpg?size=640\"}},\"linkPreview\":{}}},{\"id\":430454,\"teamId\":279,\"fromEntity\":11159859,\"info\":{\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"created\",\"messageId\":379020,\"time\":1437551238431,\"toEntity\":[11162328],\"message\":{\"id\":379020,\"teamId\":279,\"writerId\":11159859,\"contentType\":\"text\",\"permission\":740,\"info\":{\"mention\":[]},\"commentCount\":0,\"feedbackId\":-1,\"shareEntities\":[11162328],\"status\":\"created\",\"linkPreviewId\":null,\"content\":{\"body\":\"qweasdzxc\"},\"updateTime\":1437551238426,\"createTime\":1437551238426,\"writer\":{\"id\":11159859,\"name\":\"steve.test\",\"u_statusMessage\":\"\",\"u_photoUrl\":\"images/profile_80.png\",\"type\":\"user\",\"u_extraData\":{\"position\":\"\",\"department\":\"\",\"phoneNumber\":\"\"},\"u_photoThumbnailUrl\":{\"smallThumbnailUrl\":\"images/profile_80.png\",\"mediumThumbnailUrl\":\"images/profile_180.png\",\"largeThumbnailUrl\":\"images/profile_180.png\"}},\"linkPreview\":{}}},{\"id\":430455,\"fromEntity\":11159859,\"teamId\":279,\"info\":{\"eventType\":\"announcement_created\",\"eventInfo\":{\"writerId\":11159859},\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551242748,\"toEntity\":[11162328]},{\"id\":430456,\"fromEntity\":11159859,\"teamId\":279,\"info\":{\"eventType\":\"announcement_deleted\",\"inviteUsers\":[],\"createInfo\":{\"pg_members\":[],\"ch_members\":[]}},\"feedbackId\":-1,\"status\":\"event\",\"messageId\":-1,\"time\":1437551251118,\"toEntity\":[11162328]}]}";

    @Before
    public void setUp() throws Exception {
        AccountRepository.getRepository().updateSelectedTeamInfo(TEAM_ID);

    }

    @After
    public void tearDown() throws Exception {
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .getDao(ResMessages.Link.class)
                .deleteBuilder()
                .delete();

        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .getDao(SendMessage.class)
                .deleteBuilder()
                .delete();
    }

    @Test
    public void testUpsertMessages() throws Exception {
        List<ResMessages.Link> messages = getMessages();
        boolean success = MessageRepository.getRepository().upsertMessages(messages);

        assertTrue(success);

    }

    @Test
    public void testGetMessages() throws Exception {

        List<ResMessages.Link> messages = getMessages();

        long beforeTime = System.currentTimeMillis();

        MessageRepository.getRepository().upsertMessages(messages);

        System.out.println("소요 시간 : " + (System.currentTimeMillis() - beforeTime) + "ms");


        List<ResMessages.Link> savedLinks = MessageRepository.getRepository().getMessages(ROOM_ID);

        Collections.sort(savedLinks, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

        assertThat(savedLinks.size(), is(equalTo(messages.size())));
        assertThat(savedLinks.get(0).eventType, is(equalTo(messages.get(0).eventType)));
        assertThat(savedLinks.get(3).messageType, is(equalTo(messages.get(3).messageType)));
        assertThat(((ResMessages.TextMessage) savedLinks.get(3).message).content.body,
                is(equalTo(((ResMessages.TextMessage) messages.get(3).message).content.body)));
        assertThat(((ResMessages.FileMessage) savedLinks.get(5).message).content.fileUrl,
                is(equalTo(((ResMessages.FileMessage) messages.get(5).message).content.fileUrl)));
        assertThat(((ResMessages.CommentStickerMessage) savedLinks.get(7).message).content.groupId,
                is(equalTo(((ResMessages.CommentStickerMessage) messages.get(7).message).content.groupId)));
        assertThat(((ResMessages.CommentStickerMessage) savedLinks.get(7).message).content.stickerId,
                is(equalTo(((ResMessages.CommentStickerMessage) messages.get(7).message).content.stickerId)));

        assertThat(savedLinks.get(10).eventType, is(equalTo(messages.get(10).eventType)));
    }


    @Test
    public void testDeleteMessage() throws Exception {
        List<ResMessages.Link> messages = getMessages();
        MessageRepository.getRepository().upsertMessages(messages);

        MessageRepository.getRepository().deleteMessageOfMessageId(messages.get(3).messageId);

        List<ResMessages.Link> savedMessage = MessageRepository.getRepository().getMessages(ROOM_ID);
        assertThat(messages.size() - 1, is(equalTo(savedMessage.size())));
    }

    @Test
    public void testReadyMessage() throws Exception {
        long roomId = 1;

        ReadyMessage readyMessage = new ReadyMessage();
        String text = "asda";
        readyMessage.setText(text);
        readyMessage.setRoomId(roomId);
        ReadyMessageRepository.getRepository().upsertReadyMessage(readyMessage);

        String newText = "qwe";
        long newRoomId = 2;
        readyMessage = new ReadyMessage();
        readyMessage.setRoomId(newRoomId);
        readyMessage.setText(newText);

        ReadyMessageRepository.getRepository().upsertReadyMessage(readyMessage);

        ReadyMessage message = ReadyMessageRepository.getRepository().getReadyMessage(roomId);

        assertThat(message.getRoomId(), is(equalTo(roomId)));
        assertThat(message.getText(), is(equalTo(text)));


        int deleeteRow = ReadyMessageRepository.getRepository().deleteReadyMessage(roomId);
        assertThat(deleeteRow, is(equalTo(1)));

        readyMessage = ReadyMessageRepository.getRepository().getReadyMessage(roomId);
        assertThat(readyMessage.getText(), is(""));

    }

    @Test
    public void testSendMessage() throws Exception {
        SendMessage sendMessage = new SendMessage();
        int roomId = 1;
        sendMessage.setRoomId(roomId);
        String message = "hahaha";
        sendMessage.setMessage(message);
        SendMessageRepository.getRepository().insertSendMessage(sendMessage);


        List<SendMessage> sendMessages = SendMessageRepository.getRepository().getSendMessageOfRoom(roomId);
        sendMessage = sendMessages.get(0);

        assertThat(sendMessages.size(), is(equalTo(roomId)));
        assertThat(sendMessage.getMessage(), is(equalTo(message)));
        assertThat(sendMessage.getStatus(), is(equalTo(SendMessage.Status.SENDING.name())));

        sendMessage.setStatus(SendMessage.Status.COMPLETE.name());
        SendMessageRepository.getRepository().updateSendMessageStatus(sendMessage.getId(),
                SendMessage.Status.COMPLETE);

        sendMessages = SendMessageRepository.getRepository().getSendMessageOfRoom(roomId);
        sendMessage = sendMessages.get(0);

        assertThat(sendMessage.getStatus(), is(equalTo(SendMessage.Status.COMPLETE.name())));

        SendMessageRepository.getRepository().deleteSendMessage(sendMessage.getId());
        sendMessages = SendMessageRepository.getRepository().getSendMessageOfRoom(roomId);

        assertThat(sendMessages.size(), is(equalTo(0)));
    }

    private List<ResMessages.Link> getMessages() {

        try {
            ObjectMapper objectMapper = JsonMapper.getInstance().getObjectMapper();
            ResMessages resMessages = objectMapper.readValue(MESSAGE_JSON, ResMessages.class);
            return resMessages.records;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}