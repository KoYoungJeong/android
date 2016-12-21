package com.tosslab.jandi.app.ui.fileexplorer.model;

import android.os.Environment;
import android.text.TextUtils;

import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

public class FileExplorerModel {

    public static final String DEFAULT_STORAGE = "/storage";
    public static final String EMULATED_PATH_NAME = "emulated";

    public File getFile(String path) {

        if (TextUtils.isEmpty(path)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return new File(path);
    }

    public List<FileItem> getChildFiles(File f) {
        List<FileItem> files = new ArrayList<FileItem>();
        File[] originFiles = f.listFiles();
        if (originFiles == null || originFiles.length <= 0) {
            return files;
        }

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
                return StringCompareUtil.compare(lhs.getName(), rhs.getName());
            } else {
                return rightDirectory - leftDirectory;
            }

        });
        String externalSdCardPath = getExternalSdCardPath();
        String internalSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String currentPath = f.getAbsolutePath();
        if (!TextUtils.equals(currentPath, internalSdcardPath) && !TextUtils.equals(currentPath, externalSdCardPath)) {
            File parentFile = f.getParentFile();
            files.add(0, new FileItem("..", parentFile.getAbsolutePath(), parentFile.list().length, formater.format(parentFile.lastModified()), true));
        }
        return files;
    }

    public String getExternalSdCardPath() {
        File storageDir = new File(DEFAULT_STORAGE);
        File[] originFiles = storageDir.listFiles();
        if (originFiles == null || originFiles.length <= 0) {
            return null;
        }

        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String inStoragePath = absolutePath.substring(0, absolutePath.lastIndexOf("/") + 1);

        String microSdCardPath = null;

        for (int i = 0; i < originFiles.length; i++) {

            try {
                if (originFiles[i].canRead() && originFiles[i].isDirectory() && !TextUtils.equals(originFiles[i].getName(), EMULATED_PATH_NAME)
                        && !originFiles[i].getCanonicalPath().contains(inStoragePath)) {
                    microSdCardPath = originFiles[i].getAbsolutePath();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return microSdCardPath;
    }

    public boolean hasExternalSdCard() {
        try {
            return !TextUtils.isEmpty(getExternalSdCardPath());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isChildOfExternalSdcard(File file) {
        if (!hasExternalSdCard() || file == null) {
            return false;
        } else {
            String externalSdCardPath = getExternalSdCardPath();
            return file.getAbsolutePath().contains(externalSdCardPath);
        }
    }
}
