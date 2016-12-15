package com.tosslab.jandi.app.ui.invites.emails.adapter;

import android.view.View;

import com.tosslab.jandi.app.ui.invites.emails.adapter.binder.ItemViewBinder;

/**
 * Created by tee on 2016. 12. 12..
 */

public interface InviteEmailListAdapterViewModel {

    void removeItemView(View view);

    void setInviteCancelListener(ItemViewBinder.InviteCancelListener inviteCancelListener);

}
