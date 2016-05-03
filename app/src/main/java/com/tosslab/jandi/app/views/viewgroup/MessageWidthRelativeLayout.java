package com.tosslab.jandi.app.views.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.tosslab.jandi.app.R;

public class MessageWidthRelativeLayout extends RelativeLayout {

    private static final String TAG = "MessageRelativeLayout";
    boolean first = true;
    private int boundWidth;
    private int newWidthMeasureSpec;

    public MessageWidthRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBoundWidth(context, attrs);
    }

    public MessageWidthRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBoundWidth(context, attrs);
    }

    public MessageWidthRelativeLayout(Context context) {
        super(context);
    }

    private void initBoundWidth(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessageWidthRelativeLayout);
        boundWidth = typedArray.getDimensionPixelSize(R.styleable.MessageWidthRelativeLayout_boundWidth, 0);


        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (first) {

            int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
            Log.d(TAG, "onMeasure: measureWidth/ " + measureWidth);
            int newMeasureWidth = measureWidth - boundWidth;
            Log.d(TAG, "onMeasure: newMeasureWidth/ " + newMeasureWidth);
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(newMeasureWidth, MeasureSpec.getMode(widthMeasureSpec));
            first = false;
        }
        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec);
    }
}
