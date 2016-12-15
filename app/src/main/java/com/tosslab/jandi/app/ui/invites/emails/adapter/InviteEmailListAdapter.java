package com.tosslab.jandi.app.ui.invites.emails.adapter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.ui.invites.emails.adapter.binder.ItemViewBinder;
import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 2016. 12. 9..
 */

public class InviteEmailListAdapter
        implements InviteEmailListAdapterDataModel, InviteEmailListAdapterViewModel {

    private LinearLayout vgListViewLayout;

    private List<InviteEmailVO> datas;

    private ItemViewBinder itemViewBinder;

    public InviteEmailListAdapter(LinearLayout listViewLayout, Context context) {
        this.vgListViewLayout = listViewLayout;
        this.datas = new ArrayList<>();
        this.itemViewBinder = new ItemViewBinder(context);
    }

    @Override
    public void setInviteCancelListener(ItemViewBinder.InviteCancelListener inviteCancelListener) {
        itemViewBinder.setInviteCancelListener(inviteCancelListener);
    }

    @Override
    public void addItem(InviteEmailVO item) {
        datas.add(0, item);
        View view = itemViewBinder.bindView(item);
        vgListViewLayout.addView(view, 0);
    }

    @Override
    public void removeItemView(View view) {
        int index = vgListViewLayout.indexOfChild(view);

        if (index >= 0) {
            datas.remove(index);
            vgListViewLayout.removeView(view);
        }
    }

    @Override
    public void removeAllItems() {
        datas.clear();
        vgListViewLayout.removeAllViews();
    }

    @Override
    public List<InviteEmailVO> getItems() {
        return datas;
    }

}