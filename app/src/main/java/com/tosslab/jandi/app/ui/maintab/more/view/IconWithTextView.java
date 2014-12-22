package com.tosslab.jandi.app.ui.maintab.more.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 */
public class IconWithTextView extends LinearLayout {

    public static final int UNDEFINED_VALUE = -1;
    private static final int[] ATTRS = new int[]{
            android.R.attr.text,
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    private ImageView imageView;
    private TextView textView;

    public IconWithTextView(Context context) {
        this(context, null);
    }

    public IconWithTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_icon_with_text, this);

        imageView = (ImageView) findViewById(R.id.img_icon_with_text);
        textView = (TextView) findViewById(R.id.tv_icon_with_text);

        initAttrs(context, attrs);
    }

    public ImageView getImageView() {
        return imageView;
    }

    private void initAttrs(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        if (a.hasValue(0)) {
            CharSequence text = a.getText(0);
            textView.setText(text);
        }

        if (a.hasValue(1)) {
            int textSize = a.getDimensionPixelSize(1, UNDEFINED_VALUE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        if (a.hasValue(2)) {
            int textColor = a.getColor(2, UNDEFINED_VALUE);
            textView.setTextColor(textColor);
        }

        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.IconWithTextView);

        LayoutParams layoutParams = (LayoutParams) imageView.getLayoutParams();
        if (a.hasValue(R.styleable.IconWithTextView_srcWidth)) {
            int srcWidth = a.getDimensionPixelSize(R.styleable.IconWithTextView_srcWidth, UNDEFINED_VALUE);
            layoutParams.width = srcWidth;
        }

        if (a.hasValue(R.styleable.IconWithTextView_srcHeight)) {
            int srcHeight = a.getDimensionPixelSize(R.styleable.IconWithTextView_srcHeight, UNDEFINED_VALUE);
            layoutParams.height = srcHeight;
        }

        if (a.hasValue(R.styleable.IconWithTextView_marginSrcText)) {
            int margin = a.getDimensionPixelSize(R.styleable.IconWithTextView_marginSrcText, UNDEFINED_VALUE);
            layoutParams.bottomMargin = margin;
        }
        imageView.setLayoutParams(layoutParams);

        if (a.hasValue(R.styleable.IconWithTextView_img)) {
            Drawable src = a.getDrawable(R.styleable.IconWithTextView_img);
            imageView.setImageDrawable(src);
        }


        a.recycle();

    }

    public void setIconImageDrawalbe(Drawable drawable) {
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        imageView.setImageResource(resId);
    }

    public void setIconText(String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
    }

    public void setIconText(int textRes) {
        textView.setText(textRes);
    }


}
