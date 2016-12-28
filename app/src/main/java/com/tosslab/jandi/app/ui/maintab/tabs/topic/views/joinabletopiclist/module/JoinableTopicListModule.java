package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.module;

import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.adapter.JoinableTopicListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model.JoinableTopicDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.presenter.JoinableTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.presenter.JoinableTopicListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicListView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 4. 5..
 */
@Module
public class JoinableTopicListModule {

    private final JoinableTopicListView view;
    private final JoinableTopicListAdapter adapter;

    public JoinableTopicListModule(JoinableTopicListView view,
                                   JoinableTopicListAdapter adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    @Provides
    public JoinableTopicListView provideJoinableTopicListView() {
        return view;
    }

    @Provides
    public JoinableTopicDataModel provideJoinableTopicDataModel() {
        return adapter;
    }

    @Provides
    public JoinableTopicDataView provideJoinableTopicDataView() {
        return adapter;
    }

    @Provides
    public JoinableTopicListPresenter provideJoinableTopicListPresenter(
            JoinableTopicListPresenterImpl presenter) {
        return presenter;
    }

}
