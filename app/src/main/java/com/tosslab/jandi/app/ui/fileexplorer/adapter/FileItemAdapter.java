package com.tosslab.jandi.app.ui.fileexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.to.FileItem;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;

import java.util.ArrayList;


/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
public class FileItemAdapter extends ArrayAdapter<FileItem> {

    public FileItemAdapter(Context context) {
        super(context, R.layout.item_file, new ArrayList<FileItem>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_file, parent, false);
        }

        final FileItem item = getItem(position);
        if (item != null) {
            TextView fileNameTextView = (TextView) convertView.findViewById(R.id.tv_file_explorer_item_name);
            TextView dateTextView = (TextView) convertView.findViewById(R.id.tv_file_explorer_item_date);

            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.img_file_explorer_icon);

            if (item.isDirectory()) {
                iconImageView.setImageResource(R.drawable.folder_icon_135);
            } else {
                iconImageView.setImageResource(
                        FileExtensionsUtil.getFileTypeImageResourceForFileExplorer(item.getName()));
            }

            fileNameTextView.setText(item.getName());

            dateTextView.setText(item.getModifiedDate());

        }

        return convertView;
    }
}
