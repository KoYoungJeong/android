package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collection;
import java.util.Iterator;

public class IntegrationBotUtil {
    private static final String TAG = "IntegrationBotUtil";

    public static void setIntegrationSubUI(ResMessages.TextContent content, View vConnectLine, ViewGroup vgConnectInfo) {
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
            float v = UiUtils.getPixelFromDp(2f);
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

    private static void updateSubInfo(Collection<ResMessages.ConnectInfo> connectInfo, ViewGroup vgConnectInfo) {
        int UNIT_VIEW = 3;
        int viewChildGroupCount = vgConnectInfo.getChildCount() / UNIT_VIEW;

        TextView tvTitle;
        TextView tvDescription;
        TextView tvImage;
        int viewChildIdx;
        ResMessages.ConnectInfo info;
        Iterator<ResMessages.ConnectInfo> iterator = connectInfo.iterator();

        SpannableStringBuilder title = new SpannableStringBuilder();
        SpannableStringBuilder description = new SpannableStringBuilder();
        SpannableStringBuilder image = new SpannableStringBuilder();

        int titleVisible = View.GONE;
        int descriptionVisible = View.GONE;
        int imageVisible = View.GONE;

        for (int idx = 0; idx < viewChildGroupCount; ++idx) {
            viewChildIdx = idx * UNIT_VIEW;

            tvTitle = (TextView) vgConnectInfo.getChildAt(viewChildIdx);
            tvDescription = (TextView) vgConnectInfo.getChildAt(viewChildIdx + 1);
            tvImage = (TextView) vgConnectInfo.getChildAt(viewChildIdx + 2);

            title.clear();
            description.clear();
            image.clear();

            if (iterator.hasNext()) {

                info = iterator.next();
                if (!TextUtils.isEmpty(info.title)) {
                    titleVisible = View.VISIBLE;
                    title.append(info.title);
                    SpannableLookUp.text(title)
                            .hyperLink(false)
                            .markdown(false)
                            .webLink(false)
                            .telLink(false)
                            .emailLink(false)
                            .lookUp(vgConnectInfo.getContext());
                    LinkifyUtil.addLinks(tvTitle.getContext(), title);
                } else {
                    titleVisible = View.GONE;
                }

                tvTitle.setText(title);

                if (!TextUtils.isEmpty(info.description)) {
                    description.append(info.description);
                    SpannableLookUp.text(description)
                            .hyperLink(false)
                            .markdown(false)
                            .webLink(false)
                            .telLink(false)
                            .emailLink(false)
                            .lookUp(vgConnectInfo.getContext());
                    LinkifyUtil.addLinks(tvDescription.getContext(), description);

                    descriptionVisible = View.VISIBLE;
                } else {
                    descriptionVisible = View.GONE;
                }

                tvDescription.setText(description);

                if (!TextUtils.isEmpty(info.imageUrl)) {
                    image.append(info.imageUrl);
                    SpannableLookUp.text(image)
                            .hyperLink(false)
                            .lookUp(vgConnectInfo.getContext());
                    LinkifyUtil.addLinks(tvDescription.getContext(), image);
                    imageVisible = View.VISIBLE;
                } else {
                    imageVisible = View.GONE;
                }

                tvImage.setText(image);

                LinkifyUtil.setOnLinkClick(tvTitle);
                LinkifyUtil.setOnLinkClick(tvDescription);
                LinkifyUtil.setOnLinkClick(tvImage);
            } else {
                titleVisible = View.GONE;
                descriptionVisible = View.GONE;
                imageVisible = View.GONE;
            }

            tvTitle.setVisibility(titleVisible);
            tvDescription.setVisibility(descriptionVisible);
            tvImage.setVisibility(imageVisible);

        }
    }
}
