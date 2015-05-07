package com.tosslab.jandi.app.ui.share.type.text;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

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
public class TextShareDialogFragment extends DialogFragment {

    @Bean
    TextSharePresenter textSharePresenter;

    @Bean
    ShareModel shareModel;

    @FragmentArg
    String subject;

    @FragmentArg
    String text;

    @AfterInject
    void initObject() {

        textSharePresenter.setText(subject, text);

        List<EntityInfo> entities = shareModel.getEntityInfos();

        textSharePresenter.setEntityInfos(entities);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.jandi_share_to_jandi);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }

    @Click(R.id.btn_share_text_send)
    void onSendClick() {
        String messageText = textSharePresenter.getMessageText().trim();
        EntityInfo selectedEntityInfo = textSharePresenter.getSelectedEntityInfo();
        sendMessage(selectedEntityInfo, messageText);
    }

    @Background
    void sendMessage(EntityInfo entity, String messageText) {
        textSharePresenter.showProgressWheel();
        try {
            shareModel.sendMessage(entity, messageText);
            textSharePresenter.showSuccessMessage(getString(R.string.jandi_share_succeed, getString(R.string.jandi_message_hint)));
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
