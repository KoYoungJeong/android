package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.models.FormattedChannel;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EViewGroup(R.layout.item_entity)
public class ChannelEntityItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(ChannelEntityItemView.class);

    @ViewById(R.id.main_list_entitiy_title_layout)
    LinearLayout linearLayoutChannelTitle;
    @ViewById(R.id.main_list_entity_title_second_layout)
    LinearLayout linearLayoutChannelSecondTitle;
    @ViewById(R.id.main_list_entities_real_layout)
    LinearLayout linearLayoutReal;

    @ViewById(R.id.main_list_entities_unjoined)
    View viewBlindForUnjoined;

    @ViewById(R.id.main_list_entities_title_text)
    TextView textViewChannelTypeTitle;
    @ViewById(R.id.main_list_entities_name_text)
    TextView textViewChannelName;
    @ViewById(R.id.main_list_entities_cnt_joined_users_text)
    TextView textViewCntJoinedUsers;

    public ChannelEntityItemView(Context context) {
        super(context);
    }

    public void bind(FormattedChannel formattedChannel) {
        goneAllLayout();

        switch (formattedChannel.type) {
            case FormattedChannel.TYPE_REAL_CHANNEL:
                linearLayoutReal.setVisibility(VISIBLE);
                textViewChannelName.setText(formattedChannel.original.name);
                textViewCntJoinedUsers.setText(formattedChannel.original.ch_members.size() + " Users");
                if (!formattedChannel.isJoined) {
                    viewBlindForUnjoined.setVisibility(VISIBLE);
                }
                return;
            case FormattedChannel.TYPE_TITLE_JOINED:
                linearLayoutChannelTitle.setVisibility(VISIBLE);
                textViewChannelTypeTitle.setText("가입된 채널");
                return;
            case FormattedChannel.TYPE_TITLE_UNJOINED:
                linearLayoutChannelTitle.setVisibility(VISIBLE);
                linearLayoutChannelSecondTitle.setVisibility(VISIBLE);
                textViewChannelTypeTitle.setText("미가입 채널");
                return;
        }
    }

    private void goneAllLayout() {
        linearLayoutReal.setVisibility(GONE);
        linearLayoutChannelTitle.setVisibility(GONE);
        linearLayoutChannelSecondTitle.setVisibility(GONE);
        viewBlindForUnjoined.setVisibility(GONE);
    }
}
