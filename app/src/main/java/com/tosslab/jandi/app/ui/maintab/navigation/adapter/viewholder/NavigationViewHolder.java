package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.intercom.android.sdk.Intercom;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class NavigationViewHolder extends BaseViewHolder<MenuItem> {

    @Bind(R.id.v_navigation_icon)
    View vIcon;

    @Bind(R.id.tv_navigation_title)
    TextView tvTitle;

    @Bind(R.id.tv_team_unread_badge)
    TextView tvUnread;

    private NavigationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static NavigationViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_navigation, parent, false);
        return new NavigationViewHolder(itemView);
    }

    @Override
    public void onBindView(final MenuItem menuItem) {
        Drawable icon = menuItem.getIcon();
        vIcon.setVisibility(icon != null ? View.VISIBLE : View.GONE);
        vIcon.setBackgroundDrawable(icon);

        if (menuItem.getItemId() == R.id.nav_1_on_1) {
            int unreadConversationCount = Intercom.client().getUnreadConversationCount();
            if (unreadConversationCount > 0) {
                tvUnread.setVisibility(View.VISIBLE);
                tvUnread.setText(String.valueOf(unreadConversationCount));
            } else {
                tvUnread.setVisibility(View.GONE);
            }
        } else {
            tvUnread.setVisibility(View.GONE);
        }

        tvTitle.setText(menuItem.getTitle());
    }

}
