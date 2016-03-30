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
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {

    @Provides
    RetrofitAdapterBuilder provideRetrofitAdapterBuilder() {
        return RetrofitAdapterBuilder.newInstance();
    }

    @Provides
    ChannelMessageApi provideChannelMessageApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ChannelMessageApi(retrofitAdapterBuilder);
    }

    @Provides
    GroupMessageApi provideGroupMessageApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new GroupMessageApi(retrofitAdapterBuilder);
    }

    @Provides
    DirectMessageApi provideDirectMessageApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new DirectMessageApi(retrofitAdapterBuilder);
    }

    @Provides
    MessageApi provideMessageApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new MessageApi(retrofitAdapterBuilder);
    }

    @Provides
    StickerApi provideStickerApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new StickerApi(retrofitAdapterBuilder);
    }

    @Provides
    LeftSideApi provideLeftSideApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new LeftSideApi(retrofitAdapterBuilder);
    }

    @Provides
    GroupApi provideGroupApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new GroupApi(retrofitAdapterBuilder);
    }

    @Provides
    ChannelApi provideChannelApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ChannelApi(retrofitAdapterBuilder);
    }

    @Provides
    StarredEntityApi provideStarredEntityApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new StarredEntityApi(retrofitAdapterBuilder);
    }

    @Provides
    TeamApi provideTeamApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new TeamApi(retrofitAdapterBuilder);
    }

    @Provides
    ProfileApi provideProfileApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ProfileApi(retrofitAdapterBuilder);
    }

    @Provides
    FileApi provideFileApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new FileApi(retrofitAdapterBuilder);
    }

    @Provides
    CommentApi provideCommentApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new CommentApi(retrofitAdapterBuilder);
    }

    @Provides
    FolderApi provideFolderApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new FolderApi(retrofitAdapterBuilder);
    }

    @Provides
    MessageSearchApi provideMessageSearchApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new MessageSearchApi(retrofitAdapterBuilder);
    }

    @Provides
    EventsApi provideEventsApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new EventsApi(retrofitAdapterBuilder);
    }

    @Provides
    DeviceApi provideDeviceApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new DeviceApi(retrofitAdapterBuilder);
    }

    @Provides
    AccountEmailsApi provideAccountEmailsApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new AccountEmailsApi(retrofitAdapterBuilder);
    }

    @Provides
    AccountPasswordApi provideAccountPasswordApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new AccountPasswordApi(retrofitAdapterBuilder);
    }

    @Provides
    AccountApi provideAccountApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new AccountApi(retrofitAdapterBuilder);
    }

    @Provides
    ChatApi provideChatApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ChatApi(retrofitAdapterBuilder);
    }

    @Provides
    InvitationApi provideInvitationApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new InvitationApi(retrofitAdapterBuilder);
    }

    @Provides
    ConfigApi provideConfigApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ConfigApi(retrofitAdapterBuilder);
    }

    @Provides
    LoginApi provideLoginApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new LoginApi(retrofitAdapterBuilder);
    }

    @Provides
    SignUpApi provideSignUpApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new SignUpApi(retrofitAdapterBuilder);
    }

    @Provides
    PlatformApi providePlatformApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new PlatformApi(retrofitAdapterBuilder);
    }

    @Provides
    AnnounceApi provideAnnounceApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new AnnounceApi(retrofitAdapterBuilder);
    }

    @Provides
    RoomsApi provideRoomsApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new RoomsApi(retrofitAdapterBuilder);
    }

    @Provides
    AccountProfileApi provideAccountProfileApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new AccountProfileApi(retrofitAdapterBuilder);
    }

    @Provides
    ValidationApi provideValidationApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        return new ValidationApi(retrofitAdapterBuilder);
    }

}
