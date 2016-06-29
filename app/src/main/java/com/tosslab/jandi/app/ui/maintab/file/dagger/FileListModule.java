package com.tosslab.jandi.app.ui.maintab.file.dagger;

import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.file.presenter.FileListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.file.presenter.FileListPresenterV3;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 16. 6. 28..
 */

@Module(includes = ApiClientModule.class)
public class FileListModule {

    private FileListPresenterImpl.View view;
    private long searchedEntityId;

    public FileListModule(FileListPresenterImpl.View view, long searchedEntityId) {
        this.view = view;
        this.searchedEntityId = searchedEntityId;
    }

    @Provides
    FileListPresenterV3.View provideViewOfFileListPresenter() {
        return view;
    }

    @Provides
    FileListModel provideFileListModel(Lazy<FileApi> fileApi) {
        return new FileListModel(fileApi);
    }

    @Provides
    FileListPresenterV3 provideFileListPresenter(FileListModel model) {
        return new FileListPresenterImpl(searchedEntityId, model);
    }

}
