package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.AutoScaleImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 6. 18..
 */
public class LinkPreviewViewModel {

    public static final int LINK_PREVIEW_WIDTH_RATIO = 338;
    public static final int LINK_PREVIEW_HEIGHT_RATIO = 180;
    public static final String TAG = "LinkPreviewViewModel";
    private TextView tvTitle;
    private TextView tvDomain;
    private TextView tvDescription;
    private View vgThumb;
    private AutoScaleImageView ivThumb;
    private OnLinkPreviewClickListener onLinkPreviewClickListener;

    private Context context;
    private ViewGroup vgLinkPreview;

    private Map<String, Boolean> errorThumbnailMap;

    public LinkPreviewViewModel(Context context) {
        this.context = context;
        errorThumbnailMap = new HashMap<>();
    }

    public void initView(View rootView) {
        vgLinkPreview = (ViewGroup) rootView.findViewById(R.id.vg_linkpreview);
        vgLinkPreview.setOnClickListener(
                onLinkPreviewClickListener = new OnLinkPreviewClickListener(context));

        tvTitle = (TextView) rootView.findViewById(R.id.tv_linkpreview_title);
        tvDomain = (TextView) rootView.findViewById(R.id.tv_linkpreview_domain);
        tvDescription = (TextView) rootView.findViewById(R.id.tv_linkpreview_description);
        vgThumb = rootView.findViewById(R.id.vg_linkpreview_thumb);
        ivThumb = (AutoScaleImageView) rootView.findViewById(R.id.iv_linkpreview_thumb);
        ivThumb.setRatio(LINK_PREVIEW_WIDTH_RATIO, LINK_PREVIEW_HEIGHT_RATIO);
    }

    public void bindData(ResMessages.Link link) {

        ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;

        if (message.linkPreview == null || TextUtils.isEmpty(message.linkPreview.linkUrl)) {
            vgLinkPreview.setVisibility(View.GONE);
            return;
        } else {
            vgLinkPreview.setVisibility(View.VISIBLE);
        }

        ResMessages.LinkPreview linkPreview = message.linkPreview;

        tvTitle.setText(linkPreview.title);
        tvDomain.setText(linkPreview.domain);

        String description = linkPreview.description;
        tvDescription.setText(TextUtils.isEmpty(description) ? "" : description);
        tvDescription.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);

        onLinkPreviewClickListener.setLinkUrl(linkPreview.linkUrl);

        final String imageUrl = linkPreview.imageUrl;
        if (TextUtils.isEmpty(imageUrl)) {
            LogUtil.e(TAG, "imageUrl is null");
            vgThumb.setVisibility(View.GONE);
            ivThumb.setImageDrawable(null);
        } else {
            LogUtil.i(TAG, "imageUrl =" + imageUrl);
            if (errorThumbnailMap.containsKey(imageUrl)) {
                LogUtil.e(TAG, "imageUrl(" + imageUrl + ") has error.");
                vgThumb.setVisibility(View.GONE);
                ivThumb.setImageDrawable(null);
                return;
            }

            vgThumb.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.link_preview)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e,
                                                   String model, Target<Bitmap> target,
                                                   boolean isFirstResource) {
                            LogUtil.e(TAG, "error - " + model);
                            LogUtil.e(TAG, Log.getStackTraceString(e));

                            if (imageUrl.equals(model)) {
                                errorThumbnailMap.put(model, true);
                                vgThumb.setVisibility(View.GONE);
                            }
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource,
                                                       String model, Target<Bitmap> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(ivThumb);
        }
    }

    private static class OnLinkPreviewClickListener implements View.OnClickListener {
        private final Context context;
        private String linkUrl;

        public OnLinkPreviewClickListener(Context context) {

            this.context = context;
        }

        public void setLinkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
        }

        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(linkUrl)) {
                return;
            }

            InternalWebActivity_.intent(context)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .url(linkUrl)
                    .start();

            if (context instanceof Activity) {
                Activity activity = ((Activity) context);
                activity.overridePendingTransition(
                        R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
            }

        }
    }
}
