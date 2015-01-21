package com.tosslab.jandi.app.ui.message.v2;

import android.content.Context;
import android.widget.Button;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListPresenter {

    private static final Logger logger = Logger.getLogger(MessageListPresenter.class);


    @ViewById(R.id.list_messages)
    StickyListHeadersListView messageListView;

    @ViewById(R.id.btn_send_message)
    Button sendButton;

    @RootContext
    Context context;

    private MessageListAdapter messageListAdapter;

    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        messageListAdapter = new MessageListAdapter(context);

        progressWheel = new ProgressWheel(context);
        progressWheel.init();
    }

    @AfterViews
    void initViews() {
        messageListView.setAreHeadersSticky(false);
        messageListView.setAdapter(messageListAdapter);

    }

    @UiThread
    public void addAll(int position, List<ResMessages.Link> messages) {
        messageListAdapter.addAll(position, messages);
        messageListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void setEnableSendButton(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    public int getLastItemPosition() {
        return messageListAdapter.getCount() - 1;
    }

    @UiThread
    public void moveLastPage() {
        if (messageListView != null) {
            messageListView.setSelection(messageListAdapter.getCount() - 1);
        }
    }

    public void setLoadingComplete() {
        messageListAdapter.setLoadingComplete();
    }

    public void setNoMoreLoading() {
        messageListAdapter.setNoMoreLoading();
    }

    @UiThread
    public void moveToMessage(int linkId, int firstVisibleItemTop) {
        int itemPosition = messageListAdapter.getItemPositionByLinkId(linkId);
        messageListView.setSelectionFromTop(itemPosition, firstVisibleItemTop);
    }

    public int getFirstVisibleItemLinkId() {
        return messageListAdapter.getItem(messageListView.getFirstVisiblePosition()).messageId;
    }

    public int getFirstVisibleItemTop() {
        return messageListView.getChildAt(0).getTop();
    }
}
