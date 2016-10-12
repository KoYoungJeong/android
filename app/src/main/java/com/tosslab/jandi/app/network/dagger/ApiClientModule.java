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
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.sticker.StickerApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.client.teams.sendmessage.SendMessageApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {

    @Provides
    RetrofitBuilder provideRetrofitAdapterBuilder() {
        return RetrofitBuilder.getInstance();
    }

    @Provides
    ChannelMessageApi provideChannelMessageApi(RetrofitBuilder retrofitBuilder) {
        return new ChannelMessageApi(retrofitBuilder);
    }

    @Provides
    GroupMessageApi provideGroupMessageApi(RetrofitBuilder retrofitBuilder) {
        return new GroupMessageApi(retrofitBuilder);
    }

    @Provides
    DirectMessageApi provideDirectMessageApi(RetrofitBuilder retrofitBuilder) {
        return new DirectMessageApi(retrofitBuilder);
    }

    @Provides
    SendMessageApi provideSendMessageApi(RetrofitBuilder retrofitBuilder) {
        return new SendMessageApi(retrofitBuilder);
    }

    @Provides
    MessageApi provideMessageApi(RetrofitBuilder retrofitBuilder) {
        return new MessageApi(retrofitBuilder);
    }

    @Provides
    StickerApi provideStickerApi(RetrofitBuilder retrofitBuilder) {
        return new StickerApi(retrofitBuilder);
    }

    @Provides
    GroupApi provideGroupApi(RetrofitBuilder retrofitBuilder) {
        return new GroupApi(retrofitBuilder);
    }

    @Provides
    ChannelApi provideChannelApi(RetrofitBuilder retrofitBuilder) {
        return new ChannelApi(retrofitBuilder);
    }

    @Provides
    StarredEntityApi provideStarredEntityApi(RetrofitBuilder retrofitBuilder) {
        return new StarredEntityApi(retrofitBuilder);
    }

    @Provides
    TeamApi provideTeamApi(RetrofitBuilder retrofitBuilder) {
        return new TeamApi(retrofitBuilder);
    }

    @Provides
    ProfileApi provideProfileApi(RetrofitBuilder retrofitBuilder) {
        return new ProfileApi(retrofitBuilder);
    }

    @Provides
    FileApi provideFileApi(RetrofitBuilder retrofitBuilder) {
        return new FileApi(retrofitBuilder);
    }

    @Provides
    CommentApi provideCommentApi(RetrofitBuilder retrofitBuilder) {
        return new CommentApi(retrofitBuilder);
    }

    @Provides
    FolderApi provideFolderApi(RetrofitBuilder retrofitBuilder) {
        return new FolderApi(retrofitBuilder);
    }

    @Provides
    MessageSearchApi provideMessageSearchApi(RetrofitBuilder retrofitBuilder) {
        return new MessageSearchApi(retrofitBuilder);
    }

    @Provides
    EventsApi provideEventsApi(RetrofitBuilder retrofitBuilder) {
        return new EventsApi(retrofitBuilder);
    }

    @Provides
    DeviceApi provideDeviceApi(RetrofitBuilder retrofitBuilder) {
        return new DeviceApi(retrofitBuilder);
    }

    @Provides
    AccountEmailsApi provideAccountEmailsApi(RetrofitBuilder retrofitBuilder) {
        return new AccountEmailsApi(retrofitBuilder);
    }

    @Provides
    AccountPasswordApi provideAccountPasswordApi(RetrofitBuilder retrofitBuilder) {
        return new AccountPasswordApi(retrofitBuilder);
    }

    @Provides
    AccountApi provideAccountApi(RetrofitBuilder retrofitBuilder) {
        return new AccountApi(retrofitBuilder);
    }

    @Provides
    ChatApi provideChatApi(RetrofitBuilder retrofitBuilder) {
        return new ChatApi(retrofitBuilder);
    }

    @Provides
    InvitationApi provideInvitationApi(RetrofitBuilder retrofitBuilder) {
        return new InvitationApi(retrofitBuilder);
    }

    @Provides
    ConfigApi provideConfigApi(RetrofitBuilder retrofitBuilder) {
        return new ConfigApi(retrofitBuilder);
    }

    @Provides
    LoginApi provideLoginApi(RetrofitBuilder retrofitBuilder) {
        return new LoginApi(retrofitBuilder);
    }

    @Provides
    SignUpApi provideSignUpApi(RetrofitBuilder retrofitBuilder) {
        return new SignUpApi(retrofitBuilder);
    }

    @Provides
    PlatformApi providePlatformApi(RetrofitBuilder retrofitBuilder) {
        return new PlatformApi(retrofitBuilder);
    }

    @Provides
    AnnounceApi provideAnnounceApi(RetrofitBuilder retrofitBuilder) {
        return new AnnounceApi(retrofitBuilder);
    }

    @Provides
    RoomsApi provideRoomsApi(RetrofitBuilder retrofitBuilder) {
        return new RoomsApi(retrofitBuilder);
    }

    @Provides
    AccountProfileApi provideAccountProfileApi(RetrofitBuilder retrofitBuilder) {
        return new AccountProfileApi(retrofitBuilder);
    }

    @Provides
    ValidationApi provideValidationApi(RetrofitBuilder retrofitBuilder) {
        return new ValidationApi(retrofitBuilder);
    }

    @Provides
    StartApi provideStartApi(RetrofitBuilder retrofitBuilder) {
        return new StartApi(retrofitBuilder);
    }

    @Provides
    PollApi providePollApi(RetrofitBuilder retrofitBuilder) {
        return new PollApi(retrofitBuilder);
    }

    @Provides
    SearchApi provideSearchApi(RetrofitBuilder retrofitBuilder) {
        return new SearchApi(retrofitBuilder);
    }

}
