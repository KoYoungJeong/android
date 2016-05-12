package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.views.FrescoImageView;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 4..
 */
class StickerViewPagerAdapter extends PagerAdapter {

    public static final int STICKER_MAX_VIEW_PORTAIT = 8;
    public static final int STICKER_MAX_VIEW_LANDSCAPE = 6;
    private final Context context;
    private final StickerViewModel.OnStickerClick onStickerClick;
    private List<ResMessages.StickerContent> stickers;
    private int stickerMax;

    protected StickerViewPagerAdapter(Context context, List<ResMessages.StickerContent> stickers, StickerViewModel.OnStickerClick onStickerClick) {
        this.context = context;
        this.stickers = stickers;

        this.onStickerClick = onStickerClick;
        stickerMax = STICKER_MAX_VIEW_PORTAIT;

    }

    @Override
    public int getCount() {
        if (stickers == null) {
            return 0;
        }

        return ((stickers.size() - 1) / stickerMax) + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View view = getStickerItemLayout(position, Math.min(stickerMax, stickers.size() - (position * stickerMax)), stickerMax);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView(((View) object));
        }
    }

    private LinearLayout getStickerItemLayout(int page, int size, int stickerMax) {

        boolean isPortrait = stickerMax == STICKER_MAX_VIEW_PORTAIT;
        int columnCount = isPortrait ? STICKER_MAX_VIEW_PORTAIT / 2 : STICKER_MAX_VIEW_LANDSCAPE;

        Resources resources = context.getResources();

        int padding = resources.getDimensionPixelSize(R.dimen.jandi_sticker_view_pager_padding);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setPadding(padding / 2 + 1, padding / 2 + 1, padding / 2 + 1, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1;

        LinearLayout childTop = new LinearLayout(context);
        childTop.setOrientation(LinearLayout.HORIZONTAL);
        childTop.setWeightSum(columnCount);
        childTop.setLayoutParams(params);

        linearLayout.addView(childTop);

        LinearLayout childBottom = null;
        if (isPortrait) {

            childBottom = new LinearLayout(context);
            childBottom.setOrientation(LinearLayout.HORIZONTAL);
            childBottom.setWeightSum(columnCount);
            childBottom.setLayoutParams(params);
            linearLayout.addView(childBottom);
        }


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        layoutParams.leftMargin = padding / 2;
        layoutParams.rightMargin = padding / 2;

        int maxWidth = resources.getDimensionPixelSize(R.dimen.jandi_sticker_view_pager_items_max_width);

        Drawable pressedDrawable = new ColorDrawable(Color.parseColor("#45ffffff"));

        for (int idx = 0; idx < size; idx++) {
            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(resources)
                    .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .setPressedStateOverlay(pressedDrawable)
                    .build();

            SimpleDraweeView child = new FrescoImageView(context, hierarchy);
            child.setLayoutParams(layoutParams);
            child.setMaxWidth(maxWidth);
            child.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            if (idx / columnCount < 1) {
                childTop.addView(child);
            } else if (childBottom != null) {
                childBottom.addView(child);
            }

            final ResMessages.StickerContent resSticker = stickers.get(idx + page * stickerMax);

            child.setOnClickListener(v -> {
                if (onStickerClick != null) {
                    onStickerClick.onStickerClick(resSticker.groupId, resSticker.stickerId);
                }
            });

            StickerManager.LoadOptions options = new StickerManager.LoadOptions();
            options.isClickImage = true;
            options.isFadeAnimation = false;
            StickerManager.getInstance()
                    .loadSticker(child, resSticker.groupId, resSticker.stickerId, options);

        }

        return linearLayout;
    }
}
