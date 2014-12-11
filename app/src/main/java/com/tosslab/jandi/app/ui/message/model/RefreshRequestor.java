package com.tosslab.jandi.app.ui.message.model;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.lists.messages.MessageItemListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class RefreshRequestor implements Runnable {

    private static final Logger log = Logger.getLogger(RefreshRequestor.class);

    private int currentMessagesSize;

    Context context;
    MessageItemListAdapter messageItemListAdapter;
    MessageManipulator mJandiMessageClient;
    MessageState messageState;
    MessageItemConverter mMessageItemConverter;
    private final Callback callback;

    public RefreshRequestor(Context context, MessageItemListAdapter messageItemListAdapter, MessageManipulator mJandiMessageClient, MessageState messageState, MessageItemConverter mMessageItemConverter, Callback callback) {
        this.context = context;
        this.messageItemListAdapter = messageItemListAdapter;
        this.mJandiMessageClient = mJandiMessageClient;
        this.messageState = messageState;
        this.mMessageItemConverter = mMessageItemConverter;
        this.callback = callback;
    }

    @Override
    public void run() {
        onPreExecute();
        String result = doInBackground();

        if (callback != null) {
            callback.onResult(result, currentMessagesSize);
        }
    }

    protected void onPreExecute() {
        currentMessagesSize = messageItemListAdapter.getCount();
    }

    protected String doInBackground(Void... voids) {
        try {
            ResMessages restResMessages = mJandiMessageClient.getMessages(messageState.getFirstItemId());
            mMessageItemConverter.insertMessageItem(restResMessages);
            messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
            // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
            messageState.setFirstMessage(restResMessages.isFirst);
            // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
            messageState.setFirstItemId(restResMessages.firstIdOfReceivedList);

            log.debug("GetFutherMessagesTask : " + restResMessages.messageCount
                    + " messages from " + messageState.getFirstItemId());
            return null;
        } catch (RestClientException e) {
            log.error("GetFutherMessagesTask : FAILED", e);
            return context.getString(R.string.err_messages_get);
        }
    }

    public interface Callback {
        void onResult(String errorMsg, int lastCount);
    }
}
