package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.listener.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

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
    private View vgSnippet;
    private View vDividier;

    public LinkPreviewViewModel(Context context) {
        this.context = context;
    }

    public void initView(View rootView) {
        vgLinkPreview = (ViewGroup) rootView.findViewById(R.id.vg_linkpreview);
    }

    public void bindData(ResMessages.Link link) {

        ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;

        if (message.linkPreview == null || TextUtils.isEmpty(message.linkPreview.linkUrl)) {
            vgLinkPreview.setVisibility(View.GONE);
            return;
        } else {
            vgLinkPreview.removeAllViews();
            LayoutInflater.from(vgLinkPreview.getContext()).inflate(R.layout.item_message_layout_linkpreview_v2, vgLinkPreview, true);
            initInnerView(vgLinkPreview);
            vgLinkPreview.setVisibility(View.VISIBLE);
        }

        ResMessages.LinkPreview linkPreview = message.linkPreview;

        if (!TextUtils.isEmpty(linkPreview.title)) {

            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(linkPreview.title);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(linkPreview.domain)) {
            tvDomain.setVisibility(View.VISIBLE);
            tvDomain.setText(linkPreview.domain);
        } else {
            tvDomain.setVisibility(View.GONE);
        }

        String description = linkPreview.description;
        if (!TextUtils.isEmpty(description)) {
            tvDescription.setText(description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        onLinkPreviewClickListener.setLinkUrl(linkPreview.linkUrl);

        boolean useThumbnail = useThumbnail(linkPreview.imageUrl);

        if (!useThumbnail) {
            ivThumb.setImageURI(null);
            vgThumb.setVisibility(View.GONE);
            vDividier.setVisibility(View.GONE);
            vgSnippet.setBackgroundResource(R.drawable.bg_round_white_rect_for_message);
        } else {
            vgSnippet.setBackgroundResource(R.drawable.bg_round_bottom_white_for_message);

            vDividier.setVisibility(View.VISIBLE);
            vgThumb.setVisibility(View.VISIBLE);

            String imageUrl = linkPreview.imageUrl;


            float imageRound = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, JandiApplication.getContext().getResources().getDisplayMetrics());
            RoundingParams roundingParams = RoundingParams.fromCornersRadii(imageRound, imageRound, 0, 0);

            ImageLoader.newBuilder()
                    .roundingParams(roundingParams)
                    .placeHolder(R.drawable.link_preview, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .callback(new BaseOnResourceReadyCallback() {
                        @Override
                        public void onFail(Throwable cause) {
                            vgThumb.setBackgroundResource(R.drawable.bg_round_top_green_for_message);
                            ImageLoader.newBuilder()
                                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                                    .load(UriFactory.getResourceUri(R.drawable.preview_no_img))
                                    .into(ivThumb);
                        }
                    })
                    .load(Uri.parse(imageUrl))
                    .into(ivThumb);

        }

    }

    private void initInnerView(ViewGroup vgLinkPreview) {
        vgLinkPreview.setOnClickListener(
                onLinkPreviewClickListener = new OnLinkPreviewClickListener(context));

        tvTitle = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_title);
        tvDomain = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_domain);
        tvDescription = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_description);
        vgThumb = vgLinkPreview.findViewById(R.id.vg_linkpreview_thumb);
        ivThumb = (SimpleDraweeView) vgLinkPreview.findViewById(R.id.iv_linkpreview_thumb);
        vgSnippet = vgLinkPreview.findViewById(R.id.vg_snippet_summary);
        vDividier = vgLinkPreview.findViewById(R.id.v_snippet_divider);
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

            ApplicationUtil.startWebBrowser(context, linkUrl);

            if (context instanceof Activity) {
                Activity activity = ((Activity) context);
                activity.overridePendingTransition(
                        R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
            }

        }
    }
}
