package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class UserEntitySimpleListAdapter extends BaseAdapter {
    private List<FormattedEntity> mUserList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public UserEntitySimpleListAdapter(Context context, List<FormattedEntity> userList) {
        this.mUserList = userList;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public FormattedEntity getItem(int i) {
        return mUserList.get(i);
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

        FormattedEntity user = getItem(i);

        // 프로필 사진
        Glide.with(this.mContext)
                .load(user.getUserSmallProfileUrl())
                .placeholder(R.drawable.jandi_icon_directmsg)
                .transform(new GlideCircleTransform(mContext))
                .into(holder.imageView);

        holder.textView.setText(user.getName());

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
