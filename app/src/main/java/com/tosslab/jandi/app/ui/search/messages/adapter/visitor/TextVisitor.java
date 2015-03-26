package com.tosslab.jandi.app.ui.search.messages.adapter.visitor;

import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public interface TextVisitor {
    void visit(SpannableStringBuilder builder, ResMessageSearch.Record record);
}
