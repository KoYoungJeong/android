package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollDeletedInfoViewHolder extends BaseViewHolder<Poll> {

    private TextView tvDeleted;

    private PollDeletedInfoViewHolder(View itemView) {
        super(itemView);
        tvDeleted = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_deleted_date);
    }

    public static PollDeletedInfoViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_deleted_info, parent, false);
        return new PollDeletedInfoViewHolder(itemView);
    }

    @Override
    public void onBindView(Poll poll) {
        String time = DateTransformator.getTimeString(poll.getUpdatedAt());
        String deleted = tvDeleted.getResources().getString(R.string.jandi_file_deleted_with_date, time);
        tvDeleted.setText(deleted);
    }
}
