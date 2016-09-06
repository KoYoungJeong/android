package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain;

import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.adapter.MemberRecentKeywordAdapter;

public class MemberRecentEmptyKeyword implements MemberSearchKeyword {
    @Override
    public int getType() {
        return MemberRecentKeywordAdapter.TYPE_EMPTY;
    }
}
