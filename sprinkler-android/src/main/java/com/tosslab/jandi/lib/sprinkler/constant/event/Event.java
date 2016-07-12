package com.tosslab.jandi.lib.sprinkler.constant.event;

import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;

/**
 * Created by tonyjs on 15. 7. 22..
 * <p>
 * EventCategory(Event 의 종류 파악),
 * EventName,
 * AvailablePropertyKeys(Event 에 적재 가능한 Property 파악)
 */
public enum Event {
    // Account Related Event
    SignIn(EventCategory.AccountRelatedEvent,
            "e6",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.AutoSignIn, PropertyKey.ErrorCode}),

    SendAccountVerificationMail(EventCategory.AccountRelatedEvent,
            "e9",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode}),

    ResendAccountVerificationMail(EventCategory.AccountRelatedEvent,
            "e10",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode}),

    SignUp(EventCategory.AccountRelatedEvent,
            "e11",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.ErrorCode}),

    SignOut(EventCategory.AccountRelatedEvent,
            "e12",
            new PropertyKey[]{}),

    LaunchTeam(EventCategory.AccountRelatedEvent,
            "e13",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TeamId, PropertyKey.ErrorCode}),

    CreateTeam(EventCategory.AccountRelatedEvent,
            "e14",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TeamId, PropertyKey.ErrorCode}),

    ChangeAccountName(EventCategory.AccountRelatedEvent,
            "e16",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.ErrorCode}),

    ChangeAccountPrimaryEmail(EventCategory.AccountRelatedEvent,
            "e17",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode}),

    RequestVerificationEmail(EventCategory.AccountRelatedEvent,
            "e20",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode}),

    // File Related Event
    FileUpload(EventCategory.FileRelatedEvent,
            "e38",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode}),

    FileDownload(EventCategory.FileRelatedEvent,
            "e39",
            new PropertyKey[]{PropertyKey.FileId}),

    FileShare(EventCategory.FileRelatedEvent,
            "e40",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode}),

    FileUnShare(EventCategory.FileRelatedEvent,
            "e41",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode}),

    FileDelete(EventCategory.FileRelatedEvent,
            "e42",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode}),

    FileKeywordSearch(EventCategory.FileRelatedEvent,
            "e48",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.SearchKeyword, PropertyKey.ErrorCode}),

    // Message Related Event
    MessagePost(EventCategory.MessageRelatedEvent,
            "e44",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.ErrorCode}),

    MessageKeywordSearch(EventCategory.MessageRelatedEvent,
            "e45",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.SearchKeyword, PropertyKey.ErrorCode}),

    MessageDelete(EventCategory.MessageRelatedEvent,
            "e46",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.MessageId, PropertyKey.ErrorCode}),

    // Screen View Event
    ScreenView(EventCategory.ScreenViewEvent,
            "e48",
            new PropertyKey[]{PropertyKey.ScreenView}),

    // System Related Event
    AppOpen(EventCategory.SystemRelatedEvent,
            "e50",
            new PropertyKey[]{PropertyKey.AppVersion, PropertyKey.Brand, PropertyKey.Manufacturer, PropertyKey.Model, PropertyKey.OS, PropertyKey.OSVersion, PropertyKey.ScreenDPI, PropertyKey.ScreenHeight, PropertyKey.ScreenWidth, PropertyKey.Carrier, PropertyKey.Wifi, PropertyKey.GooglePlayServices}),

    AppClose(EventCategory.SystemRelatedEvent,
            "e51",
            new PropertyKey[]{}),

    // Topic Related Event
    TopicCreate(EventCategory.TopicRelatedEvent,
            "e31",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    TopicMemberInvite(EventCategory.TopicRelatedEvent,
            "e32",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.MemberCount, PropertyKey.ErrorCode}),

    TopicStar(EventCategory.TopicRelatedEvent,
            "e33",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    TopicUnStar(EventCategory.TopicRelatedEvent,
            "e34",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    TopicNameChange(EventCategory.TopicRelatedEvent,
            "e35",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    TopicLeave(EventCategory.TopicRelatedEvent,
            "e36",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    TopicDelete(EventCategory.TopicRelatedEvent,
            "e37",
            new PropertyKey[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode}),

    PollCreated(EventCategory.PollEvent,
            "e60",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId}),
    PollFinished(EventCategory.PollEvent,
            "e61",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId}),
    PollDeleted(EventCategory.PollEvent,
            "e62",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId}),
    PollVoted(EventCategory.PollEvent,
            "e63",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId,
                    PropertyKey.PollItemId}),

    PollMemberOfVoted(EventCategory.PollEvent,
            "e64",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.PollId}),

    PollCommentCreated(EventCategory.PollEvent,
            "e65",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.PollCommentId}),

    PollCommentDeleted(EventCategory.PollEvent,
            "e66",
            new PropertyKey[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.PollCommentId}),
    // Button Touch Event
    ButtonTouch(EventCategory.ButtonTouchEvent,
            "",
            new PropertyKey[]{});

    EventCategory eventCategory;
    String eventName;
    PropertyKey[] availablePropertyKeys;

    Event(EventCategory category, String name, PropertyKey[] propertyKeys) {
        eventCategory = category;
        eventName = name;
        availablePropertyKeys = propertyKeys;
    }

    public String getName() {
        return eventName;
    }

    public EventCategory getCategory() {
        return eventCategory;
    }

    public PropertyKey[] getAvailablePropertyKeys() {
        return availablePropertyKeys;
    }
}
