package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageHeaderData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class MessageHeaderViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.progress_init_loading)
    ProgressBar progressInitLoading;

    @Bind(R.id.tv_searched_result_message)
    TextView tvSearchMessage;

    @Bind(R.id.vg_searched_result_message)
    ViewGroup vgSearchedResultMessage;

    @Bind(R.id.vg_room_selection_button)
    ViewGroup vgRoomSelectionButton;

    @Bind(R.id.vg_room_selection_button_border)
    ViewGroup vgRoomSelectionButtonBorder;

    @Bind(R.id.tv_room_selection_button)
    TextView tvRoomSelectionButton;

    @Bind(R.id.iv_room_selection_button)
    ImageView ivRoomSelectionButton;

    @Bind(R.id.vg_member_selection_button_border)
    ViewGroup vgMemberSelectionButtonBorder;

    @Bind(R.id.vg_member_selection_button)
    ViewGroup vgMemberSelectionButton;

    @Bind(R.id.tv_member_selection_button)
    TextView tvMemberSelectionButton;

    @Bind(R.id.iv_member_selection_button)
    ImageView ivMemberSelectionButton;

    private OnClickRoomSelectionButtonListener onClickRoomSelectionButtonListener;
    private OnClickMemberSelectionButtonListener onClickMemberSelectionButtonListener;

    public MessageHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MessageHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_message_header, parent, false);
        return new MessageHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData data) {
        SearchMessageHeaderData searchMessageHeaderData = (SearchMessageHeaderData) data;

        if (searchMessageHeaderData.isShowSearchedResultMessage()) {
            vgSearchedResultMessage.setVisibility(View.VISIBLE);
        } else {
            vgSearchedResultMessage.setVisibility(View.GONE);
        }

        if (searchMessageHeaderData.isShowProgress()) {
            progressInitLoading.setVisibility(View.VISIBLE);
            tvSearchMessage.setText(
                    JandiApplication.getContext().getString(R.string.jandi_search_loading));

        } else {
            progressInitLoading.setVisibility(View.GONE);
            String count = searchMessageHeaderData.getSearchedMessageCount() + "";
            String searchedMessage = JandiApplication.getContext().getString(R.string.jandi_search_result_count,
                    count);

            SpannableStringBuilder ssb = new SpannableStringBuilder(searchedMessage);

            int startIndex = searchedMessage.indexOf(count);
            int endIndex = startIndex + count.length();

            ssb.setSpan(new StyleSpan(Typeface.BOLD),
                    startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvSearchMessage.setText(ssb);
        }

        vgRoomSelectionButton.setOnClickListener(v ->
                onClickRoomSelectionButtonListener.onClickRoomSelection());

        vgMemberSelectionButton.setOnClickListener(v ->
                onClickMemberSelectionButtonListener.onClickMemberSelection());

        if (!TextUtils.isEmpty(searchMessageHeaderData.getRoomName())) {
            tvRoomSelectionButton.setText(searchMessageHeaderData.getRoomName());
        }

        if (!TextUtils.isEmpty(searchMessageHeaderData.getMemberName())) {
            tvMemberSelectionButton.setText(searchMessageHeaderData.getMemberName());
        }

        setRoomButtonColor();
        setMemberButtonColor();
    }

    private void setRoomButtonColor() {
        if (TextUtils.equals(tvRoomSelectionButton.getText().toString(),
                JandiApplication.getContext().getString(R.string.jandi_search_category_everywhere))) {
            vgRoomSelectionButtonBorder.setBackgroundColor(0xff999999);
            tvRoomSelectionButton.setTextColor(0xff999999);
            ivRoomSelectionButton.setImageDrawable(
                    JandiApplication.getContext().getResources()
                            .getDrawable(R.drawable.filter_collepse));
        } else {
            vgRoomSelectionButtonBorder.setBackgroundColor(0xff00ace9);
            tvRoomSelectionButton.setTextColor(0xff00ace9);
            ivRoomSelectionButton.setImageDrawable(
                    JandiApplication.getContext().getResources()
                            .getDrawable(R.drawable.filter_collepse_on));
        }
    }

    private void setMemberButtonColor() {
        if (TextUtils.equals(tvMemberSelectionButton.getText().toString(),
                JandiApplication.getContext().getString(R.string.jandi_search_category_everyone))) {
            vgMemberSelectionButtonBorder.setBackgroundColor(0xff999999);
            tvMemberSelectionButton.setTextColor(0xff999999);
            ivMemberSelectionButton.setImageDrawable(
                    JandiApplication.getContext().getResources()
                            .getDrawable(R.drawable.filter_collepse));
        } else {
            vgMemberSelectionButtonBorder.setBackgroundColor(0xff00ace9);
            tvMemberSelectionButton.setTextColor(0xff00ace9);
            ivMemberSelectionButton.setImageDrawable(
                    JandiApplication.getContext().getResources()
                            .getDrawable(R.drawable.filter_collepse_on));
        }
    }

    public void setOnClickRoomSelectionButtonListener(
            OnClickRoomSelectionButtonListener onClickRoomSelectionButtonListener) {
        this.onClickRoomSelectionButtonListener = onClickRoomSelectionButtonListener;
    }

    public void setOnClickMemberSelectionButtonListener(
            OnClickMemberSelectionButtonListener onClickMemberSelectionButtonListener) {
        this.onClickMemberSelectionButtonListener = onClickMemberSelectionButtonListener;
    }

    public interface OnClickRoomSelectionButtonListener {
        void onClickRoomSelection();
    }

    public interface OnClickMemberSelectionButtonListener {
        void onClickMemberSelection();
    }

}