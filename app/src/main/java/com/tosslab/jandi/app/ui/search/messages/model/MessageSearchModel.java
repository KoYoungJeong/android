package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;

import com.tosslab.jandi.app.network.manager.RequestManager;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchModel {

    @RootContext
    Context context;

    public void requestSearchQuery(String query) {
//        RequestManager.newInstance(context, )
    }
}
