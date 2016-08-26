package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenterImpl;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.interfaces.actions.OpenAction;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.profile.defaultimage.model.ProfileImageSelectorModel;
import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchManager;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.sign.signup.verify.model.SignUpVerifyModel;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface ApiClientComponent {
    void inject(MessageManipulator injector);

    void inject(EntityClientManager injector);

    void inject(SignUpVerifyModel signUpVerifyModel);

    void inject(AccountHomePresenterImpl accountHomePresenter);

    void inject(OpenAction openAction);

    void inject(ShareModel shareModel);

    void inject(CarouselViewerModel carouselViewerModel);

    void inject(FileListModel fileListModel);

    void inject(MainChatListModel mainChatListModel);

    void inject(EntityMenuDialogModel entityMenuDialogModel);

    void inject(MessageSearchManager messageSearchManager);

    void inject(MessageListModel messageListModel);

    void inject(FileDetailModel fileDetailModel);

    void inject(ProfileImageSelectorModel profileImageSelectorModel);

    void inject(MembersModel membersModel);

    void inject(MainTopicModel mainTopicModel);

    void inject(InvitationDialogExecutor invitationDialogExecutor);

    void inject(AnnouncementModel announcementModel);

    void inject(TopicFolderSettingModel topicFolderSettingModel);

    void inject(com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel topicFolderSettingModel);
}
