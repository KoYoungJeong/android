package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 14..
 */
public class UnjoinedUserListAdapter extends BaseAdapter {
    private final Context context;
    private List<FormattedEntity> listUserToBeJoined;

    public UnjoinedUserListAdapter(Context context) {
        this.listUserToBeJoined = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return listUserToBeJoined.size();
    }

    public void setUnjoinedEntities(List<FormattedEntity> list) {
        this.listUserToBeJoined = list;
        notifyDataSetChanged();
    }

    @Override
    public FormattedEntity getItem(int i) {
        return listUserToBeJoined.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_check_user, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_check_user_name);
            holder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.img_check_user_icon);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_check_user);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    FormattedEntity item = (FormattedEntity) compoundButton.getTag();
                    item.isSelectedToBeJoined = b;
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormattedEntity entity = getItem(i);
        if (entity.isUser()) {
            // 프로필 사진
            Uri uri = Uri.parse(entity.getUserSmallProfileUrl());

            ImageUtil.loadCircleImageByFresco(holder.imageView, uri, R.drawable.profile_img);

            holder.textView.setText(entity.getName());
            holder.checkBox.setTag(entity);
            if (entity.isSelectedToBeJoined) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        }

        return convertView;
    }

    public List<Integer> getSelectedUserIds() {
        List<Integer> selectedUserIds = new ArrayList<Integer>();
        for (FormattedEntity selectedItem : listUserToBeJoined) {
            if (selectedItem.isSelectedToBeJoined) {
                selectedUserIds.add(selectedItem.getUser().id);
            }
        }
        return selectedUserIds;
    }

    static class ViewHolder {
        TextView textView;
        SimpleDraweeView imageView;
        CheckBox checkBox;
    }
}
