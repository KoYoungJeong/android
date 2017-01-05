package com.tosslab.jandi.app.utils.analytics.sprinkler;

import com.tosslab.jandi.lib.sprinkler.io.domain.event.Event;

public class SprinklerEvents {

    public static final Event SignIn = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e6",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.AutoSignIn, PropertyKey.ErrorCode});

    public static final Event SendAccountVerificationMail = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e9",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode});

    public static final Event ResendAccountVerificationMail = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e10",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode});

    public static final Event SignUp = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e11",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.ErrorCode});

    public static final Event SignOut = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e12",
            new String[]{});

    public static final Event LaunchTeam = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e13",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TeamId, PropertyKey.ErrorCode});

    public static final Event CreateTeam = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e14",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TeamId, PropertyKey.ErrorCode});

    public static final Event InviteTeam = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e15",
            new String[]{PropertyKey.MemberCount, PropertyKey.TeamId, PropertyKey.RankdId});

    public static final Event ChangeAccountName = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e16",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.ErrorCode});

    public static final Event ChangeAccountPrimaryEmail = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e17",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode});

    public static final Event RequestVerificationEmail = Event.create(EventCategory.AccountRelatedEvent.name(),
            "e20",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.Email, PropertyKey.ErrorCode});

    public static final Event FileUpload = Event.create(EventCategory.FileRelatedEvent.name(),
            "e38",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode});

    public static final Event FileDownload = Event.create(EventCategory.FileRelatedEvent.name(),
            "e39",
            new String[]{PropertyKey.FileId});

    public static final Event FileShare = Event.create(EventCategory.FileRelatedEvent.name(),
            "e40",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode});

    public static final Event FileUnShare = Event.create(EventCategory.FileRelatedEvent.name(),
            "e41",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode});

    public static final Event FileDelete = Event.create(EventCategory.FileRelatedEvent.name(),
            "e42",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.FileId, PropertyKey.ErrorCode});

    public static final Event FileKeywordSearch = Event.create(EventCategory.FileRelatedEvent.name(),
            "e43",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.SearchKeyword, PropertyKey.ErrorCode});

    public static final Event MessagePost = Event.create(EventCategory.MessageRelatedEvent.name(),
            "e44",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.MentionCount,
                    PropertyKey.HasAllMention,
                    PropertyKey.MessageId,
                    PropertyKey.StickerId,
                    PropertyKey.FileId,
                    PropertyKey.PollId
            });

    public static final Event MessageKeywordSearch = Event.create(EventCategory.MessageRelatedEvent.name(),
            "e45",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.SearchKeyword, PropertyKey.ErrorCode});

    public static final Event MessageDelete = Event.create(EventCategory.MessageRelatedEvent.name(),
            "e46",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.MessageId, PropertyKey.ErrorCode});

    public static final Event ScreenView = Event.create(EventCategory.ScreenViewEvent.name(),
            "e48",
            new String[]{PropertyKey.ScreenView});

    public static final Event TopicCreate = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e31",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event TopicMemberInvite = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e32",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.MemberCount, PropertyKey.ErrorCode});

    public static final Event TopicStar = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e33",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event TopicUnStar = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e34",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event TopicNameChange = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e35",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event TopicLeave = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e36",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event TopicDelete = Event.create(EventCategory.TopicRelatedEvent.name(),
            "e37",
            new String[]{PropertyKey.ResponseSuccess, PropertyKey.TopicId, PropertyKey.ErrorCode});

    public static final Event PollCreated = Event.create(EventCategory.PollEvent.name(),
            "e60",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId});

    public static final Event PollFinished = Event.create(EventCategory.PollEvent.name(),
            "e61",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId});

    public static final Event PollDeleted = Event.create(EventCategory.PollEvent.name(),
            "e62",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId});

    public static final Event PollVoted = Event.create(EventCategory.PollEvent.name(),
            "e63",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.MemberId,
                    PropertyKey.TopicId,
                    PropertyKey.PollId});

    public static final Event PollMemberOfVoted = Event.create(EventCategory.PollEvent.name(),
            "e64",
            new String[]{PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.TeamId,
                    PropertyKey.PollId});

    public static final Event Starred = Event.create(EventCategory.StarredEvent.name(),
            "e67",
            new String[]{
                    PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.StarredType,
                    PropertyKey.MessageId,
                    PropertyKey.FileId,
                    PropertyKey.PollId
            });

    public static final Event UnStarred = Event.create(EventCategory.StarredEvent.name(),
            "e68",
            new String[]{
                    PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.StarredType,
                    PropertyKey.MessageId,
                    PropertyKey.FileId,
                    PropertyKey.PollId
            });

    public static final Event PublicLinkCreated = Event.create(EventCategory.PublicLinkEvent.name(),
            "e69",
            new String[]{
                    PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.FileId
            });

    public static final Event PublicLinkDeleted = Event.create(EventCategory.PublicLinkEvent.name(),
            "e70",
            new String[]{
                    PropertyKey.ResponseSuccess,
                    PropertyKey.ErrorCode,
                    PropertyKey.FileId
            });

}
