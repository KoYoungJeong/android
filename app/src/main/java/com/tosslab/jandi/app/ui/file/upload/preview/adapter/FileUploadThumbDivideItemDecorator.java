package com.tosslab.jandi.app.ui.file.upload.preview.adapter;


import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;

public class FileUploadThumbDivideItemDecorator extends RecyclerView.ItemDecoration {

    private final int contentPadding;
    private final int itemMargin;

    public FileUploadThumbDivideItemDecorator() {
        super();
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        contentPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, displayMetrics);
        itemMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.5f, displayMetrics);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            outRect.set(contentPadding, contentPadding, itemMargin, -contentPadding);
        } else if (parent.getAdapter().getItemCount() - 1 == position) {
            outRect.set(itemMargin, contentPadding, contentPadding, -contentPadding);
        } else {
            outRect.set(itemMargin, contentPadding, itemMargin, -contentPadding);
        }
    }
}
