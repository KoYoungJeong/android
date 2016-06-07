package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by justinygchoi on 2014. 8. 14..
 */
public class UnjoinedUserListAdapter extends BaseAdapter {
    private final Context context;
    private List<User> listUserToBeJoined;
    private Map<Long, Boolean> checkedMap;

    public UnjoinedUserListAdapter(Context context) {
        this.listUserToBeJoined = new ArrayList<>();
        this.context = context;
        checkedMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return listUserToBeJoined.size();
    }

    public void setUnjoinedEntities(List<User> list) {
        this.listUserToBeJoined = list;
        notifyDataSetChanged();
    }

    @Override
    public User getItem(int i) {
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
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_check_user_icon);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_check_user);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User entity = getItem(i);
        // 프로필 사진
        Uri uri = Uri.parse(entity.getPhotoUrl());

        ImageUtil.loadProfileImage(holder.imageView, uri, R.drawable.profile_img);

        holder.textView.setText(entity.getName());
        holder.checkBox.setTag(entity);
        if (checkedMap.containsKey(entity.getId())) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        return convertView;
    }

    public List<Long> getSelectedUserIds() {
        List<Long> selectedUserIds = new ArrayList<>();

        Observable.from(listUserToBeJoined)
                .filter(user -> checkedMap.containsKey(user.getId()))
                .collect(() -> selectedUserIds, (longs, user1) -> longs.add(user1.getId()))
                .subscribe();

        return selectedUserIds;
    }

    public void toggleChecked(User user) {
        if (checkedMap.containsKey(user.getId())) {
            checkedMap.remove(user.getId());
        } else {
            checkedMap.put(user.getId(), true);
        }
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
        CheckBox checkBox;
    }
}
