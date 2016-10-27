package com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 21..
 */
public class MentionMemberListViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvJobTitle;
    private TextView tvDepartment;
    private ViewGroup vgContent;
    private View convertView;

    public MentionMemberListViewHolder(View itemView) {
        super(itemView);
        ivIcon = (ImageView) itemView.findViewById(R.id.iv_member_item_icon);
        tvName = (TextView) itemView.findViewById(R.id.tv_user_name);
        tvJobTitle = (TextView) itemView.findViewById(R.id.tv_job_title);
        tvDepartment = (TextView) itemView.findViewById(R.id.tv_user_department);
        vgContent = (ViewGroup) itemView.findViewById(R.id.vg_content);
        convertView = itemView;
    }

    public void bindView(SearchedItemVO itemVO) {
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();

        // 컨텐트 영역 = 전체 화면 - 외쪽 profileImage영역 - 오른쪽 마진
        int contentWidth = (int) (displayMetrics.widthPixels
                - UiUtils.getPixelFromDp(72) - UiUtils.getPixelFromDp(16));

        if (itemVO.getName().equals("all") && itemVO.getType().equals("room")) {
            ImageLoader.loadFromResources(ivIcon, R.drawable.thum_all_member);
            tvName.setText(R.string.jandi_all_of_topic_members);
            LinearLayout.LayoutParams userNameLP =
                    (LinearLayout.LayoutParams) tvName.getLayoutParams();
            userNameLP.width = contentWidth;
            tvName.setLayoutParams(userNameLP);
            tvJobTitle.setVisibility(View.GONE);
            tvDepartment.setVisibility(View.GONE);
        } else {

            if (!itemVO.isInactive()) {
                ImageUtil.loadProfileImage(ivIcon,
                        itemVO.getSmallProfileImageUrl(), R.drawable.profile_img);
            } else {
                ImageLoader.loadFromResources(ivIcon, R.drawable.profile_img_dummyaccount_43);
            }

            tvName.setText(itemVO.getName());

            if (!TextUtils.isEmpty(itemVO.getJobTitle())) {
                int maxUserNameWidth = (int) (contentWidth * 0.7);
                Paint userNamePaint = tvName.getPaint();
                int nameWidth = (int) userNamePaint.measureText(tvName.getText().toString());

                LinearLayout.LayoutParams userNameLP =
                        (LinearLayout.LayoutParams) tvName.getLayoutParams();

                if (nameWidth > maxUserNameWidth) {
                    userNameLP.width = maxUserNameWidth;
                } else {
                    userNameLP.width = nameWidth;
                }

                tvName.setLayoutParams(userNameLP);
                tvJobTitle.setVisibility(View.VISIBLE);
                tvJobTitle.setText(itemVO.getJobTitle());
            } else {
                tvJobTitle.setVisibility(View.GONE);
                LinearLayout.LayoutParams userNameLP =
                        (LinearLayout.LayoutParams) tvName.getLayoutParams();
                userNameLP.width = contentWidth;
                tvName.setLayoutParams(userNameLP);
            }

            if (!TextUtils.isEmpty(itemVO.getDepartment())) {
                tvDepartment.setVisibility(View.VISIBLE);
                tvDepartment.setText(itemVO.getDepartment());
            } else {
                tvDepartment.setVisibility(View.GONE);
            }
        }

        convertView.setOnClickListener(v -> {
            SelectedMemberInfoForMentionEvent event =
                    new SelectedMemberInfoForMentionEvent(itemVO.getName(), itemVO.getId(),
                            itemVO.getType());
            EventBus.getDefault().post(event);
        });
    }

}
