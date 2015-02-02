package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.TextUtils;

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

    @Bean
    FileExplorerModel fileExplorerModel;

    @Bean
    FileExplorerPresenter fileExplorerPresenter;
    private String path;

    @AfterViews
    void initView() {

        setHasOptionsMenu(true);

        path = currentPath;

        File file = fileExplorerModel.getFile(currentPath);
        String absolutePath = file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");
        setupActionbar(absolutePath);

        List<FileItem> fileItems = fileExplorerModel.fill(file);
        fileExplorerPresenter.setFiles(fileItems);
    }

    private void setupActionbar(String absolutePath) {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(absolutePath);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
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
        File file = fileExplorerModel.getFile(currentPath);
        String absolutePath = file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");
        setupActionbar(absolutePath);

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

                File file = fileExplorerModel.getFile(currentPath).getParentFile();
                String absolutePath = file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");
                setupActionbar(absolutePath);


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
