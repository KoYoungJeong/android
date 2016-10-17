package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.LinkPreviewClickEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 18..
 */
public class LinkPreviewViewModel {

    public static final String TAG = "LinkPreviewViewModel";
    private LinearLayout vgSummary;
    private TextView tvTitle;
    private TextView tvDomain;
    private TextView tvDescription;

    private ImageView ivThumb;
    private OnLinkPreviewClickListener onLinkPreviewClickListener;

    private Context context;
    private ViewGroup vgLinkPreview;
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
            LayoutInflater.from(vgLinkPreview.getContext())
                    .inflate(R.layout.item_message_layout_linkpreview_v2, vgLinkPreview, true);
            initInnerView(vgLinkPreview);
            vgLinkPreview.setVisibility(View.VISIBLE);
            vDividier.setVisibility(View.VISIBLE);
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

        // LinkPreview 클릭시 이벤트코드 삽입과 액티비티를 필요로 해서 불가피하게 EventBus 사용
        final String linkUrl = linkPreview.linkUrl;
        ivThumb.setOnClickListener(v -> {
            EventBus.getDefault().post(
                    new LinkPreviewClickEvent(linkUrl, LinkPreviewClickEvent.TouchFrom.IMAGE));
        });
        vgSummary.setOnClickListener(v -> {
            EventBus.getDefault().post(
                    new LinkPreviewClickEvent(linkUrl, LinkPreviewClickEvent.TouchFrom.IMAGE));
        });

        boolean useThumbnail = useThumbnail(linkPreview.imageUrl);

        final Resources resources = ivThumb.getResources();

        if (!useThumbnail) {
            disableImage();
        } else {
            showImage(linkPreview, resources);
        }
    }

    void showImage(ResMessages.LinkPreview linkPreview, final Resources resources) {
        vDividier.setVisibility(View.VISIBLE);
        ImageLoader.newInstance()
                .backgroundColor(resources.getColor(R.color.jandi_messages_image_background))
                .placeHolder(R.drawable.comment_image_preview_download, ImageView.ScaleType.CENTER_INSIDE)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .uri(Uri.parse(linkPreview.imageUrl))
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        disableImage();
                        return true;
                    }
                })
                .into(ivThumb);
    }

    void disableImage() {
        vgSummary.setBackgroundResource(R.drawable.bg_round_white_rect_for_message);
        ivThumb.setVisibility(View.GONE);
        vDividier.setVisibility(View.GONE);
    }

    private void initInnerView(ViewGroup vgLinkPreview) {
        tvTitle = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_title);
        tvDomain = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_domain);
        tvDescription = (TextView) vgLinkPreview.findViewById(R.id.tv_linkpreview_description);
        ivThumb = (ImageView) vgLinkPreview.findViewById(R.id.iv_linkpreview_thumb);
        vgSummary = (LinearLayout) vgLinkPreview.findViewById(R.id.vg_snippet_summary);
        vDividier = vgLinkPreview.findViewById(R.id.v_snippet_divider);
    }

    private boolean useThumbnail(String imagUrl) {
        return !TextUtils.isEmpty(imagUrl) && imagUrl.contains("linkpreview-thumb");
    }
}
