package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.BackPressedEvent;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
@EFragment(R.layout.fragment_file_explorer)
public class FileExplorerFragment extends Fragment {

    @FragmentArg
    String currentPath;

    @FragmentArg
    String microSdCardPath;

    @Bean
    FileExplorerModel fileExplorerModel;

    @Bean
    FileExplorerPresenter fileExplorerPresenter;

    @ViewById(R.id.file_explorer_navigation_text)
    TextView filePath;

    private Logger log = Logger.getLogger(FileExplorerFragment.class);

    @AfterViews
    void initView() {

        setHasOptionsMenu(true);

        log.info("initView currentPath : " + currentPath);

        File file = fileExplorerModel.getFile(currentPath);

        filePath.setText(getReplaceFilePath(file));

        List<FileItem> fileItems = fileExplorerModel.fill(file, microSdCardPath);
        fileExplorerPresenter.setFiles(fileItems);
        log.info("initView!!");
    }


    private String getReplaceFilePath(File file) {
        if (microSdCardPath == null || microSdCardPath.length() == 0) {
            return file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");
        } else {
            return file.getAbsolutePath().replaceFirst(microSdCardPath, "/micro_sdcard");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(BackPressedEvent event) {
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        getActivity().finish();
    }


    @ItemClick(R.id.lv_file_explorer)
    void onFileItemClick(FileItem fileItem) {

        if (fileItem.isDirectory()) {
            if (!TextUtils.equals(fileItem.getName(), "..")) {
                fileExplorerPresenter.addFileFragment(fileItem, microSdCardPath);
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
