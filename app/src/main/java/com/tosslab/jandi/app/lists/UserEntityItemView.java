package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
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

    public UserEntityItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResLeftSideMenu.User user) {
        Picasso.with(mContext)
                .load(JandiConstants.SERVICE_ROOT_URL + user.u_photoUrl)
                .placeholder(R.drawable.jandi_profile)
                .transform(new CircleTransform())
                .into(imageViewUserProfile);
        textViewUserName.setText(user.u_lastName + " " + user.u_firstName);
        textViewUserNick.setText(user.name);
    }
}
