package com.tosslab.jandi.app.ui.message.model;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.lists.messages.MessageItemListAdapter;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class RefreshRequestor {

    Context context;
    MessageItemListAdapter messageItemListAdapter;
    MessageManipulator mJandiMessageClient;
    MessageState messageState;
    MessageItemConverter mMessageItemConverter;

    public RefreshRequestor(Context context, MessageItemListAdapter messageItemListAdapter, MessageManipulator mJandiMessageClient, MessageState messageState, MessageItemConverter mMessageItemConverter) {
        this.context = context;
        this.messageItemListAdapter = messageItemListAdapter;
        this.mJandiMessageClient = mJandiMessageClient;
        this.messageState = messageState;
        this.mMessageItemConverter = mMessageItemConverter;
    }

    public String getMoreMessageResult() {
        return doInBackground();
    }

    protected String doInBackground(Void... voids) {
        try {
            ResMessages restResMessages = mJandiMessageClient.getMessages(messageState.getFirstItemId(), 20);
            mMessageItemConverter.insertMessageItem(restResMessages);
            messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
            return null;
        } catch (JandiNetworkException e) {
            LogUtil.e("GetFutherMessagesTask : FAILED", e);
            return context.getString(R.string.err_messages_get);
        } catch (Exception e) {
            return context.getString(R.string.err_messages_get);
        }
    }

    public interface Callback {
        void onResult(String errorMsg, int lastCount);
    }
}
