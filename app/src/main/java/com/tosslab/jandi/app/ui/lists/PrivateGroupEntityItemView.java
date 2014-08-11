package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.widget.ImageView;
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
public class PrivateGroupEntityItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(ChannelEntityItemView.class);

    @ViewById(R.id.main_list_entities_icon)
    ImageView imageViewEntityIcon;
    @ViewById(R.id.main_list_entities_name_text)
    TextView textViewPrivateGrouplName;
    @ViewById(R.id.main_list_entities_cnt_joined_users_text)
    TextView textViewCntJoinedUsers;

    public PrivateGroupEntityItemView(Context context) {
        super(context);
    }

    public void bind(ResLeftSideMenu.PrivateGroup privateGroup) {
        imageViewEntityIcon.setImageResource(R.drawable.jandi_icon_privategroup);
        textViewPrivateGrouplName.setText(privateGroup.name);
        textViewCntJoinedUsers.setText(privateGroup.pg_members.size() + " Users");
    }
}
