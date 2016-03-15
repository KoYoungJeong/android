package com.tosslab.jandi.app.ui.share.multi.dagger;

import com.tosslab.jandi.app.ui.share.multi.model.MultiShareModel;
import com.tosslab.jandi.app.ui.share.multi.model.SharesDataModel;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenter;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenterImpl;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class MultiShareModule {

    private MultiSharePresenter.View view;
    private SharesDataModel sharesDataModel;

    public MultiShareModule(MultiSharePresenter.View view) {
        this.view = view;
        sharesDataModel = new SharesDataModel(new ArrayList<>());
    }

    @Provides
    public MultiSharePresenter provideMultiSharePresenter() {
        return new MultiSharePresenterImpl(view, sharesDataModel);
    }

    @Provides
    public MultiShareModel provideMultiShareModel() {
        return new MultiShareModel();
    }

    @Provides
    public SharesDataModel provideSharesDataModel() {
        return sharesDataModel;
    }

}
