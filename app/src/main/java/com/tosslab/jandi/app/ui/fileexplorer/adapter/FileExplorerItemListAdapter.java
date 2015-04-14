package com.tosslab.jandi.app.ui.fileexplorer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.files.FileExplorerItem;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 23..
 * File 탐색기를 위한...
 */
public class FileExplorerItemListAdapter extends ArrayAdapter<FileExplorerItem> {
    private Context c;
    private int id;
    private List<FileExplorerItem> items;

    public FileExplorerItemListAdapter(Context context, int textViewResourceId, List<FileExplorerItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public FileExplorerItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

        final FileExplorerItem o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.tv_file_explorer_item_name);
            //TextView t2 = (TextView) v.findViewById(R.id.tv_file_explorer_item_childcount);
            TextView t3 = (TextView) v.findViewById(R.id.tv_file_explorer_item_date);
                       /* Take the ImageView from layout and set the city's image */
            ImageView imageCity = (ImageView) v.findViewById(R.id.img_file_explorer_icon);
            String uri = "drawable/" + o.getImage();
            int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
            Drawable image = c.getResources().getDrawable(imageResource);
            imageCity.setImageDrawable(image);

            if (t1 != null) {
                t1.setText(o.getName());
            } else if (t3 != null) {
                t3.setText(o.getDate());
            }

        }
        return v;
    }
}
