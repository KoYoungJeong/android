package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main.object.SearchData;
import com.tosslab.jandi.app.ui.search.main.object.SearchOneToOneRoomData;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2016. 8. 19..
 */

public class OneToOneRoomItemViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.iv_profile)
    ImageView ivProfile;
    @Bind(R.id.tv_room_name)
    TextView tvRoomName;
    @Bind(R.id.v_full_divider)
    View vFullDivider;
    @Bind(R.id.v_half_divider)
    View vHalfDivider;

    private OnClickOneToOneRoomListener onClickOneToOneRoomListener;

    public OneToOneRoomItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static OneToOneRoomItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_one_to_one_room, parent, false);
        return new OneToOneRoomItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        SearchOneToOneRoomData searchOneToOneRoomData = (SearchOneToOneRoomData) searchData;
        String photoUrl = searchOneToOneRoomData.getUserProfileUrl();
        ImageUtil.loadProfileImage(ivProfile, photoUrl, R.drawable.profile_img);

        SpannableStringBuilder ssb = new SpannableStringBuilder(searchOneToOneRoomData.getTitle());

        Pattern compile = Pattern.compile(searchOneToOneRoomData.getKeyword(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = compile.matcher(ssb);

        matcher.find();

        int start = matcher.start();
        int end = matcher.end();

        ssb.setSpan(new HighlightSpannable(0xfffffad1,
                tvRoomName.getCurrentTextColor()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRoomName.setText(ssb);


        itemView.setOnClickListener(v -> {
            if (onClickOneToOneRoomListener != null) {
                onClickOneToOneRoomListener.onClick(searchOneToOneRoomData.getMemberId());
            }
        });

        if (searchOneToOneRoomData.hasHalfLine()) {
            vFullDivider.setVisibility(View.GONE);
            vHalfDivider.setVisibility(View.VISIBLE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vHalfDivider.setVisibility(View.GONE);
        }
    }

    public void setOnClickOneToOneRoomListener(OnClickOneToOneRoomListener onClickOneToOneRoomListener) {
        this.onClickOneToOneRoomListener = onClickOneToOneRoomListener;
    }

    public interface OnClickOneToOneRoomListener {
        void onClick(long memberId);
    }
}
