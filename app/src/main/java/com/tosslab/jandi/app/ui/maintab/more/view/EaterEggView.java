package com.tosslab.jandi.app.ui.maintab.more.view;

import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;

@EBean
public class EaterEggView {

    @ViewById(R.id.vg_more_bottom_wrapper)
    FrameLayout vgMoreBottomWrapper;
    @ViewById(R.id.tv_more_additional_text)
    TextView tvMoreAdditionalText;
    @ViewById(R.id.iv_more_additional_image)
    ImageView ivMoreAdditionalImage;
    @ViewById(R.id.iv_more_additional_image_cover)
    ImageView ivMoreAdditionalImageCover;

    @AfterViews
    void initViews() {
        StringBuilder sb = new StringBuilder();

        String line1 = getTextWithSpace("MERRY");
        String line2 = getTextWithSpace("CHRISTMAS");

        if (shouldShowHappyNewYear()) {
            line1 = getTextWithSpace("HAPPY");
            line2 = getTextWithSpace("NEWYEAR");
        }

        sb.append(line1).append("\n");
        sb.append(line2);

        tvMoreAdditionalText.setText(sb.toString());
        ivMoreAdditionalImage.setImageResource(R.drawable.christmas_tree);
        ivMoreAdditionalImageCover.setImageResource(R.drawable.christmas_tree_longtap);
        ivMoreAdditionalImageCover.setAlpha(0.0f);

        vgMoreBottomWrapper.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    long downTime = event.getDownTime();
                    long eventTime = event.getEventTime();
                    long gap = eventTime - downTime;
                    if (gap > 30) {
                        ivMoreAdditionalImageCover.animate()
                                .alpha(1.0f)
                                .setDuration(1000);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ivMoreAdditionalImageCover.animate()
                            .alpha(0.0f)
                            .setDuration(1000);
                    break;
            }
            return true;
        });
    }

    private boolean shouldShowHappyNewYear() {
        long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.DECEMBER, 28);
        Date shouldShowHappyNewYearDate = calendar.getTime();

        long shouldShowHappyNewYearTime = shouldShowHappyNewYearDate.getTime();


        return currentTime >= shouldShowHappyNewYearTime;
    }

    private String getTextWithSpace(String text) {
        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
            if (i < chars.length - 1) {
                sb.append("  ");
            }
        }
        return sb.toString();
    }

}
