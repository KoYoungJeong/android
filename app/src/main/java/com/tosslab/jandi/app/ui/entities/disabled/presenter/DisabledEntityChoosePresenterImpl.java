package com.tosslab.jandi.app.ui.entities.disabled.presenter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.disabled.model.DisabledEntityChooseModel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class DisabledEntityChoosePresenterImpl implements DisabledEntityChoosePresenter {
    private View view;

    @Bean
    DisabledEntityChooseModel model;

    @Override
    public void setView(View view) {

        this.view = view;
    }

    @Override
    public void initDisabledMembers() {
        List<ChatChooseItem> disabledMembers = model.getDisabledMembers();
        view.setDisabledMembers(disabledMembers);
    }
}
