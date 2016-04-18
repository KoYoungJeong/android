package com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 24..
 */
public class EmptySearchedMemberViewHolder extends BaseViewHolder<String> {

    @Bind(R.id.tv_team_member_header_message)
    TextView tvMessage;

    private EmptySearchedMemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static EmptySearchedMemberViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_member_header, parent, false);
        return new EmptySearchedMemberViewHolder(itemView);
    }

    @Override
    public void onBindView(String query) {
        String message = itemView.getResources()
                .getString(R.string.jandi_has_no_searched_member, query);
        tvMessage.setText(Html.fromHtml(message));
    }
}
