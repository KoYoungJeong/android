package com.tosslab.jandi.app.ui.message.v2.sticker;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.network.models.sticker.ResSticker;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 4..
 */
public class StickerViewPagerAdapter extends PagerAdapter {

    public static final int STICKER_MAX_VIEW = 8;
    private final Context context;
    private List<ResSticker> stickers;

    public StickerViewPagerAdapter(Context context, List<ResSticker> stickers) {
        this.context = context;
        this.stickers = stickers;
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


        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

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
        LogUtil.d("getStickerItemLayout Start, Page : " + page + ", Size : " + size);
        for (int idx = 0; idx < size; idx++) {
            LogUtil.d("getStickerItemLayout : " + idx);
            ImageView child = new ImageView(context);
            child.setLayoutParams(layoutParams);
            child.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            if ((idx - 0) / 4 < 1) {
                childTop.addView(child);
            } else {
                childBottom.addView(child);
            }

            ResSticker resSticker = stickers.get(idx + page * STICKER_MAX_VIEW);
            String fileName = resSticker.getGroupId() + "_" + resSticker.getId();

            Glide.with(context)
                    .load(Uri.parse("file:///android_asset/stickers/default/mozzi/" + fileName + ".png"))
                    .into(child);


        }

        return linearLayout;
    }
}
