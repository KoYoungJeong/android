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
 * Created by justinygchoi on 2014. 8. 18..
 */
public class EntitySimpleListAdapter extends BaseAdapter {
    private final List<FormattedEntity> mFormattedEntities;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

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
    public FormattedEntity getItem(int i) {
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

        FormattedEntity entity = getItem(i);

        // dummy entity는 이름이 없다. 지정된 string resource id 만 가져옴.
        if (entity.isDummy()) {
            holder.textView.setText(entity.getDummyNameRes());
        } else {
            holder.textView.setText(entity.getName());
        }

        holder.imageView.clearColorFilter();
        // user 는 개별 프로필 사진이 존재하기에 별도로 가져온다.
        if (entity.isUser()) {
            // 프로필 사진
            Ion.with(holder.imageView)
                    .placeholder(R.drawable.jandi_profile_comment)
                    .error(R.drawable.jandi_profile_comment)
                    .transform(new IonCircleTransform())
                    .load(entity.getUserSmallProfileUrl());
        } else {
            holder.imageView.setImageResource(entity.getIconImageResId());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
