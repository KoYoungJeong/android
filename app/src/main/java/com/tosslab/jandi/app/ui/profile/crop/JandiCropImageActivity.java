package com.tosslab.jandi.app.ui.profile.crop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.soundcloud.android.crop.CropImageActivity;
import com.tosslab.jandi.app.R;

/**
 * Created by tee on 16. 1. 8..
 */
public class JandiCropImageActivity extends CropImageActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.done_cancel_bar);
        viewGroup.setBackgroundColor(getResources().getColor(R.color.jandi_grey_black));
        LinearLayout tempLayout =
                (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_done_cancel_actionbar_for_crop, null);
        FrameLayout vCancel = (FrameLayout) findViewById(R.id.btn_cancel);
        vCancel.removeAllViews();
        vCancel.addView(tempLayout.findViewById(R.id.btn_cancel_));
        FrameLayout vOk = (FrameLayout) findViewById(R.id.btn_done);
        vOk.removeAllViews();
        vOk.addView(tempLayout.findViewById(R.id.btn_done_));
    }
}
