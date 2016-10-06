package com.tosslab.jandi.app.ui.search.filter.member.module;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.SearchableMemberFilterAdapter;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.model.MemberFilterableDataModel;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.view.MemberFilterableDataView;
import com.tosslab.jandi.app.ui.search.filter.member.model.MemberSearchModel;
import com.tosslab.jandi.app.ui.search.filter.member.presenter.MemberFilterPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 4. 7..
 */
@Module
public class MemberFilterModule {

    private final SearchableMemberFilterAdapter searchableMemberFilterAdapter;
    private final MemberFilterPresenter.View view;

    public MemberFilterModule(MemberFilterPresenter.View view, SearchableMemberFilterAdapter adapter) {
        this.view = view;
        this.searchableMemberFilterAdapter = adapter;
    }

    @Provides
    public MemberFilterPresenter.View providesMemberFilterView() {
        return view;
    }

    @Provides
    public InputMethodManager providesInputMethodManager() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) JandiApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager;
    }

    @Provides
    public MemberFilterableDataModel providesMemberFilterableDataModel() {
        return searchableMemberFilterAdapter;
    }

    @Provides
    public MemberFilterableDataView providesMemberFilterableDataView() {
        return searchableMemberFilterAdapter;
    }

    @Provides
    public MemberSearchModel providesMemberSearchModel() {
        return new MemberSearchModel();
    }

}
