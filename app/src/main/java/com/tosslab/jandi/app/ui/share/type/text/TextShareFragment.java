package com.tosslab.jandi.app.ui.share.type.text;

import android.app.Fragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.model.ShareModel;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EFragment(R.layout.fragment_text_share)
public class TextShareFragment extends Fragment {

    @Bean
    TextSharePresenter textSharePresenter;

    @Bean
    ShareModel shareModel;

    @FragmentArg
    String text;

    @AfterInject
    void initObject() {
        textSharePresenter.setText(text);

        List<EntityInfo> entities = shareModel.getEntityInfos();

        textSharePresenter.setEntityInfos(entities);
    }

    @Click(R.id.btn_share_text_send)
    void onSendClick() {
        String messageText = textSharePresenter.getMessageText();
        EntityInfo selectedEntityInfo = textSharePresenter.getSelectedEntityInfo();
        sendMessage(selectedEntityInfo, messageText);
    }

    @Background
    void sendMessage(EntityInfo entity, String messageText) {
        textSharePresenter.showProgressWheel();
        try {
            shareModel.sendMessage(entity, messageText);
            int teamId = shareModel.getTeamId();
            boolean starredEntity = shareModel.isStarredEntity(entity.getEntityId());
            textSharePresenter.moveEntity(teamId, entity, starredEntity);
            finishOnUiThread();
        } catch (JandiNetworkException e) {
            textSharePresenter.showErrorMessage(getString(R.string.err_network));
        } finally {
            textSharePresenter.dismissProgressWheel();
        }
    }

    @UiThread
    void finishOnUiThread() {
        getActivity().finish();
    }
}
