package com.tosslab.jandi.app.ui.sticker;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 4..
 */
class StickerViewPagerAdapter extends PagerAdapter {

    public static final int STICKER_MAX_VIEW = 8;
    private final Context context;
    private final StickerViewModel.OnStickerClick onStickerClick;
    private List<ResMessages.StickerContent> stickers;

    protected StickerViewPagerAdapter(Context context, List<ResMessages.StickerContent> stickers, StickerViewModel.OnStickerClick onStickerClick) {
        this.context = context;
        this.stickers = stickers;
        this.onStickerClick = onStickerClick;
    }

    @Override
    public int getCount() {
        if (stickers == null) {
            return 0;
        }
        return ((stickers.size() - 1) / STICKER_MAX_VIEW) + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View view = getStickerItemLayout(position, Math.min(STICKER_MAX_VIEW, stickers.size() - (position * STICKER_MAX_VIEW)));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView(((View) object));
        }
    }

    private LinearLayout getStickerItemLayout(int page, int size) {

        int padding = context.getResources().getDimensionPixelSize(R.dimen.jandi_sticker_view_pager_padding);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setPadding(padding / 2 + 1, padding / 2 + 1, padding / 2 + 1, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1;

        LinearLayout childTop = new LinearLayout(context);
        childTop.setOrientation(LinearLayout.HORIZONTAL);
        childTop.setWeightSum(4f);
        childTop.setLayoutParams(params);

        LinearLayout childBottom = new LinearLayout(context);
        childBottom.setOrientation(LinearLayout.HORIZONTAL);
        childBottom.setWeightSum(4f);
        childBottom.setLayoutParams(params);

        linearLayout.addView(childTop);
        linearLayout.addView(childBottom);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        layoutParams.leftMargin = padding / 2;
        layoutParams.rightMargin = padding / 2;

        int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.jandi_sticker_view_pager_items_max_width);

        for (int idx = 0; idx < size; idx++) {
            ImageView child = new ImageView(context);
            child.setLayoutParams(layoutParams);
            child.setMaxWidth(maxWidth);
            child.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            if (idx / 4 < 1) {
                childTop.addView(child);
            } else {
                childBottom.addView(child);
            }

            final ResMessages.StickerContent resSticker = stickers.get(idx + page * STICKER_MAX_VIEW);

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
