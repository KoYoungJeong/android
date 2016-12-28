package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.adapter.FileItemAdapter;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class FileExplorerFragment extends Fragment {

    public static final String EXTERNAL_ROOT_PATH = "/micro_sdcard";
    public static final String DEVICE_ROOT_PATH = "/sdcard";

    @Nullable
    @InjectExtra
    String currentPath;

    FileExplorerModel fileExplorerModel;

    @Bind(R.id.file_explorer_navigation_text)
    TextView filePath;

    @Bind(R.id.lv_file_explorer)
    ListView fileListView;

    FileItemAdapter fileItemAdapter;


    public static FileExplorerFragment create(String currentPath) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle args = new Bundle();
        args.putString("currentPath", currentPath);
        fragment.setArguments(args);
        return fragment;
    }

    public static FileExplorerFragment create() {
        return new FileExplorerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }
        initView();
    }

    void initView() {
        fileItemAdapter = new FileItemAdapter(getActivity());
        fileListView.setAdapter(fileItemAdapter);

        fileExplorerModel = new FileExplorerModel();
        File file = fileExplorerModel.getFile(currentPath);

        filePath.setText(getReplaceFilePath(file));

        List<FileItem> fileItems = fileExplorerModel.getChildFiles(file);
        for (FileItem fileItem : fileItems) {
            fileItemAdapter.add(fileItem);
        }

        fileItemAdapter.notifyDataSetChanged();
    }

    private String getReplaceFilePath(File file) {
        if (fileExplorerModel.isChildOfExternalSdcard(file)) {
            return file.getAbsolutePath().replaceFirst(fileExplorerModel.getExternalSdCardPath(), EXTERNAL_ROOT_PATH);
        } else {
            return file.getAbsolutePath().replaceFirst(Environment.getExternalStorageDirectory().getAbsolutePath(), DEVICE_ROOT_PATH);
        }
    }

    @OnItemClick(R.id.lv_file_explorer)
    void onFileItemClick(int position) {
        FileItem fileItem = fileItemAdapter.getItem(position);

        if (fileItem.isDirectory()) {
            if (!TextUtils.equals(fileItem.getName(), "..")) {
                addFileFragment(fileItem);
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

    public void addFileFragment(FileItem fileItem) {
        FileExplorerFragment fragment = FileExplorerFragment.create(fileItem.getPath());

        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, fragment, fileItem.getPath())
                .addToBackStack(fileItem.getPath())
                .commit();

    }
}
