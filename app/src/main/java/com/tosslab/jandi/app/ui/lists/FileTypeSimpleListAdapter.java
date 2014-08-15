package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.events.CategorizingAsFileType;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class FileTypeSimpleListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;

    public FileTypeSimpleListAdapter(Context context) {
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return CategorizingAsFileType.stringTitleList.length;
    }

    @Override
    public String getItem(int i) {
        return CategorizingAsFileType.stringTitleList[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_select_cdp, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_select_cdp_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(CategorizingAsFileType.resourceList[i]);
        holder.textView.setText(getItem(i));

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
