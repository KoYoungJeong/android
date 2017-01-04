package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main.object.SearchData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class RoomHeaderViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.cb_unjoin_topic)
    AppCompatCheckBox cbUnjoinTopic;

    private OnCheckChangeListener onCheckChangeListener;

    public RoomHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static RoomHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_room_header, parent, false);
        return new RoomHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        cbUnjoinTopic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onCheckChangeListener != null) {
                onCheckChangeListener.onCheckUnjoinTopic(isChecked);
            }
        });
    }

    public void setSetOnCheckChangeListener(OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }

    public void setGuest(boolean guest) {
        cbUnjoinTopic.setEnabled(!guest);
        if (guest) {
            itemView.setOnClickListener(null);
        } else {
            itemView.setOnClickListener(v -> cbUnjoinTopic.setChecked(!cbUnjoinTopic.isChecked()));
        }
    }

    public interface OnCheckChangeListener {
        void onCheckUnjoinTopic(boolean isChecked);
    }

}
