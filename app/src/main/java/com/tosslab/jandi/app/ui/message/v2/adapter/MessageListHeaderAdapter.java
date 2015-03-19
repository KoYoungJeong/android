package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.HeaderViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

import java.util.Calendar;

/**
 * Created by Steve SeongUg Jung on 15. 3. 19..
 */
public class MessageListHeaderAdapter implements StickyHeadersAdapter<HeaderViewHolder> {

    private final Context context;
    private final MessageListAdapter originAdapter;

    public MessageListHeaderAdapter(Context context, MessageListAdapter originAdapter) {
        this.context = context;
        this.originAdapter = originAdapter;
    }

    @Override
    public HeaderViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.item_message_header, viewGroup, false);

        return new HeaderViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(HeaderViewHolder viewHolder, int position) {

        long headerId = getHeaderId(position);

        if (DateUtils.isToday(headerId)) {
            viewHolder.dateTextView.setText(R.string.today);
        } else {
            viewHolder.dateTextView.setText(DateTransformator.getTimeStringForDivider(headerId));
        }
    }

    @Override
    public long getHeaderId(int position) {
        Calendar instance = Calendar.getInstance();
        if (originAdapter.getItem(position).time != null) {
            instance.setTime(originAdapter.getItem(position).time);
        }

        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        return instance.getTimeInMillis();
    }
}
