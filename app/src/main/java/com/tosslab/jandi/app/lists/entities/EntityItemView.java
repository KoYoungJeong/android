package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.CircleTransform;

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
    @ViewById(R.id.main_list_entities_real_layout)
    RelativeLayout linearLayoutReal;

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

    private Context mContext;

    public EntityItemView(Context context) {
        super(context);
        mContext = context;
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
                // 채널 아이콘의 색상이 자신의 ID에 따라 자동으로 변하도록...
                if (formattedEntity.isJoined) {
                    imageViewEntityIcon.setColorFilter(formattedEntity.getMyColor(),
                            android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    imageViewEntityIcon.clearColorFilter();
                    viewBlindForUnjoined.setVisibility(VISIBLE);
                }
                return;
            case FormattedEntity.TYPE_REAL_USER:
                linearLayoutReal.setVisibility(VISIBLE);
                Picasso.with(mContext)
                        .load(formattedEntity.getUserSmallProfileUrl())
                        .placeholder(R.drawable.jandi_profile)
                        .transform(new CircleTransform())
                        .into(imageViewEntityIcon);
                textViewChannelName.setText(formattedEntity.getUserName());
                textViewCntJoinedUsers.setText(formattedEntity.getUserEmail());
                if (formattedEntity.alarmCount > 0) {
                    textViewBadge.setVisibility(VISIBLE);
                    textViewBadge.setText(formattedEntity.alarmCount + "");
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
                textViewChannelTypeTitle.setText(R.string.jandi_entity_joined_channel);
                return;
            case FormattedEntity.TYPE_TITLE_UNJOINED_CHANNEL:
                linearLayoutChannelTitle.setVisibility(VISIBLE);
                textViewChannelTypeTitle.setText(R.string.jandi_entity_unjoined_channel);
                return;
        }
    }

    private void goneAllLayout() {
        linearLayoutReal.setVisibility(GONE);
        linearLayoutChannelTitle.setVisibility(GONE);
        viewBlindForUnjoined.setVisibility(GONE);
        textViewBadge.setVisibility(GONE);
    }
}
