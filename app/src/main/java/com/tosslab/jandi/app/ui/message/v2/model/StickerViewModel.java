package com.tosslab.jandi.app.ui.message.v2.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
@EBean
public class StickerViewModel {

    @ViewById(R.id.vg_message_sticker_selector)
    ViewGroup vgStickerSelector;

    @ViewById(R.id.btn_message_sticker)
    View btnStickerShow;

    @RootContext
    Context conext;

    @AfterViews
    void initViews() {
        LayoutInflater.from(conext).inflate(R.layout.layout_stickers_default, vgStickerSelector, true);
    }

    public void showStickerSelector(int keyboardHeight) {
        ViewGroup.LayoutParams layoutParams = vgStickerSelector.getLayoutParams();
        if (layoutParams.height != keyboardHeight) {
            layoutParams.height = keyboardHeight;
            vgStickerSelector.setLayoutParams(layoutParams);
        }

        vgStickerSelector.setVisibility(View.VISIBLE);
        btnStickerShow.setSelected(true);
    }

    public void dismissStickerSelector() {
        vgStickerSelector.setVisibility(View.GONE);
        btnStickerShow.setSelected(false);
    }


}
