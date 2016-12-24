package com.tosslab.jandi.app.ui.message.v2.dagger;


import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.MainFileUploadControllerImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;

import dagger.Module;
import dagger.Provides;

@Module
public class MessageListModule {
    private final MessageListV2Presenter.View view;
    private final Room room;
    private final MessagePointer messagePointer;

    public MessageListModule(MessageListV2Presenter.View view, Room room, MessagePointer messagePointer) {
        this.view = view;
        this.room = room;
        this.messagePointer = messagePointer;
    }

    @Provides
    MessageListV2Presenter.View view() {
        return view;
    }

    @Provides
    Room room() {
        return room;
    }

    @Provides
    MessagePointer messagePointer() {
        return messagePointer;
    }

    @Provides
    FileUploadController fileUploadController() {
        return new MainFileUploadControllerImpl();
    }
}
