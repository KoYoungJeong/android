package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.R;

/**
 * Created by Bill Minwook Heo on 15. 4. 14..
 */
public class FileExtensionCheck {

    public static int fileExtensionCheck(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(pos + 1);

        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("bmp") || ext.equals("png")
                || ext.equals("tif")) {
            return R.drawable.jandi_fview_icon_etc;
        } else if (ext.equals("avi") || ext.equals("mpg") || ext.equals("mpeg") || ext.equals("wmv") || ext.equals("mp4")
                || ext.equals("mkv") || ext.equals("asf") || ext.equals("flv")) {
            return R.drawable.jandi_fview_icon_video;
        } else if (ext.equals("mp3") || ext.equals("wma") || ext.equals("wav") || ext.equals("mid")) {
            return R.drawable.jandi_fview_icon_audio;
        } else if (ext.equals("pdf")) {
            return R.drawable.jandi_fview_icon_pdf;
        } else if (ext.equals("txt")) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (ext.equals("hwp")) {
            return R.drawable.jandi_fl_icon_hwp;
        } else if (ext.equals("xls") || ext.equals("xlsx")) {
            return R.drawable.jandi_fl_icon_exel;
        } else if (ext.equals("doc") || ext.equals("dicx")) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (ext.equals("ppt") || ext.equals("pptx")) {
            return R.drawable.jandi_fview_icon_ppt;
        } else {
            return R.drawable.jandi_fview_icon_etc;
        }

    }
}
