package com.tosslab.jandi.app.ui.search.filter.member.adapter.view;

import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;

/**
 * Created by tonyjs on 16. 7. 25..
 */
public interface MemberFilterableDataView extends MemberSearchableDataView {

    void setOnAllMemberClickListener(OnAllMemberClickListener onAllMemberClickListener);

    interface OnAllMemberClickListener {
        void onAllMemberClick();
    }

}
