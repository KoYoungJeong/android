package com.tosslab.jandi.app.ui.message.v2.model.file.action;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 4. 20..
 */
public class DeleteAction implements FileAction {

    private final Context context;

    public DeleteAction(Context context) {
        this.context = context;
    }

    @Override
    public void action(ResMessages.Link link) {

    }
}
