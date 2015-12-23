package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

/**
 * Created by Steve SeongUg Jung on 15. 6. 18..
 */
public class LinkPreviewViewModel {

    public static final String TAG = "LinkPreviewViewModel";
    private TextView tvTitle;
    private TextView tvDomain;
    private TextView tvDescription;
    private View vgThumb;
    private SimpleDraweeView ivThumb;
    private OnLinkPreviewClickListener onLinkPreviewClickListener;

    private Context context;
    private ViewGroup vgLinkPreview;

    public LinkPreviewViewModel(Context context) {
        this.context = context;
    }

    public void initView(View rootView) {
        vgLinkPreview = (ViewGroup) rootView.findViewById(R.id.vg_linkpreview);
        vgLinkPreview.setOnClickListener(
                onLinkPreviewClickListener = new OnLinkPreviewClickListener(context));

        tvTitle = (TextView) rootView.findViewById(R.id.tv_linkpreview_title);
        tvDomain = (TextView) rootView.findViewById(R.id.tv_linkpreview_domain);
        tvDescription = (TextView) rootView.findViewById(R.id.tv_linkpreview_description);
        vgThumb = rootView.findViewById(R.id.vg_linkpreview_thumb);
        ivThumb = (SimpleDraweeView) rootView.findViewById(R.id.iv_linkpreview_thumb);
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

        boolean useThumbnail = useThumbnail(linkPreview.imageUrl);

        GenericDraweeHierarchy hierarchy = ivThumb.getHierarchy();
        Drawable placeHolder = context.getResources().getDrawable(R.drawable.link_preview);
        hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.CENTER_INSIDE);
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        ivThumb.setHierarchy(hierarchy);
        if (!useThumbnail) {
            ivThumb.setImageURI(null);
            vgThumb.setVisibility(View.GONE);
            return;
        }

        vgThumb.setVisibility(View.VISIBLE);

        String imageUrl = linkPreview.imageUrl;
        ivThumb.setImageURI(Uri.parse(imageUrl));
    }

    private boolean useThumbnail(String imagUrl) {
        return !TextUtils.isEmpty(imagUrl) && imagUrl.contains("linkpreview-thumb");
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
