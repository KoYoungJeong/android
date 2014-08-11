package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EViewGroup(R.layout.item_entity)
public class ChannelEntityItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(ChannelEntityItemView.class);

    @ViewById(R.id.channel_name_text)
    TextView textViewChannelName;
    @ViewById(R.id.channel_cnt_joined_users_text)
    TextView textViewCntJoinedUsers;

    public ChannelEntityItemView(Context context) {
        super(context);
    }

    public void bind(ResLeftSideMenu.Channel channel) {
        textViewChannelName.setText(channel.name);
        textViewCntJoinedUsers.setText(channel.ch_members.size() + " Users");
    }
}
