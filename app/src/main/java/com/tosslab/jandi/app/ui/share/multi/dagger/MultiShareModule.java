package com.tosslab.jandi.app.ui.share.multi.dagger;

import com.tosslab.jandi.app.ui.share.multi.adapter.ShareFragmentPageAdapter;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareListDataView;
import com.tosslab.jandi.app.ui.share.multi.model.ShareListDataModel;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenter;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class MultiShareModule {

    private MultiSharePresenter.View view;
    private ShareFragmentPageAdapter adapter;

    public MultiShareModule(MultiSharePresenter.View view, ShareFragmentPageAdapter adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    @Provides
    MultiSharePresenter provideMultiSharePresenter(MultiSharePresenterImpl multiSharePresenter) {
        return multiSharePresenter;
    }

    @Provides
    MultiSharePresenter.View provideViewOfMultiSharePresenter() {
        return view;
    }

    @Provides
    ShareListDataModel provideSharesDataModel() {
        return adapter;
    }

    @Provides
    ShareListDataView provideSharesDataView() {
        return adapter;
    }
}
