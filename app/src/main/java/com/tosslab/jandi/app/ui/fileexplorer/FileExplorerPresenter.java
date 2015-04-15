package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.adapter.FileItemAdapter;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
@EBean
public class FileExplorerPresenter {

    @RootContext
    Activity activity;

    @ViewById(R.id.lv_file_explorer)
    ListView fileListView;

    FileItemAdapter fileItemAdapter;

    @AfterInject
    void initObject() {
        fileItemAdapter = new FileItemAdapter(activity);
    }

    @AfterViews
    void initViews() {
        fileListView.setAdapter(fileItemAdapter);
    }


    public void setFiles(List<FileItem> fileItems) {
        for (FileItem fileItem : fileItems) {
            fileItemAdapter.add(fileItem);
        }
        fileItemAdapter.notifyDataSetChanged();

    }

    public void addFileFragment(FileItem fileItem, String microSdCardPath) {
        FileExplorerFragment fragment = FileExplorerFragment_.builder()
                .currentPath(fileItem.getPath()).microSdCardPath(microSdCardPath)
                .build();

        activity.getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, fragment, fileItem.getPath())
                .addToBackStack(fileItem.getPath())
                .commit();
    }
}
