package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 21..
 */
public class CdpSelectListAdapter extends BaseAdapter {
    private List<CdpItem> listSelectCdp;
    private LayoutInflater layoutInflater;
    private Context context;

    public CdpSelectListAdapter(Context context, List<CdpItem> listSelectCdp) {
        this.listSelectCdp = listSelectCdp;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listSelectCdp.size();
    }

    @Override
    public Object getItem(int i) {
        return listSelectCdp.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_check_user, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_select_cdp_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CdpItem item = (CdpItem)getItem(i);
        if (item.type == JandiConstants.TYPE_DIRECT_MESSAGE) {
            // 프로필 사진
            Picasso.with(this.context).load(item.getProfileUrl()).fit().into(holder.imageView);
        }
        holder.textView.setText(item.name);

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
