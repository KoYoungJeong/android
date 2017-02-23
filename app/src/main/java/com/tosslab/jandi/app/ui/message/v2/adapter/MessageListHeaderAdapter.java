package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.DateTransformator;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 3. 19..
 */
public class MessageListHeaderAdapter implements StickyHeadersAdapter<MessageListHeaderAdapter.HeaderViewHolder> {

    private final Context context;
    private final MessageItemDate originAdapter;
    private int savedHeight = -1;

    public MessageListHeaderAdapter(Context context, MessageItemDate originAdapter) {
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

        ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();

        if (headerId == -1) {
            params.height = 0;
            viewHolder.itemView.setLayoutParams(params);
            return;
        } else {
            if (savedHeight == -1) {
                savedHeight = params.height;
            }
            if (params.height == 0) {
                params.height = savedHeight;
                viewHolder.itemView.setLayoutParams(params);
            }
        }

        if (DateUtils.isToday(headerId)) {
            viewHolder.dateTextView.setText(R.string.today);
        } else {
            viewHolder.dateTextView.setText(DateTransformator.getTimeStringForDivider(headerId));
        }
    }

    @Override
    public long getHeaderId(int position) {

        Calendar instance = Calendar.getInstance();
        if (originAdapter.getItemDate(position) != null) {
            instance.setTime(originAdapter.getItemDate(position));
        } else {
            return -1;
        }

        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        return instance.getTimeInMillis();
    }

    interface MessageItemDate {
        Date getItemDate(int position);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.txt_message_date_devider);
        }
    }

}
