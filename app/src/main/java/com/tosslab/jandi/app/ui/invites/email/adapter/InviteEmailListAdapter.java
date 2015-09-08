package com.tosslab.jandi.app.ui.invites.email.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class InviteEmailListAdapter extends BaseAdapter {

    private Context context;
    private List<EmailVO> inviteList;

    public InviteEmailListAdapter(Context context) {
        this.context = context;
        this.inviteList = new ArrayList<EmailVO>();
    }

    @Override
    public int getCount() {
        return inviteList.size();
    }

    @Override
    public EmailVO getItem(int position) {
        return inviteList.get(position);
    }

    public List<String> getEmailLists() {
        List<String> emailList = new ArrayList<String>();
        for (int i = inviteList.size() - 1; i >= 0; i--) {
            emailList.add(inviteList.get(i).getEmail());
        }
        return emailList;
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
            viewHolder.sendingProgress = (ProgressBar) convertView.findViewById(R.id.progress_invite_processing);
            viewHolder.successImageView = (ImageView) convertView.findViewById(R.id.img_invite_success);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EmailVO item = getItem(position);

        viewHolder.emailView.setText(item.getEmail());

        int success = item.getSuccess();
        if (success != 0) {
            viewHolder.successImageView.setVisibility(View.VISIBLE);
            viewHolder.sendingProgress.setVisibility(View.GONE);

            if (success == 1) {
                viewHolder.successImageView.setImageResource(R.drawable.icon_accept);
            } else {
                viewHolder.successImageView.setImageResource(R.drawable.alert_icon_warning);
            }

        } else {
            viewHolder.successImageView.setVisibility(View.GONE);
            viewHolder.sendingProgress.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public void add(int position, EmailVO emailText) {
        inviteList.add(position, emailText);
    }

    public void remove(int index) {
        inviteList.remove(index);
    }

    public void clear() {
        inviteList.clear();
    }

    public int add(EmailVO emailTO) {
        inviteList.add(emailTO);

        return inviteList.size() - 1;
    }

    private static class ViewHolder {
        TextView emailView;
        ProgressBar sendingProgress;
        ImageView successImageView;
    }
}
