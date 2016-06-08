package com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class MemberViewHolder extends BaseViewHolder<FormattedEntity> {

    @Bind(R.id.tv_team_member_name)
    TextView tvName;
    @Bind(R.id.tv_team_member_status)
    TextView tvStatus;
    @Bind(R.id.iv_team_member_profile)
    ImageView ivProfile;

    private MemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MemberViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_member, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindView(FormattedEntity user) {
        boolean inavtived = user.isInavtived();
        if (!inavtived) {
            tvName.setText(user.getName());
        } else {
            tvName.setText(user.getUserEmail());
        }

        String userStatusMessage = user.getUserStatusMessage();
        tvStatus.setVisibility(TextUtils.isEmpty(userStatusMessage) ? View.GONE : View.VISIBLE);
        tvStatus.setText(userStatusMessage);

        ViewGroup.LayoutParams ivProfileLayoutParams = ivProfile.getLayoutParams();
        if (user instanceof BotEntity) {
            ivProfileLayoutParams.height = (int) UiUtils.getPixelFromDp(54f);
            ivProfile.setLayoutParams(ivProfileLayoutParams);
            ImageLoader.loadFromResources(ivProfile, R.drawable.bot_43x54);
        } else {
            ivProfileLayoutParams.height = (int) UiUtils.getPixelFromDp(43f);
            ivProfile.setLayoutParams(ivProfileLayoutParams);
            if (!inavtived) {
                ImageUtil.loadProfileImage(
                        ivProfile, user.getUserLargeProfileUrl(), R.drawable.profile_img);
            } else {
                ImageLoader.loadFromResources(ivProfile, R.drawable.profile_img_dummyaccount_43);
            }
        }
    }

}
