package com.tosslab.jandi.app.ui.entities.disabled.presenter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import java.util.List;

public interface DisabledEntityChoosePresenter {

    void initDisabledMembers();

    interface View {

        void setDisabledMembers(List<ChatChooseItem> disabledMembers);
    }
}
