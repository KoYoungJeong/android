package com.tosslab.jandi.app.ui.share.multi.dagger;

import com.tosslab.jandi.app.ui.share.multi.model.SharesDataModel;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenter;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class MultiShareModule {

    private MultiSharePresenter.View view;
    private SharesDataModel sharesDataModel;

    public MultiShareModule(MultiSharePresenter.View view, SharesDataModel sharesDataModel) {
        this.view = view;
        this.sharesDataModel = sharesDataModel;
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
    SharesDataModel provideSharesDataModel() {
        return sharesDataModel;
    }

}
