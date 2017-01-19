package com.tosslab.jandi.app.ui.members.view;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public interface MemberSearchableDataView {

    void setOnMemberClickListener(OnMemberClickListener onMemberClickListener);

    void notifyDataSetChanged();

    interface OnMemberClickListener {
        void onMemberClick(ChatChooseItem member);
    }
}
