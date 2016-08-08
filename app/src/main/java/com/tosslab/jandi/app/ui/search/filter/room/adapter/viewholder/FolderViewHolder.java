package com.tosslab.jandi.app.ui.search.filter.room.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

/**
 * Created by tonyjs on 2016. 7. 28..
 */
public class FolderViewHolder extends BaseViewHolder<Folder> {

    private TextView tvName;

    private FolderViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView) itemView.findViewById(R.id.tv_room_filter_folder_name);
    }

    public static FolderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_room_filter_folder, parent, false);
        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindView(Folder folder) {
        tvName.setText(folder.getName());
    }

}
