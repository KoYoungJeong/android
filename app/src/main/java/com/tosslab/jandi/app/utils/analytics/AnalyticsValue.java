package com.tosslab.jandi.app.utils.analytics;

public class AnalyticsValue {
    public enum Screen {
        PushNotification,
        StartPage,
        SignIn,
        SignUp,
        CodeVerification,
        AccountHome,
        ChooseAnEmail,
        CreateaTeam,
        SharetoJandi,
        MsgSearch,
        FilesSearch,
        InviteTeamMember,
        TopicsTab,
        MoveToaFolder,
        BrowseOtherTopics,
        TopicChat,
        TopicDescription,
        Participants,
        InviteTeamMembers,
        MessageTab,
        Message,
        MessageDescription,
        FilesTab,
        FileDetail,
        MoreTab,
        EditProfile,
        Mentions,
        Stars,
        TeamMembers,
        Invite,
        SwitchTeams,
        Help,
        Setting,
        UserProfile,
        InputPasscode, WrongPasscode, SetPasscode, MyProfile,
        SwitchTeam, MypageTab, Account, TeamTab,
        Polls, PollDetail, CreatePoll, PollParticipant, ChoiceParticipant,
        UniversalSearch, ChooseMember, SelectRoom, Carousel,
        ForceTeamCreation,
        FolderManagement, InviteTeamMembers_Department, InviteTeamMembers_JobTitle, InviteMemberSearch, SelectTeamMember, SelectTeamMemberSearch, TeamInformation, TeamTab_Department, TeamTab_JobTitle, TeamTabSearch, ImageFullScreen, HamburgerMenu, NotificationSetting, PasscodeLock, AccountSetting, screen, SelectTeamMember_Department, SelectTeamMember_JobTitle, Upload_Photo, Upload_Camera, Upload_File, TeamPhoneNumberSetting, EditAccount
    }

    public enum Action {
        ForgotPW,
        SignIn,
        SignUp,
        Submit,
        TermofService,
        AcceptAll,
        AgreeTermsofService,
        AgreePrivacyPolicy,
        SignUpNow,
        LaunchJandi,
        Resend,
        AccountName,
        ChooseAnEmail,
        ChooseEmail,
        AddNewEmail,
        TeamSelect,
        TopicSelect,
        TapComment,
        DeleteInputField,
        ChooseSearchResult,
        GoToFilesSearch,
        GoToMsgSearch,
        SendInvitations,
        Later,
        CloseModal,
        CreateNewTopic,
        ChoosePublicTopic,
        ChoosePrivateTopic,
        BrowseOtherTopics,
        TopicSubMenu,
        TopicSubMenu_Move,
        TopicSubMenu_Star,
        TopicSubMenu_Unstar,
        TopicSubMenu_Cancel,
        TopicSubMenu_Leave,
        TopicSubMenu_Close,
        TopicFolderExpand,
        TopicFolderCollapse,
        TopicFolder_Rename,
        TopicFolder_Delete,
        NewFolder,
        RemoveFromThisFolder,
        ChooseFolder,
        JoinTopic,
        ViewTopicInfo,
        Announcement_Expand,
        Announcement_ExpandFromMinimize,
        Accouncement_Restore,
        Accouncement_Delete,
        Accouncement_Minimize,
        TopicChat_Search,
        TopicChat_File,
        TopicChat_Decription,
        TopicName,
        TopicDescription,
        Participants,
        Delete,
        InviteTeamMembers,
        SelectMember,
        Invite,
        SearchInviteMember,
        ChooseDM,
        SelectTeamMember,
        SelectTeamMember_EmptyData,
        Search,
        Upload,
        Upload_Photo,
        Upload_Camera,
        Upload_File,
        Upload_Video,
        Upload_Poll,
        Upload_Cancel,
        Upload_Close,
        Message_Search,
        Message_File,
        Message_Decription,
        ViewProfile_Mention,
        ViewProfile_MyMention,
        ViewProfile_SysMessage,
        FileView_ByPhoto,
        FileView_ByFile,
        FileView_ByComment,
        MsgLongTap,
        MsgLongTap_Copy,
        MsgLongTap_Star,
        MsgLongTap_Unstar,
        MsgLongTap_Delete,
        MsgLongTap_Announce,
        MsgCellPhone,
        MsgEmail,
        MsgURL,
        Leave,
        OpenTopicFilter,
        CloseTopicFilter,
        OpenMemberFilter,
        CloseMemberFilter,
        OpenTypeFilter,
        CloseTypeFilter,
        ChooseFilteredFile,
        ViewFile,
        ViewPhoto,
        TurnOnStar,
        TurnOffStar,
        ResendInvitation,
        CancelInvitation,
        FileSubMenu,
        FileSubMenu_Download,
        FileSubMenu_Share,
        FileSubMenu_UnShare,
        FileSubMenu_Delete,
        FileSubMenu_Export,
        FileSubMenu_CreatePublicLink,
        FileSubMenu_CopyLink,
        FileSubMenu_DeleteLink,
        ViewOriginalImage,
        MoveToLeft_Click,
        MoveToLeft_Swipe,
        MoveToRight_Click,
        MoveToRight_Swipe,
        TapCommentCount,
        Close,
        CloseBySwipe,
        Download,
        TapCommentIcon,
        TapSharedTopic,
        ViewProfile_FromComment,
        CommentLongTap,
        CommentLongTap_Star,
        CommentLongTap_Unstar,
        Sticker,
        Sticker_RecentTab,
        Sticker_StickerTab,
        Sticker_Select,
        Sticker_cancel,
        Sticker_Send,
        Send,
        EditProfile,
        Mentions,
        Stars,
        TeamMembers,
        InviteMember,
        TeamSwitch,
        Help,
        Setting,
        PhotoEdit,
        Name,
        Email,
        Status,
        Mobile,
        PhoneNumber,
        Department,
        JobTitle,
        Division,
        Position,
        ChooseMention,
        ChooseMsg,
        ChooseFile,
        ChooseComment,
        Filter_All,
        Filter_Files,
        SearchInputField,
        ViewProfile,
        ViewProfile_Image,
        Profile_Photo,
        Profile_Email,
        Profile_Cellphone,
        InviteMember_Email,
        InviteMember_KakaoTalk,
        InviteMember_Line,
        InviteMember_WeChat,
        InviteMember_FBMessenger,
        InviteMember_CopyLink,
        CreateTeam,
        ChooseTeam,
        AcceptTeamInvitation,
        IgnoreTeamInvitation,
        TurnOnNotifications,
        TurnOffNotifications,
        SoundsOn,
        SoundsOff,
        VibrateOn,
        VibrateOff,
        PhoneLedOn,
        PhoneLedOff,
        AllMessages,
        None,
        TermsOfService,
        PrivacyPolicy,
        SignOut,
        KickMember,
        ShowProfilePicture,
        TapPhoneNumber, TapEmailAddress,
        DirectMessage, TurnOffPasscode, TurnOnPasscode, ChangePasscode, ChangeTopicOrder, Call,
        CreateNewTeam, MemberSearch, ViewMyProfile,
        Notifications, Sounds, Vibrate, PhoneLed,
        Passcode, Account, SetPasscode, ChooseTopicFilter, ChooseMemberFilter, ChooseTypeFilter, Star, Polls, OpenTeamList,
        ViewPollDetail, ViewChoiceParticipant, ViewPollParticipant, ClosePoll, DeletePoll, AddChoice, DeleteChoice, Anonymous, AllowMultipleChoices, CreatePoll, ViewMember, Vote,
        TapRecentKeywords, DeleteRecentKeyword, DeleteAllKeywords, GoSearchResult, IncludeNotJoinedTopics,
        ChooseJoinedTopic, ChooseUnJoinedTopic, ChooseDm, CollapseRoomList, ChooseRoomFilter, TapMsgSearchResult,
        ChooseTopic, KeywordSearch, ChooseAllMember, ChooseMember,
        TapPlusButton, TapPlusButton_CreateNewTopic, TapPlusButton_CreateNewFolder, TapPlusButton_FolderManagement, TapPlusButton_BrowseOtherTopics,
        TopicSubMenu_Notification,
        SubmitTeam, GoToJANDIMain,
        SubmitATeam, NewFolderonTop, CreateNewFolder, EditAccount,
        IncorrectPasscode, CorrectPasscode, DeleteaFolder, ArrangeaFolder,
        TapLinkPreview, TransferTopicAdmin, AutoInvitation,
        MembersTab, JobTitlesTab, DepartmentsTab,
        MembersTab_SelectMember, MembersTab_UnSelectMember, MembersTab_SelectAll, MembersTab_Invite,
        CancelSelect, ChooseDepartment, ChooseJobTitle, ChooseDepartment_Undefined, ChooseJobTitle_Undefined,
        UnselectMember, InviteMembers, SubmitSearch, MembersTab_CancelSelect, ViewVoteCreator, StarVote, SubmitVote,
        CommentLongTap_Copy, CommentLongTap_Delete, MessageInputField, ChooseJANDI, SearchSelectTeamMember,
        InviteMember_InviteDisabled, TeamInformation, MentionTab, StarTab, PollTab, MentionTab_ChooseTopicMsg, MentionTab_ChooseFileComment, StarTab_ChooseMsg, StarTab_ChooseFile, StarTab_ChoosePoll, StarTab_ChooseFileComment, PollTab_ChooseOngoingVoted, PollTab_ChooseOngoingUnvoted, PollTab_ChooseCompleted, HamburgerIcon, HamburgerSwipe, HamburgerLongTap, NotificationSetting, PasscodeLock, AccountSetting, LiveSupport, TermsofService, FAQ, VersionInfo, VersionInfo_Close, VersionInfo_Email, Sounds_Topic, Sounds_DM, Sounds_Mention, PreviewMsgContents, UseFingerPrint, Members_ChooseMember, EditFolder_Rename, CallPreview, UpgradePlan_More, UpgradePlan_LiveSupport, UpgradePlan_Cancel, Whoscall_MoveToSetting, Whoscall_Close, Whoscall_DontShowAgain, MentionLongTap, MentionLongTap_Copy, MentionLongTap_Star, TeamPhoneNumberSetting, AllowPhoneCalls, DrawOverApps,

    }

    public enum Label {
        Folder, On, Off, UpdateDate,
        AllTopic, Topic, Member,
        AllMember,
        AllType, GoogleDocs, Words, Presentations, Spreadsheets, PDFs, Images, Videos, Audios,
        ongoing, SubmitATeam, completed,
        AllRoom, JoinedRoom, Zipfiles, SelectRoom,
        PhotoLibrary, Camera, ChooseCharacter,
        Star, text, image, All, Public, None, Unstar
    }
}
