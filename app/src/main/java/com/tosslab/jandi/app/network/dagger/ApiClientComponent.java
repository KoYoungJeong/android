package com.tosslab.jandi.app.network.dagger;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.file.upload.preview.model.FileUploadModel;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.interfaces.actions.OpenAction;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface ApiClientComponent {
    void inject(MessageManipulator injector);

    void inject(EntityClientManager injector);

    void inject(OpenAction openAction);

    void inject(CarouselViewerModel carouselViewerModel);

    void inject(FileListModel fileListModel);

    void inject(MainChatListModel mainChatListModel);

    void inject(EntityMenuDialogModel entityMenuDialogModel);

    void inject(MessageListModel messageListModel);

    void inject(FileDetailModel fileDetailModel);

    void inject(MembersModel membersModel);

    void inject(AnnouncementModel announcementModel);

    void inject(FileUploadModel fileUploadModel);
}
