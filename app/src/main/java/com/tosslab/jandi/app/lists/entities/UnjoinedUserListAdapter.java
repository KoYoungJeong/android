package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.CircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 14..
 */
public class UnjoinedUserListAdapter extends BaseAdapter {
    private List<FormattedEntity> listUserToBeJoined;
    private LayoutInflater layoutInflater;
    private Context context;

    public UnjoinedUserListAdapter(Context context, List<FormattedEntity> users) {
        this.listUserToBeJoined = users;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listUserToBeJoined.size();
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
            convertView = layoutInflater.inflate(R.layout.item_check_user, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_check_user_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_check_user_icon);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_check_user);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    FormattedEntity item = (FormattedEntity)compoundButton.getTag();
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
            Picasso.with(this.context)
                    .load(entity.getUserSmallProfileUrl())
                    .placeholder(R.drawable.jandi_profile)
                    .transform(new CircleTransform())
                    .into(holder.imageView);
            holder.textView.setText(entity.getUserName());
            holder.checkBox.setTag(entity);
        }

        return convertView;
    }

    public List<Integer> getSelectedUserIds() {
        ArrayList<Integer> selectedUserIds = new ArrayList<Integer>();
        for (FormattedEntity selectedItem : listUserToBeJoined) {
            if (selectedItem.isSelectedToBeJoined) {
                selectedUserIds.add(selectedItem.getUser().id);
            }
        }
        return selectedUserIds;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
        CheckBox checkBox;
    }
}
