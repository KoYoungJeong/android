package com.tosslab.jandi.app.ui.invites.emails.adapter;

import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;

import java.util.List;

/**
 * Created by tee on 2016. 12. 12..
 */

public interface InviteEmailListAdapterDataModel {

    void addItem(InviteEmailVO item);

    void removeAllItems();

    List<InviteEmailVO> getItems();

}
