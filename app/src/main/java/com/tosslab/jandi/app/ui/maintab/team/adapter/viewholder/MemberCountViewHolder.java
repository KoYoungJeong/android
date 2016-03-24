package com.tosslab.jandi.app.ui.maintab.team.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class MemberCountViewHolder extends BaseViewHolder<Integer> {

    @Bind(R.id.tv_team_member_header_message)
    TextView tvMessage;

    public static MemberCountViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_member_header, parent, false);
        return new MemberCountViewHolder(itemView);
    }

    public MemberCountViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBindView(Integer count) {
        String memberCount = itemView.getResources()
                .getString(R.string.jandi_team_member_count_with_format, count);
        tvMessage.setText(memberCount);
    }
}
