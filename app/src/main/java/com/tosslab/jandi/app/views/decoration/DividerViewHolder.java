package com.tosslab.jandi.app.views.decoration;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class DividerViewHolder extends BaseViewHolder<DividerViewHolder.Info> {

    private DividerViewHolder(View itemView) {
        super(itemView);
    }

    public static DividerViewHolder newInstance(ViewGroup parent) {
        View itemView = new View(parent.getContext());

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(width, height);
        layoutParams.setMargins(layoutParams.leftMargin, (int) UiUtils.getPixelFromDp(8),
                layoutParams.rightMargin, layoutParams.bottomMargin);
        itemView.setLayoutParams(layoutParams);


        return new DividerViewHolder(itemView);
    }

    @Override
    public void onBindView(Info info) {
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (layoutParams.height != info.height) {
            layoutParams.height = info.height;
            itemView.setLayoutParams(layoutParams);
        }

        itemView.setBackgroundColor(info.getColor());
    }

    public static class Info {
        private int height;
        private int color = Color.TRANSPARENT;

        private Info(int height, int color) {
            this.height = height;
            this.color = color;
        }

        public static Info create(int height, int color) {
            return new Info(height, color);
        }

        public int getHeight() {
            return height;
        }

        public int getColor() {
            return color;
        }
    }
}
