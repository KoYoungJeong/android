package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EViewGroup(R.layout.item_entity)
public class EntityItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(EntityItemView.class);

    @ViewById(R.id.main_list_entitiy_title_layout)
    LinearLayout linearLayoutChannelTitle;
    @ViewById(R.id.main_list_entity_title_second_layout)
    LinearLayout linearLayoutChannelSecondTitle;
    @ViewById(R.id.main_list_entities_real_layout)
    LinearLayout linearLayoutReal;

    @ViewById(R.id.main_list_entities_icon)
    ImageView imageViewEntityIcon;
    @ViewById(R.id.main_list_entities_unjoined)
    View viewBlindForUnjoined;

    @ViewById(R.id.main_list_entities_title_text)
    TextView textViewChannelTypeTitle;
    @ViewById(R.id.main_list_entities_name_text)
    TextView textViewChannelName;
    @ViewById(R.id.main_list_entities_cnt_joined_users_text)
    TextView textViewCntJoinedUsers;

    @ViewById(R.id.main_list_entities_badge)
    TextView textViewBadge;

    public EntityItemView(Context context) {
        super(context);
    }

    public void bind(FormattedEntity formattedEntity) {
        goneAllLayout();

        switch (formattedEntity.type) {
            case FormattedEntity.TYPE_REAL_CHANNEL:
                ResLeftSideMenu.Channel channel = formattedEntity.getChannel();
                if (channel == null) return;
                linearLayoutReal.setVisibility(VISIBLE);
                textViewChannelName.setText(channel.name);
                textViewCntJoinedUsers.setText(channel.ch_members.size() + " Users");
                if (formattedEntity.alarmCount > 0) {
                    textViewBadge.setVisibility(VISIBLE);
                    textViewBadge.setText(formattedEntity.alarmCount + "");
                }
                if (!formattedEntity.isJoined) {
                    viewBlindForUnjoined.setVisibility(VISIBLE);
                }
                return;
            case FormattedEntity.TYPE_REAL_PRIVATE_GROUP:
                ResLeftSideMenu.PrivateGroup privateGroup = formattedEntity.getPrivateGroup();
                if (privateGroup == null) return;
                linearLayoutReal.setVisibility(VISIBLE);
                imageViewEntityIcon.setImageResource(R.drawable.jandi_icon_privategroup);
                textViewChannelName.setText(privateGroup.name);
                textViewCntJoinedUsers.setText(privateGroup.pg_members.size() + " Users");
                if (formattedEntity.alarmCount > 0) {
                    textViewBadge.setVisibility(VISIBLE);
                    textViewBadge.setText(formattedEntity.alarmCount + "");
                }
                return;
            case FormattedEntity.TYPE_TITLE_JOINED_CHANNEL:
                linearLayoutChannelTitle.setVisibility(VISIBLE);
                textViewChannelTypeTitle.setText("가입된 채널");
                return;
            case FormattedEntity.TYPE_TITLE_UNJOINED_CHANNEL:
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
        textViewBadge.setVisibility(GONE);
    }
}
