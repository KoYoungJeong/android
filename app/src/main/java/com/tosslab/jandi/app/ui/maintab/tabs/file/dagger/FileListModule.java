package com.tosslab.jandi.app.ui.maintab.tabs.file.dagger;

import com.tosslab.jandi.app.ui.maintab.tabs.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.file.presenter.FileListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.file.presenter.FileListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class FileListModule {

    private final boolean inSearchActivity;
    private FileListPresenterImpl.View view;
    private long searchedEntityId;

    public FileListModule(FileListPresenterImpl.View view, long searchedEntityId, boolean inSearchActivity) {
        this.view = view;
        this.searchedEntityId = searchedEntityId;
        this.inSearchActivity = inSearchActivity;
    }

    @Provides
    FileListPresenter.View provideViewOfFileListPresenter() {
        return view;
    }

    @Provides
    FileListPresenter provideFileListPresenter(FileListModel model) {
        return new FileListPresenterImpl(searchedEntityId, model, view, inSearchActivity);
    }

}
