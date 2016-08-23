package com.tosslab.jandi.app.ui.search.file.dagger;

import com.tosslab.jandi.app.ui.search.file.presenter.FileSearchPresenter;
import com.tosslab.jandi.app.ui.search.file.presenter.FileSearchPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class FileSearchModule {

    private FileSearchPresenter.View view;

    public FileSearchModule(FileSearchPresenter.View view) {this.view = view;}

    @Provides
    FileSearchPresenter.View provideView() {
        return view;
    }


    @Provides
    FileSearchPresenter provideSearchPresenter(FileSearchPresenterImpl searchPresenter) {
        return searchPresenter;
    }

}
