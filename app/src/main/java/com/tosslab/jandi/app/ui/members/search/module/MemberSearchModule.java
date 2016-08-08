package com.tosslab.jandi.app.ui.members.search.module;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.members.adapter.searchable.SearchableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.ui.members.search.model.MemberSearchModel;
import com.tosslab.jandi.app.ui.members.search.presenter.MemberSearchPresenter;
import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 4. 7..
 */
@Module
public class MemberSearchModule {

    private final SearchableMemberListAdapter searchableMemberListAdapter;
    private final MemberSearchPresenter.View view;

    public MemberSearchModule(MemberSearchPresenter.View view, SearchableMemberListAdapter adapter) {
        this.view = view;
        this.searchableMemberListAdapter = adapter;
    }

    @Provides
    public MemberSearchPresenter.View providesMemberSearchView() {
        return view;
    }

    @Provides
    public InputMethodManager providesInputMethodManager() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) JandiApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager;
    }

    @Provides
    public MemberSearchableDataModel providesMemberSearchableDataModel() {
        return searchableMemberListAdapter;
    }

    @Provides
    public MemberSearchableDataView providesMemberSearchableDataView() {
        return searchableMemberListAdapter;
    }

    @Provides
    public MemberSearchModel providesMemberSearchModel() {
        return new MemberSearchModel();
    }

}
