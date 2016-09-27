package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main.object.SearchData;
import com.tosslab.jandi.app.ui.search.main.object.SearchMessageData;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class MessageItemViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.iv_profile)
    ImageView ivProfile;

    @Bind(R.id.tv_time)
    TextView tvTime;

    @Bind(R.id.tv_user_name)
    TextView tvUserName;

    @Bind(R.id.tv_message_content)
    TextView tvMessageContent;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.tv_divide_bar)
    TextView tvDivideBar;

    @Bind(R.id.iv_shared_item_icon)
    ImageView ivSharedItemIcon;

    @Bind(R.id.tv_shared_item_title)
    TextView tvSharedItemTitle;

    @Bind(R.id.v_full_divider)
    View vFullDivider;

    @Bind(R.id.v_half_divider)
    View vHalfDivider;

    private OnClickMessageListener onClickMessageListener;

    public MessageItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MessageItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_message_item, parent, false);
        return new MessageItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        SearchMessageData searchMessageData = (SearchMessageData) searchData;
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        if (TeamInfoLoader.getInstance().isBot(searchMessageData.getWriterId())) {
            WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(searchMessageData.getWriterId());
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new JandiProfileTransform(ivProfile.getContext(),
                            TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                            TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                            Color.WHITE))
                    .uri(Uri.parse(webhookBot.getPhotoUrl()))
                    .into(ivProfile);
        } else if (TeamInfoLoader.getInstance().isJandiBot(searchMessageData.getWriterId())) {
            ivProfile.setImageResource(R.drawable.logotype_80);
        } else {
            User writer = teamInfoLoader.getUser(searchMessageData.getWriterId());
            if (writer != null) {
                String photoUrl =
                        teamInfoLoader.getUser(searchMessageData.getWriterId()).getPhotoUrl();
                ImageUtil.loadProfileImage(ivProfile, photoUrl, R.drawable.profile_img);
            }
        }

        tvTime.setText(DateTransformator.getTimeString(searchMessageData.getCreatedAt()));

        tvUserName.setText(teamInfoLoader.getMemberName(searchMessageData.getWriterId()));

        long roomId = searchMessageData.getRoomId();

        if (teamInfoLoader.isChat(roomId)) {
            String companionName =
                    teamInfoLoader.getMemberName(teamInfoLoader.getChat(roomId).getCompanionId());
            tvRoomName.setText(companionName);
        } else if (teamInfoLoader.isTopic(roomId)) {
            tvRoomName.setText(teamInfoLoader.getTopic(roomId).getName());
        }

        String feedbackType = searchMessageData.getFeedbackType();

        if (TextUtils.isEmpty(feedbackType)) {
            tvDivideBar.setVisibility(View.GONE);
            ivSharedItemIcon.setVisibility(View.GONE);
            tvSharedItemTitle.setVisibility(View.GONE);
        } else {
            tvDivideBar.setVisibility(View.VISIBLE);
            ivSharedItemIcon.setVisibility(View.VISIBLE);
            tvSharedItemTitle.setVisibility(View.VISIBLE);
            if (feedbackType.equals("file") && searchMessageData.getFile() != null) {
                ivSharedItemIcon.setImageDrawable(JandiApplication.getContext().getResources()
                        .getDrawable(R.drawable.account_icon_upload));
                tvSharedItemTitle.setText(searchMessageData.getFile().getTitle());
            } else if (feedbackType.equals("poll") && searchMessageData.getPoll() != null) {
                ivSharedItemIcon.setImageDrawable(JandiApplication.getContext().getResources()
                        .getDrawable(R.drawable.account_icon_poll));
                tvSharedItemTitle.setText(searchMessageData.getPoll().getSubject());
            }
        }

        if (searchMessageData.hasHalfLine()) {
            vFullDivider.setVisibility(View.GONE);
            vHalfDivider.setVisibility(View.VISIBLE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vHalfDivider.setVisibility(View.GONE);
        }

        mesureRoomInfoArea();

        setMentionAndHighlight(searchMessageData);

        if (onClickMessageListener != null) {
            itemView.setOnClickListener(v -> onClickMessageListener.onClickMessage(searchMessageData));
        }
    }

    private void setMentionAndHighlight(SearchMessageData searchMessageData) {
        long myId = TeamInfoLoader.getInstance().getMyId();

        SpannableStringBuilder ssb = new SpannableStringBuilder(searchMessageData.getText());

        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, searchMessageData.getMentions())
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
                        .build();

        SpannableLookUp.text(ssb)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvMessageContent.getContext());

        String[] tokens = searchMessageData.getKeyword().split(" ");

        for (String token : tokens) {
            Pattern compile = Pattern.compile(token, Pattern.CASE_INSENSITIVE);
            Matcher matcher = compile.matcher(ssb);

            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                ssb.setSpan(new HighlightSpannable(0xfffffad1,
                        tvMessageContent.getCurrentTextColor()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        tvMessageContent.setText(ssb, TextView.BufferType.SPANNABLE);
    }

    private void mesureRoomInfoArea() {
        int roomNameAreaWidth = itemView.getLayoutParams().width
                - (int) UiUtils.getPixelFromDp(75) - (int) UiUtils.getPixelFromDp(16);

        int maxRoomNameWidth = (int) (roomNameAreaWidth * 0.4);
        Paint userNamePaint = tvRoomName.getPaint();
        int roomNameWidth = (int) userNamePaint.measureText(tvRoomName.getText().toString());
        LinearLayout.LayoutParams roomNameLP =
                (LinearLayout.LayoutParams) tvRoomName.getLayoutParams();
        if (roomNameWidth > maxRoomNameWidth) {
            roomNameLP.width = maxRoomNameWidth;
        } else {
            roomNameLP.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        tvRoomName.setLayoutParams(roomNameLP);
    }

    public void setOnClickMessageListener(OnClickMessageListener onClickMessageListener) {
        this.onClickMessageListener = onClickMessageListener;
    }

    public interface OnClickMessageListener {
        void onClickMessage(SearchMessageData searchMessageData);
    }

}