package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 20..
 */
public class MensionCommentSpannable extends ReplacementSpan {

    private TextView tvDate;

    public MensionCommentSpannable(Context context, String date) {

        super();

        tvDate = new TextView(context);
        tvDate.setText(date);
        tvDate.setTextColor(0xFF00a6e9);
        tvDate.setBackgroundColor(0xFFdaf2ff);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources()
                .getDimensionPixelSize(R.dimen.jandi_mention_comment_item_font_size));

        prepareView();
        Log.e("ss", tvDate.getText().toString());
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
