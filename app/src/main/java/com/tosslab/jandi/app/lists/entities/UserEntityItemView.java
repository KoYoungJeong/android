package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
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
@EViewGroup(R.layout.item_entity_user)
public class UserEntityItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(UserEntityItemView.class);
    private Context mContext;

    @ViewById(R.id.main_list_entities_user_profile)
    ImageView imageViewUserProfile;
    @ViewById(R.id.main_list_entities_user_name_text)
    TextView textViewUserName;
    @ViewById(R.id.main_list_entities_user_nick_text)
    TextView textViewUserNick;
    @ViewById(R.id.main_list_entities_user_badge)
    TextView textViewUserBadge;

    public UserEntityItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(FormattedEntity user) {
        Picasso.with(mContext)
                .load(user.getUserProfileUrl())
                .placeholder(R.drawable.jandi_profile)
                .transform(new CircleTransform())
                .into(imageViewUserProfile);
        textViewUserName.setText(user.getUserName());
        textViewUserNick.setText(user.getUser().name);
        if (user.alarmCount > 0) {
            textViewUserBadge.setVisibility(VISIBLE);
            textViewUserBadge.setText(user.alarmCount + "");
        } else {
            textViewUserBadge.setVisibility(GONE);
        }
    }
}
