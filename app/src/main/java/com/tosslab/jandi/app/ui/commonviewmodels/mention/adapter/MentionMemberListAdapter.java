package com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMensionEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.viewholder.MentionMemberListViewHolder;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 21..
 */
public class MentionMemberListAdapter extends ArrayAdapter<SearchedItemVO> {

    public MentionMemberListAdapter(Context context, List<SearchedItemVO> searchedMembersList) {
        super(context, R.layout.item_search_member_list, searchedMembersList);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return "";
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    public void setSearchedMembersList(List<SearchedItemVO> searchedMembersList) {
        clear();
        addAll(searchedMembersList);
        notifyDataSetChanged();
    }

    public MentionMemberListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MentionMemberListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_member_list, parent, false));
    }

    public void onBindViewHolder(MentionMemberListViewHolder holder, int position) {
        SearchedItemVO item = getItem(position);

        if (item.getName().equals("All") && item.getType().equals("room")) {
            Ion.with(holder.getIvIcon())
                    .placeholder(R.drawable.thum_all_member)
                    .transform(new IonCircleTransform())
                    .load(null);

            holder.getTvName().setText(item.getName() + " (of topic member)");
        } else {
            Ion.with(holder.getIvIcon())
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .transform(new IonCircleTransform())
                    .load(item.getSmallProfileImageUrl());
            holder.getTvName().setText(item.getName());
        }
        holder.getConvertView().setOnClickListener(v -> {
            SearchedItemVO selectedMember = getItem(position);
            SelectedMemberInfoForMensionEvent event =
                    new SelectedMemberInfoForMensionEvent(selectedMember.getName(), selectedMember.getId(),
                            selectedMember.getType());
            EventBus.getDefault().post(event);
        });
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MentionMemberListViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = onCreateViewHolder(parent, getItemViewType(position));
            convertView = viewHolder.itemView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MentionMemberListViewHolder) convertView.getTag();
            convertView = viewHolder.itemView;
        }

        onBindViewHolder(viewHolder, position);

        return convertView;
    }
}
