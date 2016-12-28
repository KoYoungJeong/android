package com.tosslab.jandi.app.ui.entities.disabled.presenter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.disabled.model.DisabledEntityChooseModel;

import java.util.List;

import javax.inject.Inject;

public class DisabledEntityChoosePresenterImpl implements DisabledEntityChoosePresenter {
    private View view;
    DisabledEntityChooseModel model;

    @Inject
    public DisabledEntityChoosePresenterImpl(View view, DisabledEntityChooseModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void initDisabledMembers() {
        List<ChatChooseItem> disabledMembers = model.getDisabledMembers();
        view.setDisabledMembers(disabledMembers);
    }
}
