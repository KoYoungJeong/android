package com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMensionEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.viewholder.MentionMemberListViewHolder;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 21..
 */
public class MentionMemberListAdapter extends RecyclerView.Adapter<MentionMemberListViewHolder> {

    private List<SearchedItemVO> searchedMembersList;

    public MentionMemberListAdapter(List<SearchedItemVO> searchedMembersList) {
        this.searchedMembersList = searchedMembersList;
    }

    public List<SearchedItemVO> getSearchedMembersList() {
        return searchedMembersList;
    }

    public void setSearchedMembersList(List<SearchedItemVO> searchedMembersList) {
        this.searchedMembersList = searchedMembersList;
        notifyDataSetChanged();
    }

    public SearchedItemVO getSearchedMemberByPosition(int position) {
        return searchedMembersList.get(position);
    }

    public void clearMembersList() {
        searchedMembersList.clear();
        notifyDataSetChanged();
    }

    @Override
    public MentionMemberListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MentionMemberListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_member_list, parent, false));
    }

    @Override
    public void onBindViewHolder(MentionMemberListViewHolder holder, int position) {
        if (searchedMembersList == null)
            return;
        SearchedItemVO item = searchedMembersList.get(position);

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
            SearchedItemVO selectedMember = getSearchedMemberByPosition(position);
            SelectedMemberInfoForMensionEvent event =
                    new SelectedMemberInfoForMensionEvent(selectedMember.getName(), selectedMember.getId(),
                            selectedMember.getType());
            EventBus.getDefault().post(event);
        });
    }

    @Override
    public int getItemCount() {
        return searchedMembersList.size();
    }

}
