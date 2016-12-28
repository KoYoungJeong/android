package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dagger;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.MentionListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.model.MentionListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.view.MentionListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.model.MentionListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter.MentionListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter.MentionListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view.MentionListView;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public class MentionListModule {

    private final MentionListView view;
    private final MentionListAdapter mentionListAdapter;

    public MentionListModule(MentionListAdapter mentionListAdapter, MentionListView view) {
        this.mentionListAdapter = mentionListAdapter;
        this.view = view;
    }

    @Provides
    public MentionListModel providesMentionListModel(Lazy<MessageApi> messageApi) {
        return new MentionListModel(messageApi);
    }

    @Provides
    public MentionListDataModel providesMentionListDataModel() {
        return mentionListAdapter;
    }

    @Provides
    public MentionListDataView providesMentionListDataView() {
        return mentionListAdapter;
    }

    @Provides
    public MentionListView providesMentionListView() {
        return view;
    }

    @Provides
    public MentionListPresenter providesMentionListPresenter(MentionListPresenterImpl presenter) {
        return presenter;
    }

}
