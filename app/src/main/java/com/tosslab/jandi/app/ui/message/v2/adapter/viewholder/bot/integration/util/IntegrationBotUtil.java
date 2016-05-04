package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collection;
import java.util.Iterator;

public class IntegrationBotUtil {
    private static final String TAG = "IntegrationBotUtil";

    public static void setIntegrationSubUI(ResMessages.TextContent content, View vConnectLine, LinearLayout vgConnectInfo) {
        if (content.connectInfo != null && !content.connectInfo.isEmpty()) {

            if (vConnectLine.getVisibility() != View.VISIBLE) {
                vConnectLine.setVisibility(View.VISIBLE);
            }
            if (vgConnectInfo.getVisibility() != View.VISIBLE) {
                vgConnectInfo.setVisibility(View.VISIBLE);
            }

            updateSubInfoSideLine(content.connectColor, vConnectLine);
            updateSubInfo(content.connectInfo, vgConnectInfo);
        } else {
            vConnectLine.setVisibility(View.GONE);
            vgConnectInfo.setVisibility(View.GONE);
        }
    }

    private static void updateSubInfoSideLine(String connectColor, View vConnectLine) {
        try {
            float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, JandiApplication.getContext().getResources().getDisplayMetrics());
            int color = Color.parseColor(connectColor);
            RoundRectShape shape = new RoundRectShape(new float[]{v, v, 0, 0, 0, 0, v, v}, null, null);
            ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
            shapeDrawable.getPaint().setColor(color);
            vConnectLine.setBackgroundDrawable(shapeDrawable);
        } catch (Exception e) {
            LogUtil.d(TAG, "updateSubInfoSideLine" + e.getMessage());
            vConnectLine.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static void updateSubInfo(Collection<ResMessages.ConnectInfo> connectInfo, LinearLayout vgConnectInfo) {
        int viewChildGroupCount = vgConnectInfo.getChildCount() / 2;

        TextView tvTitle;
        TextView tvDescription;
        int viewChildIdx;
        ResMessages.ConnectInfo info;
        Iterator<ResMessages.ConnectInfo> iterator = connectInfo.iterator();

        SpannableStringBuilder title = new SpannableStringBuilder();
        SpannableStringBuilder description = new SpannableStringBuilder();

        int titleVisible = View.GONE;
        int descriptionVisible = View.GONE;
        for (int idx = 0; idx < viewChildGroupCount; ++idx) {
            viewChildIdx = idx * 2;

            tvTitle = (TextView) vgConnectInfo.getChildAt(viewChildIdx);
            tvDescription = (TextView) vgConnectInfo.getChildAt(viewChildIdx + 1);

            title.clear();
            description.clear();

            if (iterator.hasNext()) {

                info = iterator.next();
                if (!TextUtils.isEmpty(info.title)) {
                    titleVisible = View.VISIBLE;
                    SpannableStringBuilder spannableStringBuilder =
                            SpannableLookUp.text(info.title)
                                    .hyperLink(false)
                                    .lookUp(vgConnectInfo.getContext());
                    title.append(spannableStringBuilder);
                    LinkifyUtil.addLinks(tvTitle.getContext(), title);
                } else {
                    titleVisible = View.GONE;
                }

                if (!TextUtils.isEmpty(info.description)) {
                    SpannableStringBuilder spannableStringBuilder =
                            SpannableLookUp.text(info.description)
                                    .hyperLink(false)
                                    .lookUp(vgConnectInfo.getContext());
                    description.append(spannableStringBuilder);
                    LinkifyUtil.addLinks(tvDescription.getContext(), description);

                    descriptionVisible = View.VISIBLE;
                } else {
                    descriptionVisible = View.GONE;
                }

                tvTitle.setText(title);
                tvDescription.setText(description);

                if (titleVisible == View.GONE && descriptionVisible == View.VISIBLE) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvDescription.getLayoutParams();
                    if (idx == 0) {
                        layoutParams.topMargin = 0;
                    } else {
                        DisplayMetrics displayMetrics = tvDescription.getResources().getDisplayMetrics();
                        layoutParams.topMargin = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                11f,
                                displayMetrics);
                    }

                    tvDescription.setLayoutParams(layoutParams);
                }

                LinkifyUtil.setOnLinkClick(tvTitle);
                LinkifyUtil.setOnLinkClick(tvDescription);
            } else {
                titleVisible = View.GONE;
                descriptionVisible = View.GONE;
            }

            tvTitle.setVisibility(titleVisible);
            tvDescription.setVisibility(descriptionVisible);

        }
    }
}
