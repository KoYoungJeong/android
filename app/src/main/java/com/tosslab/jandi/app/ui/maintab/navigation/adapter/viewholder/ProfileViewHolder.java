package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
@Deprecated
public class ProfileViewHolder extends BaseViewHolder<User> {

    @Bind(R.id.iv_navigation_profile_large)
    ImageView ivProfileLarge;
    @Bind(R.id.iv_navigation_profile)
    ImageView ivProfile;
    @Bind(R.id.tv_navigation_profile_name)
    TextView tvName;
    @Bind(R.id.tv_navigation_profile_email)
    TextView tvEmail;
    @Bind(R.id.v_navigation_owner_badge)
    View vOwnerBadge;

    private ProfileViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static ProfileViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_navigation_profile, parent, false);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindView(User user) {
        String photoUrl = user.getPhotoUrl();
        ImageUtil.loadProfileImage(ivProfile, photoUrl, R.drawable.profile_img);

        Resources resources = itemView.getResources();
        int defaultColor = resources.getColor(R.color.jandi_member_profile_img_overlay_default);
        Drawable placeHolder = new ColorDrawable(defaultColor);
        ImageLoader.newInstance()
                .placeHolder(placeHolder, ImageView.ScaleType.FIT_XY)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .transformation(new BlurTransformation(itemView.getContext(), 50))
                .uri(Uri.parse(ImageUtil.getLargeProfileUrl(photoUrl)))
                .into(ivProfileLarge);

        tvName.setText(user.getName());
        vOwnerBadge.setVisibility(user.isTeamOwner() ? View.VISIBLE : View.GONE);
        tvEmail.setText(user.getEmail());

    }
}
