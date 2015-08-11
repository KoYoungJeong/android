package com.tosslab.jandi.app.ui.commonviewmodels.mention.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 20..
 */
public class MentionSpannable extends ReplacementSpan {

    private TextView tvDate;

    public MentionSpannable(Context context, String date) {

        super();

        tvDate = new TextView(context);
        tvDate.setText(date);
        tvDate.setTextColor(0xFFfefefe);
        tvDate.setBackgroundColor(0xFF01a4e7);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources()
                .getDimensionPixelSize(R.dimen.jandi_mention_edit_text_item_font_size));

        prepareView();
    }

    private void prepareView() {

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        tvDate.measure(widthSpec, heightSpec);
        tvDate.layout(0, 0, tvDate.getMeasuredWidth(), tvDate.getMeasuredHeight());

    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return tvDate.getWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        canvas.save();

        int padding = (bottom - top - tvDate.getBottom()) / 2;
        canvas.translate(x, bottom - tvDate.getBottom() - padding);
        tvDate.draw(canvas);

        canvas.restore();

    }

}
