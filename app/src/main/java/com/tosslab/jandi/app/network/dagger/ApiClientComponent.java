package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenterImpl;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.interfaces.actions.OpenAction;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.ui.intro.signup.verify.model.SignUpVerifyModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.profile.defaultimage.model.ProfileImageSelectorModel;
import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchManager;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.ui.starmention.model.StarMentionListModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface ApiClientComponent {
    void inject(MessageManipulator injector);

    void inject(EntityClientManager injector);

    void inject(JandiInterfaceModel jandiInterfaceModel);

    void inject(IntroActivityModel introActivityModel);

    void inject(SignUpVerifyModel signUpVerifyModel);

    void inject(AccountHomePresenterImpl accountHomePresenter);

    void inject(OpenAction openAction);

    void inject(TeamDomainInfoModel teamDomainInfoModel);

    void inject(AccountHomeModel accountHomeModel);

    void inject(ShareModel shareModel);

    void inject(ShareSelectModel shareSelectModel);

    void inject(CarouselViewerModel carouselViewerModel);

    void inject(FileListModel fileListModel);

    void inject(MainChatListModel mainChatListModel);

    void inject(EntityMenuDialogModel entityMenuDialogModel);

    void inject(LeaveViewModel leaveViewModel);

    void inject(MessageSearchManager messageSearchManager);

    void inject(MessageListModel messageListModel);

    void inject(FileDetailModel fileDetailModel);

    void inject(ProfileImageSelectorModel profileImageSelectorModel);

    void inject(MembersModel membersModel);

    void inject(TopicFolderSettingModel topicFolderSettingModel);

    void inject(MainTopicModel mainTopicModel);

    void inject(com.tosslab.jandi.app.ui.maintab.topic.dialog.model.TopicFolderSettingModel topicFolderSettingModel);

    void inject(StarMentionListModel starMentionListModel);

    void inject(InvitationDialogExecutor invitationDialogExecutor);

    void inject(AnnouncementModel announcementModel);

    void inject(TopicDetailModel topicDetailModel);
}
