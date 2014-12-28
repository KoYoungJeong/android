package com.tosslab.jandi.app.ui.invites.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.to.EmailTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class InviteListAdapter extends BaseAdapter {

    private Context context;
    private List<EmailTO> inviteList;
    private MenuStatus menuStatus = MenuStatus.ADD;

    public InviteListAdapter(Context context) {
        this.context = context;
        this.inviteList = new ArrayList<EmailTO>();
    }

    public MenuStatus getMenuStatus() {
        return menuStatus;
    }

    public void setMenuStatus(MenuStatus menuStatus) {
        this.menuStatus = menuStatus;
    }

    @Override
    public int getCount() {
        return inviteList.size();
    }

    @Override
    public EmailTO getItem(int position) {
        return inviteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_invite_list, parent, false);
            viewHolder.emailView = (TextView) convertView.findViewById(R.id.txt_invite_email);
            viewHolder.checkView = (ImageView) convertView.findViewById(R.id.img_invite_select);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EmailTO item = getItem(position);

        viewHolder.emailView.setText(item.getEmail());
        if (menuStatus == MenuStatus.DELETE) {
            viewHolder.checkView.setVisibility(View.VISIBLE);
            if (item.isSelected()) {
                viewHolder.checkView.setImageResource(R.drawable.btn_check_on_holo_light);
            } else {
                viewHolder.checkView.setImageResource(R.drawable.btn_check_off_holo_light);
            }
        } else {
            viewHolder.checkView.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void add(int position, EmailTO emailText) {
        inviteList.add(position, emailText);
    }

    public void remove(int index) {
        inviteList.remove(index);
    }

    public void clear() {
        inviteList.clear();
    }

    public enum MenuStatus {
        ADD, DELETE
    }

    private static class ViewHolder {
        TextView emailView;
        ImageView checkView;
    }
}
