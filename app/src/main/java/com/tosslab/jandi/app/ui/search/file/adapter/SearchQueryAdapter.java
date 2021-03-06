package com.tosslab.jandi.app.ui.search.file.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class SearchQueryAdapter extends ArrayAdapter<String> {

    public SearchQueryAdapter(Context context) {
        super(context, R.layout.item_searched_old_query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_searched_old_query, parent, false);

            holder.textView = (TextView) convertView.findViewById(R.id.txt_searched_old_query);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(getItem(position));

        return convertView;
    }

    static class ViewHolder {
        TextView textView;

    }
}
