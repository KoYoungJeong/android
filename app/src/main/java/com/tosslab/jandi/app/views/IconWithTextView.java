package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 */
@SuppressWarnings("ResourceType")
public class IconWithTextView extends RelativeLayout {

    public static final int UNDEFINED_VALUE = -1;
    private static final int[] ATTRS = new int[]{
            android.R.attr.text,
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvBadge;

    public IconWithTextView(Context context) {
        this(context, null);
    }

    public IconWithTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_icon_with_text, this);

        ivIcon = (ImageView) findViewById(R.id.img_icon_with_text);
        tvTitle = (TextView) findViewById(R.id.tv_icon_with_text);
        tvBadge = (TextView) findViewById(R.id.tv_icon_with_text_badge);

        initAttrs(context, attrs);
        setBadgeCount(0);
    }

    public ImageView getImageView() {
        return ivIcon;
    }

    private void initAttrs(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        if (a.hasValue(0)) {
            CharSequence text = a.getText(0);
            tvTitle.setText(text);
        }

        if (a.hasValue(1)) {
            int textSize = a.getDimensionPixelSize(1, UNDEFINED_VALUE);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        if (a.hasValue(2)) {
            int textColor = a.getColor(2, UNDEFINED_VALUE);
            tvTitle.setTextColor(textColor);
        }

        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.IconWithTextView);

        LayoutParams layoutParams = (LayoutParams) ivIcon.getLayoutParams();
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

        ivIcon.setLayoutParams(layoutParams);

        if (a.hasValue(R.styleable.IconWithTextView_img)) {
            Drawable src = a.getDrawable(R.styleable.IconWithTextView_img);
            ivIcon.setImageDrawable(src);
        }

        a.recycle();

    }

    public void setIconImageDrawalbe(Drawable drawable) {
        if (drawable != null) {
            ivIcon.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        ivIcon.setImageResource(resId);
    }

    public void setIconText(String text) {
        if (!TextUtils.isEmpty(text)) {
            tvTitle.setText(text);
        }
    }

    public int getTextLine() {
        return tvTitle.getLayout().getLineCount();
    }

    public CharSequence getText() {
        return tvTitle.getText();
    }

    public void setIconText(int textRes) {
        tvTitle.setText(textRes);
    }

    public void setBadgeCount(int count) {
        if (count <= 0) {
            tvBadge.setVisibility(View.GONE);
        } else if (count > 999) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(999));
        } else {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(count));
        }
    }

}
