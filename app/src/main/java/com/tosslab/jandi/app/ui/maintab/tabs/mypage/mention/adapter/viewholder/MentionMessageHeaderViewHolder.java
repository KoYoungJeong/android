package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 2016. 9. 21..
 */
public class MentionMessageHeaderViewHolder extends BaseViewHolder<Date> {

    @Bind(R.id.tv_title)
    TextView tvTitle;

    private MentionMessageHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MentionMessageHeaderViewHolder newInstance(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mention_header, parent, false);
        return new MentionMessageHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(Date date) {
        String time = DateTransformator.getTimeStringForDivider(date.getTime());
        tvTitle.setText(time);
    }
}
