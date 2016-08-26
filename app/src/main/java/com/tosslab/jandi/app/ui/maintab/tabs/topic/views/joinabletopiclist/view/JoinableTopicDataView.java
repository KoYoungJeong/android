package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view;

import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicDataView {
    void notifyDataSetChanged();

    void setOnTopicClickListener(OnRecyclerItemClickListener onTopicClickListener);
}
