package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 18..
 */
public class EntitySimpleListAdapter extends BaseAdapter {
    private final Context context;
    private List<FormattedEntity> mFormattedEntities;

    public EntitySimpleListAdapter(Context context, List<FormattedEntity> formattedEntities) {
        this.mFormattedEntities = formattedEntities;
        this.context = context;
    }

    public void setEntities(List<FormattedEntity> entities) {
        mFormattedEntities = entities;
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_cdp, viewGroup, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.img_select_cdp_icon);
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

        SimpleDraweeView imageView = holder.imageView;
        imageView.clearColorFilter();
        // user 는 개별 프로필 사진이 존재하기에 별도로 가져온다.
        if (entity.isUser()) {
            // 프로필 사진
            ImageUtil.loadCircleImageByFresco(imageView,
                    entity.getUserSmallProfileUrl(), R.drawable.profile_img_comment);
        } else {
            imageView.setImageURI(UriFactory.getResourceUri(entity.getIconImageResId()));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        SimpleDraweeView imageView;
    }
}
