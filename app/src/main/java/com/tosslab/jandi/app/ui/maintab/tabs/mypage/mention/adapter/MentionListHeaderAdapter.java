package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter;

import android.view.ViewGroup;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.model.MentionListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.viewholder.MentionMessageHeaderViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

import java.util.Calendar;

/**
 * Created by tonyjs on 2016. 9. 22..
 */
public class MentionListHeaderAdapter implements StickyHeadersAdapter<MentionMessageHeaderViewHolder> {

    private final MentionListDataModel model;

    public MentionListHeaderAdapter(MentionListDataModel model) {
        this.model = model;
    }

    @Override
    public MentionMessageHeaderViewHolder onCreateViewHolder(ViewGroup parent) {
        return MentionMessageHeaderViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(MentionMessageHeaderViewHolder mentionMessageHeaderViewHolder, int position) {
        MentionMessage message = model.getItem(position);
        if (message == null) {
            return;
        }

        mentionMessageHeaderViewHolder.onBindView(message.getCreatedAt());
    }

    @Override
    public long getHeaderId(int position) {
        MentionMessage message = model.getItem(position);
        if (message == null) {
            return 0;
        }
        Calendar calendar =
                Calendar.getInstance(JandiApplication.getContext().getResources().getConfiguration().locale);
        calendar.setTime(message.getCreatedAt());
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);
        return year * 1000 + month * 100 + date * 10;
    }
}