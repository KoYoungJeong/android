package com.tosslab.jandi.app.lists.invitations;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.lists.messages.MessageItemView;
import com.tosslab.jandi.app.lists.messages.MessageItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
@EBean
public class InvitationListAdapter extends BaseAdapter {
    @RootContext
    Context mContext;

    private List<String> mEmailAddressForInvitations;

    @AfterInject
    void init() {
        mEmailAddressForInvitations = new ArrayList<String>();
    }

    public void addInvitaion(String email) {
        mEmailAddressForInvitations.add(email);
        notifyDataSetChanged();
    }

    public void removeInvitation(String email) {
        mEmailAddressForInvitations.remove(email);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEmailAddressForInvitations.size();
    }

    @Override
    public String getItem(int position) {
        return mEmailAddressForInvitations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InvitationView invitaionView;
        if (convertView == null) {
            invitaionView = InvitationView_.build(mContext);
        } else {
            invitaionView = (InvitationView) convertView;
        }

        invitaionView.bind(getItem(position));

        return invitaionView;
    }
}
