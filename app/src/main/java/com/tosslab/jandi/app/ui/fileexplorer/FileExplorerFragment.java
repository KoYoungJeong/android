package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
@EFragment(R.layout.fragment_file_explorer)
public class FileExplorerFragment extends Fragment {

    public static final String EXTERNAL_ROOT_PATH = "/micro_sdcard";
    public static final String DEVICE_ROOT_PATH = "/sdcard";
    @FragmentArg
    String currentPath;

    @Bean
    FileExplorerModel fileExplorerModel;

    @Bean
    FileExplorerPresenter fileExplorerPresenter;

    @ViewById(R.id.file_explorer_navigation_text)
    TextView filePath;

    @AfterViews
    void initView() {

        File file = fileExplorerModel.getFile(currentPath);

        filePath.setText(getReplaceFilePath(file));

        List<FileItem> fileItems = fileExplorerModel.getChildFiles(file);
        fileExplorerPresenter.setFiles(fileItems);
    }

    private String getReplaceFilePath(File file) {
        if (fileExplorerModel.isChildOfExternalSdcard(file)) {
            return file.getAbsolutePath().replaceFirst(fileExplorerModel.getExternalSdCardPath(), EXTERNAL_ROOT_PATH);
        } else {
            return file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), DEVICE_ROOT_PATH);
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        getActivity().finish();
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
