package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class UserEntitySimpleListAdapter extends BaseAdapter {
    private final List<FormattedEntity> mUserList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
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

        FormattedEntity user = getItem(position);

        // 프로필 사진
        Ion.with(holder.imageView)
                .placeholder(R.drawable.profile_img_comment)
                .error(R.drawable.profile_img_comment)
                .transform(new IonCircleTransform())
                .load(user.getUserSmallProfileUrl());

        if (position != 0) {
            holder.textView.setText(user.getName());
        } else {
            holder.textView.setText(R.string.jandi_my_files);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
