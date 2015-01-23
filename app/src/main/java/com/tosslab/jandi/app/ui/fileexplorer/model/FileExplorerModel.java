package com.tosslab.jandi.app.ui.fileexplorer.model;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
@EBean
public class FileExplorerModel {

    @RootContext
    Context context;

    public File getFile(String path) {

        if (TextUtils.isEmpty(path)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return new File(path);
    }

    public List<FileItem> fill(File f) {

        File[] originFiles = f.listFiles();

        List<FileItem> files = new ArrayList<FileItem>();

        DateFormat formater = DateFormat.getDateTimeInstance();
        Iterator<FileItem> iterator = Observable.from(originFiles)
                .filter(file -> !file.getName().startsWith("."))
                .map(file -> new FileItem(file.getName(), file.getAbsolutePath(), file.list() != null ? file.list().length : 0, formater.format(file.lastModified()), file.isDirectory()))
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            files.add(iterator.next());
        }

        Collections.sort(files, (lhs, rhs) -> {

            int leftDirectory = lhs.isDirectory() ? 1 : 0;
            int rightDirectory = rhs.isDirectory() ? 1 : 0;

            if (leftDirectory - rightDirectory == 0) {

                return lhs.getName().compareToIgnoreCase(rhs.getName());
            } else {
                return rightDirectory - leftDirectory;
            }

        });

        if (!TextUtils.equals(f.getAbsolutePath(), Environment.getExternalStorageDirectory().getAbsolutePath())) {
            File parentFile = f.getParentFile();
            files.add(0, new FileItem("..", parentFile.getAbsolutePath(), parentFile.list().length, formater.format(parentFile.lastModified()), true));
        }

        return files;
    }
}
