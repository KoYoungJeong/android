package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v7.view.ContextThemeWrapper;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.colors.ColorUtils;

import java.util.Collection;
import java.util.Iterator;

public class IntegrationBotUtil {
    private static final String TAG = "IntegrationBotUtil";
    private static final int UNIT_VIEW = 3;
    private static final int MAX_ITEM_COUNT = 50;

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
        float v = UiUtils.getPixelFromDp(2f);
        int color = ColorUtils.parseColor(connectColor);
        RoundRectShape shape = new RoundRectShape(new float[]{v, v, 0, 0, 0, 0, v, v}, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        vConnectLine.setBackgroundDrawable(shapeDrawable);
    }

    private static void updateSubInfo(Collection<ResMessages.ConnectInfo> connectInfo, ViewGroup vgConnectInfo) {

        TextView tvTitle;
        TextView tvDescription;
        TextView tvImage;
        ResMessages.ConnectInfo info;

        Iterator<ResMessages.ConnectInfo> iterator = connectInfo.iterator();
        int count = Math.min(MAX_ITEM_COUNT, connectInfo.size());

        SpannableStringBuilder title = new SpannableStringBuilder();
        SpannableStringBuilder description = new SpannableStringBuilder();
        SpannableStringBuilder image = new SpannableStringBuilder();

        int titleVisible;
        int descriptionVisible;
        int imageVisible;

        vgConnectInfo.removeAllViews();

        float marginBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, vgConnectInfo.getResources().getDisplayMetrics());

        for (int idx = 0; idx < count; ++idx) {

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
                } else {
                    titleVisible = View.GONE;
                }

                if (!TextUtils.isEmpty(info.description)) {
                    description.append(info.description);
                    SpannableLookUp.text(description)
                            .hyperLink(false)
                            .markdown(false)
                            .webLink(false)
                            .telLink(false)
                            .emailLink(false)
                            .lookUp(vgConnectInfo.getContext());

                    descriptionVisible = View.VISIBLE;
                } else {
                    descriptionVisible = View.GONE;
                }

                if (!TextUtils.isEmpty(info.imageUrl)) {
                    image.append(info.imageUrl);
                    SpannableLookUp.text(image)
                            .hyperLink(false)
                            .lookUp(vgConnectInfo.getContext());
                    imageVisible = View.VISIBLE;
                } else {
                    imageVisible = View.GONE;
                }

            } else {
                titleVisible = View.GONE;
                descriptionVisible = View.GONE;
                imageVisible = View.GONE;
            }

            if (titleVisible == View.VISIBLE) {
                tvTitle = new TextView(new ContextThemeWrapper(vgConnectInfo.getContext(), R.style.JandiIntegration_Sub_Title));
                LinkifyUtil.setOnLinkClick(tvTitle);
                tvTitle.setText(title);
                vgConnectInfo.addView(tvTitle);

                if (idx == 0) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvTitle.getLayoutParams();
                    lp.topMargin = 0;
                    lp.bottomMargin = (int) marginBottom;
                    tvTitle.setLayoutParams(lp);
                }
            }

            if (descriptionVisible == View.VISIBLE) {
                tvDescription = new TextView(new ContextThemeWrapper(vgConnectInfo.getContext(), R.style.JandiIntegration_Sub_Description));
                tvDescription.setText(description);
                LinkifyUtil.setOnLinkClick(tvDescription);
                vgConnectInfo.addView(tvDescription);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvDescription.getLayoutParams();
                lp.topMargin = 0;
                lp.bottomMargin = (int) marginBottom;
                tvDescription.setLayoutParams(lp);
            }

            if (imageVisible == View.VISIBLE) {
                tvImage = new TextView(new ContextThemeWrapper(vgConnectInfo.getContext(), R.style.JandiIntegration_Sub_Image));
                tvImage.setText(image);
                LinkifyUtil.setOnLinkClick(tvImage);
                tvImage.setVisibility(imageVisible);
                vgConnectInfo.addView(tvImage);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvImage.getLayoutParams();
                lp.topMargin = 0;
                tvImage.setLayoutParams(lp);

            }


        }
    }
}
