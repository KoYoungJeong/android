package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.component;

import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.JoinableTopicListActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.module.JoinableTopicListModule;

import dagger.Component;

/**
 * Created by tonyjs on 16. 4. 5..
 */
@Component(modules = {JoinableTopicListModule.class})
public interface JoinableTopicListComponent {
    void inject(JoinableTopicListActivity activity);
}
