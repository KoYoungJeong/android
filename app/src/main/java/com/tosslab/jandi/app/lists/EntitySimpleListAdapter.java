package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.CircleTransform;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 18..
 */
public class EntitySimpleListAdapter extends BaseAdapter {
    private List<FormattedEntity> mFormattedEntities;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public EntitySimpleListAdapter(Context context, List<FormattedEntity> formattedEntities) {
        this.mFormattedEntities = formattedEntities;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mFormattedEntities.size();
    }

    @Override
    public Object getItem(int i) {
        return mFormattedEntities.get(i);
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

        FormattedEntity entity = (FormattedEntity)getItem(i);

        if (entity.isChannel()) {
            holder.textView.setText(entity.getChannel().name);
            holder.imageView.setImageResource(R.drawable.jandi_icon_channel);
        } else if (entity.isPrivateGroup()) {
            holder.textView.setText(entity.getPrivateGroup().name);
            holder.imageView.setImageResource(R.drawable.jandi_icon_privategroup);
        } else if (entity.isUser()) {
            // 프로필 사진
            Picasso.with(this.mContext)
                    .load(entity.getUserProfileUrl())
                    .placeholder(R.drawable.jandi_icon_directmsg)
                    .transform(new CircleTransform())
                    .into(holder.imageView);
            holder.textView.setText(entity.getUserName());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
