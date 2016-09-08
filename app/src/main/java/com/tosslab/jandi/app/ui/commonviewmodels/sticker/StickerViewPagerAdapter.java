package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.UiUtils;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 4..
 */
class StickerViewPagerAdapter extends PagerAdapter {

    public static final int STICKER_MAX_VIEW_PORTAIT = 8;
    public static final int STICKER_MAX_VIEW_LANDSCAPE = 6;
    public static final int PAGE_MULTIPLE = 30;
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
        int actualCount = getActualCount();
        if (actualCount == 1) {
            return actualCount;
        }
        return actualCount * PAGE_MULTIPLE;
    }

    public int getActualCount() {
        if (stickers == null) {
            return 0;
        }
        return ((stickers.size() - 1) / stickerMax) + 1;
    }

    public int getAcualPosition(int position, int actualCount) {
        return position % actualCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        int actualPosition = getAcualPosition(position, getActualCount());

        View view = getStickerItemLayout(actualPosition, Math.min(stickerMax, stickers.size() - (actualPosition * stickerMax)), stickerMax);
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
        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(matchParent, matchParent));
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

        LinearLayout.LayoutParams wrapperLayoutParams = new LinearLayout.LayoutParams(0, matchParent);
        wrapperLayoutParams.weight = 1;
        wrapperLayoutParams.leftMargin = padding / 2;
        wrapperLayoutParams.rightMargin = padding / 2;

        ViewGroup.LayoutParams imageViewLayoutParams = new ViewGroup.LayoutParams(matchParent, matchParent);

        int maxWidth = resources.getDimensionPixelSize(R.dimen.jandi_sticker_view_pager_items_max_width);

        for (int idx = 0; idx < size; idx++) {
            FrameLayout wrapper = new FrameLayout(context);
            wrapper.setLayoutParams(wrapperLayoutParams);

            ImageView child = new ImageView(context);
            child.setLayoutParams(imageViewLayoutParams);
            child.setMaxWidth(maxWidth);
            child.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            child.setDuplicateParentStateEnabled(true);

            wrapper.addView(child);

            View view = new View(context);
            view.setLayoutParams(imageViewLayoutParams);
            view.setBackgroundDrawable(UiUtils.getRippleEffectBackgroundDrawable());

            wrapper.addView(view);

            if (idx / columnCount < 1) {
                childTop.addView(wrapper);
            } else if (childBottom != null) {
                childBottom.addView(wrapper);
            }

            final ResMessages.StickerContent resSticker = stickers.get(idx + page * stickerMax);

            wrapper.setOnClickListener(v -> {
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

    private Drawable getStickerBackground(Context context) {
        int selectableItemBackground = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? android.R.attr.selectableItemBackgroundBorderless
                : android.R.attr.selectableItemBackground;
        int[] attrs = new int[]{selectableItemBackground};

        TypedArray ta = context.obtainStyledAttributes(attrs);

        Drawable drawable = ta.getDrawable(0 /* index */);

        ta.recycle();
        return drawable;
    }

}
