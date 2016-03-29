package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApi;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.messages.comments.CommentApi;
import com.tosslab.jandi.app.network.client.messages.search.MessageSearchApi;
import com.tosslab.jandi.app.network.client.platform.PlatformApi;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApi;
import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApi;
import com.tosslab.jandi.app.network.client.rooms.AnnounceApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApi;
import com.tosslab.jandi.app.network.client.sticker.StickerApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
@Singleton
public class ApiClientModule {

    @Provides
    ChannelMessageApi provideChannelMessageApi() {
        return new ChannelMessageApi();
    }

    @Provides
    GroupMessageApi provideGroupMessageApi() {
        return new GroupMessageApi();
    }

    @Provides
    DirectMessageApi provideDirectMessageApi() {
        return new DirectMessageApi();
    }

    @Provides
    MessageApi provideMessageApi() {
        return new MessageApi();
    }

    @Provides
    StickerApi provideStickerApi() {
        return new StickerApi();
    }

    @Provides
    LeftSideApi provideLeftSideApi() {
        return new LeftSideApi();
    }

    @Provides
    GroupApi provideGroupApi() {
        return new GroupApi();
    }

    @Provides
    ChannelApi provideChannelApi() {
        return new ChannelApi();
    }

    @Provides
    StarredEntityApi provideStarredEntityApi() {
        return new StarredEntityApi();
    }

    @Provides
    TeamApi provideTeamApi() {
        return new TeamApi();
    }

    @Provides
    ProfileApi provideProfileApi() {
        return new ProfileApi();
    }

    @Provides
    FileApi provideFileApi() {
        return new FileApi();
    }

    @Provides
    CommentApi provideCommentApi() {
        return new CommentApi();
    }

    @Provides
    FolderApi provideFolderApi() {
        return new FolderApi();
    }

    @Provides
    MessageSearchApi provideMessageSearchApi() {
        return new MessageSearchApi();
    }

    @Provides
    EventsApi provideEventsApi() {
        return new EventsApi();
    }

    @Provides
    DeviceApi provideDeviceApi() {
        return new DeviceApi();
    }

    @Provides
    AccountEmailsApi provideAccountEmailsApi() {
        return new AccountEmailsApi();
    }

    @Provides
    AccountPasswordApi provideAccountPasswordApi() {
        return new AccountPasswordApi();
    }

    @Provides
    AccountApi provideAccountApi() {
        return new AccountApi();
    }

    @Provides
    ChatApi provideChatApi() {
        return new ChatApi();
    }

    @Provides
    InvitationApi provideInvitationApi() {
        return new InvitationApi();
    }

    @Provides
    ConfigApi provideConfigApi() {
        return new ConfigApi();
    }

    @Provides
    LoginApi provideLoginApi() {
        return new LoginApi();
    }

    @Provides
    SignUpApi provideSignUpApi() {
        return new SignUpApi();
    }

    @Provides
    PlatformApi providePlatformApi() {
        return new PlatformApi();
    }

    @Provides
    AnnounceApi provideAnnounceApi() {
        return new AnnounceApi();
    }

    @Provides
    RoomsApi provideRoomsApi() {
        return new RoomsApi();
    }

    @Provides
    AccountProfileApi provideAccountProfileApi() {
        return new AccountProfileApi();
    }

    @Provides
    ValidationApi provideValidationApi() {
        return new ValidationApi();
    }

}
