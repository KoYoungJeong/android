package com.tosslab.jandi.app.ui.share.text.dagger;


import com.tosslab.jandi.app.ui.share.text.presenter.TextSharePresenter;
import com.tosslab.jandi.app.ui.share.text.presenter.TextSharePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class TextShareModule {
    private final TextSharePresenter.View view;

    public TextShareModule(TextSharePresenter.View view) {
        this.view = view;
    }

    @Provides
    TextSharePresenter presenter(TextSharePresenterImpl presenter) {
        return presenter;
    }

    @Provides
    TextSharePresenter.View view() {
        return view;
    }

}
