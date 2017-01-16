package com.tosslab.jandi.app.ui.share.multi.dagger;

import com.tosslab.jandi.app.ui.share.multi.adapter.ShareAdapterDataView;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareFragmentPageAdapter;
import com.tosslab.jandi.app.ui.share.multi.interaction.FileShareInteractor;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;
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
    FileShareInteractor fileShareInteractor() {
        return adapter;
    }

    @Provides
    MultiSharePresenter.View provideViewOfMultiSharePresenter() {
        return view;
    }

    @Provides
    ShareAdapterDataModel provideSharesDataModel() {
        return adapter;
    }

    @Provides
    ShareAdapterDataView provideSharesDataView() {
        return adapter;
    }
}
