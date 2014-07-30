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
import com.tosslab.jandi.app.utils.CircleTransform;

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
            convertView = layoutInflater.inflate(R.layout.item_select_cdp, null);
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
            Picasso.with(this.context)
                    .load(item.getProfileUrl())
                    .placeholder(R.drawable.jandi_icon_directmsg)
                    .transform(new CircleTransform())
                    .into(holder.imageView);
        } else if (item.type == JandiConstants.TYPE_PRIVATE_GROUP) {
            holder.imageView.setImageDrawable(this.context.getResources().getDrawable(R.drawable.jandi_icon_privategroup));
        } else {
            holder.imageView.setImageDrawable(this.context.getResources().getDrawable(R.drawable.jandi_icon_channel));
        }
        holder.textView.setText(item.name);

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
