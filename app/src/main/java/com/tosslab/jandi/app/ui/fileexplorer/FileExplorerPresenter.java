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
import org.apache.log4j.Logger;

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

    private Logger log = Logger.getLogger(FileExplorerFragment.class);

    public void setFiles(List<FileItem> fileItems) {
        for (FileItem fileItem : fileItems) {
            fileItemAdapter.add(fileItem);
        }
        fileItemAdapter.notifyDataSetChanged();
        log.info("adapter change : ");

    }

    public void addFileFragment(FileItem fileItem) {
        FileExplorerFragment fragment = FileExplorerFragment_.builder()
                .currentPath(fileItem.getPath())
                .build();

        log.info("addfile fragment : " + fileItem.getPath());

        activity.getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, fragment, fileItem.getPath())
                .addToBackStack(fileItem.getPath())
                .commit();

        log.info("addfile count : " + activity.getFragmentManager().getBackStackEntryCount());
    }
}
