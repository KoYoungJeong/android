package com.tosslab.jandi.app.ui.fileexplorer.model;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;
import com.tosslab.jandi.app.utils.FormatConverter;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

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

    private Logger log = Logger.getLogger(FileExplorerModel.class);

    @RootContext
    Context context;

    public File getFile(String path) {

        if (TextUtils.isEmpty(path)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return new File(path);
    }

    public List<FileItem> fill(File f, String microSdCardPath) {

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

        if (!TextUtils.equals(f.getAbsolutePath(), (microSdCardPath == null || microSdCardPath.length() == 0) ? Environment.getExternalStorageDirectory().getAbsolutePath() : microSdCardPath)) {
            File parentFile = f.getParentFile();
            files.add(0, new FileItem("..", parentFile.getAbsolutePath(), parentFile.list().length, formater.format(parentFile.lastModified()), true));
        }

        return files;
    }

    private int getIconByFileType(String type) {
        if (type.startsWith("audio")) {
            return R.drawable.jandi_fview_icon_audio;
        } else if (type.startsWith("video")) {
            return R.drawable.jandi_fview_icon_video;
        } else if (type.startsWith("application/pdf")) {
            return R.drawable.jandi_fview_icon_pdf;
        } else if (type.startsWith("text")) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (TextUtils.equals(type, "application/x-hwp")) {
            return R.drawable.jandi_fl_icon_hwp;
        } else if (FormatConverter.isSpreadSheetMimeType(type)) {
            return R.drawable.jandi_fl_icon_exel;
        } else if (FormatConverter.isPresentationMimeType(type)) {
            return R.drawable.jandi_fview_icon_ppt;
        } else if (FormatConverter.isDocmentMimeType(type)) {
            return R.drawable.jandi_fview_icon_txt;
        } else {
            return R.drawable.jandi_fview_icon_etc;
        }
    }

    /*public List<FileItem> getStorageList() {

        List<FileItem> list = new ArrayList<FileItem>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_internal = !Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        BufferedReader buf_reader = null;
        try {
            HashSet<String> paths = new HashSet<String>();
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            int cur_display_number = 1;
            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path)) {
                        paths.add(def_path);
                        list.add(0, new FileItem(def_path, def_path, 0, def_path, true));
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new FileItem(mount_point, mount_point, 0, mount_point, true));
                        }
                    }
                }
            }

            if (!paths.contains(def_path) && def_path_available) {
                list.add(0, new FileItem(def_path, def_path, 0, "a", true));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                }
            }
        }
        return list;
    }*/
}
