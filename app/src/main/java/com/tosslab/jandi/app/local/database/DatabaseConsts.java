package com.tosslab.jandi.app.local.database;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class DatabaseConsts {

    public enum Table {
        account, account_email, account_team, account_device,

        left_team, left_user, left_starred_entity, left_message_marker, left_topic_entity, left_join_entity,

        messages, temp_messages,

        files
    }

    public enum Account {
        _id, id, name, updatedAt, tutoredAt, createdAt, loggedAt, activatedAt, notificationTarget, status, photoUrl, largeThumbPhotoUrl, mediumThumbPhotoUrl, smallThumbPhotoUrl
    }

    public enum AccountEmail {
        _id, id, is_primary, status, confirmedAt
    }

    public enum AccountTeam {
        _id, teamId, memberId, name, teamDomain, unread, selected;
    }

    public enum AccountDevice {
        _id, token, type, badgeCount, subscribe
    }

    public enum LeftTeam {
        _id, id, name, teamDomain, teamDefaultChannelId
    }

    public enum LeftUser {
        /* user */
        _id, id, teamId, name, email, authority, firstName, lastName, photoUrl, statusMessage, nickName,

        /* u_extraData*/
        phoneNumber, department, position,

        /* u_photoThumbnailUrl*/
        thumbSmall, thumbMedium, thumbLarge,

        isMe
    }

    public enum LeftStarredEntity {
        _id, teamId, entityId
    }

    public enum LeftMessageMarkers {
        _id, teamId, entityType, entityId, lastLinkId, alarmCount
    }

    public enum LeftTopicEntity {
        _id, teamId, id, type, name, creatorId, createdTime, members
    }

    public enum LeftJoinEntity {
        /* reference User & TopicEntity */
        _id, teamId, id, type
    }

    public enum Messages {
        _id, teamId, entityId, link
    }

    public enum TempMessages {
        _id, teamId, entityId, text
    }

    public enum Files {
        _id, teamId, files
    }
}