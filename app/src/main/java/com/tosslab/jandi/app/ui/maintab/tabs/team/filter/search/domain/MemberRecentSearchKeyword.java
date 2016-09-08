package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain;

import com.tosslab.jandi.app.local.orm.domain.MemberRecentKeyword;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.adapter.MemberRecentKeywordAdapter;

public class MemberRecentSearchKeyword implements MemberSearchKeyword {

    private final MemberRecentKeyword raw;

    public MemberRecentSearchKeyword(MemberRecentKeyword raw) {
        this.raw = raw;
    }

    public long getId() {
        return raw.get_id();
    }

    public String getKeyword() {
        return raw.getKeyword();
    }

    @Override
    public int getType() {
        return MemberRecentKeywordAdapter.TYPE_DEFAULT;
    }
}
