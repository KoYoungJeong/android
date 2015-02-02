package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
@EFragment(R.layout.fragment_file_explorer)
public class FileExplorerFragment extends Fragment {

    @FragmentArg
    String currentPath;

    @Bean
    FileExplorerModel fileExplorerModel;

    @Bean
    FileExplorerPresenter fileExplorerPresenter;

    @AfterViews
    void initView() {

        File file = fileExplorerModel.getFile(currentPath);

        String absolutePath = file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");

        getActivity().getActionBar().setTitle(absolutePath);
        List<FileItem> fileItems = fileExplorerModel.fill(file);
        fileExplorerPresenter.setFiles(fileItems);
    }

    @ItemClick(R.id.lv_file_explorer)
    void onFileItemClick(FileItem fileItem) {

        if (fileItem.isDirectory()) {

            if (!TextUtils.equals(fileItem.getName(), "..")) {

                fileExplorerPresenter.addFileFragment(fileItem);
            } else {
                getFragmentManager().popBackStack();
            }

        } else {
            Intent intent = new Intent();
            String path = new File(fileItem.getPath()).getParent();
            intent.putExtra("GetPath", path);
            intent.putExtra("GetFileName", fileItem.getName());
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

}