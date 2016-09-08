package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 2016. 8. 30..
 */
public class VersionViewHolder extends BaseViewHolder<String> {

    @Bind(R.id.tv_name)
    TextView tvName;

    private VersionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static VersionViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_navigation_version, parent, false);
        return new VersionViewHolder(itemView);
    }

    @Override
    public void onBindView(String version) {
        tvName.setText(new StringBuilder("Ver ").append(version));
    }

}
