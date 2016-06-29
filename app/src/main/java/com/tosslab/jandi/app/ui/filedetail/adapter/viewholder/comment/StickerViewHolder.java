package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ProfileBinder;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class StickerViewHolder extends BaseViewHolder<ResMessages.CommentStickerMessage> {
    private TextView tvUserName;
    private ImageView ivUserProfile;
    private View vUserNameDisableIndicator;
    private View vUserProfileDisableIndicator;
    private TextView tvCreatedDate;
    private ImageView ivSticker;

    public StickerViewHolder(View itemView) {
        super(itemView);

        tvUserName = (TextView) itemView.findViewById(R.id.tv_file_detail_comment_sticker_user_name);
        ivUserProfile = (ImageView) itemView.findViewById(R.id.iv_file_detail_comment_sticker_user_profile);
        tvCreatedDate = (TextView) itemView.findViewById(R.id.tv_file_detail_comment_sticker_create_date);

        vUserNameDisableIndicator =
                itemView.findViewById(R.id.v_file_detail_comment_sticker_user_name_disable_indicator);
        vUserProfileDisableIndicator =
                itemView.findViewById(R.id.v_file_detail_comment_sticker_user_profile_disable_indicator);

        ivSticker = (ImageView) itemView.findViewById(R.id.iv_file_detail_comment_sticker_content);
    }

    public static StickerViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment_sticker, parent, false);
        return new StickerViewHolder(itemView);
    }

    @Override
    public void onBindView(ResMessages.CommentStickerMessage stickerMessage) {
        User writer = TeamInfoLoader.getInstance().getUser(stickerMessage.writerId);
        ProfileBinder.newInstance(tvUserName, vUserNameDisableIndicator,
                ivUserProfile, vUserProfileDisableIndicator)
                .bindForComment(writer);

        bindComment(stickerMessage);
    }

    public void bindComment(ResMessages.CommentStickerMessage stickerMessage) {
        // 날짜
        String createTime = DateTransformator.getTimeString(stickerMessage.createTime);
        tvCreatedDate.setText(createTime);

        // 댓글 내용
        ResMessages.StickerContent content = stickerMessage.content;
        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
    }
}
