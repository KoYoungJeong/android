package com.tosslab.jandi.app.views.profile;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileLabelView extends LinearLayout {

    @Bind(R.id.tv_profile_label_title)
    TextView tvTitle;
    @Bind(R.id.tv_profile_label_content)
    TextView tvContent;

    public ProfileLabelView(Context context) {
        this(context, null);
    }

    public ProfileLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_profile_label, this);
        ButterKnife.bind(this, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfileLabelView);

        int textSizeTitle = typedArray.getDimensionPixelSize(R.styleable.ProfileLabelView_textSizeTitle, -1);
        int textColorTitle = typedArray.getColor(R.styleable.ProfileLabelView_textColorTitle, -1);
        String textTitle = typedArray.getString(R.styleable.ProfileLabelView_textTitle);

        int textSizeContent = typedArray.getDimensionPixelSize(R.styleable.ProfileLabelView_textSizeContent, -1);
        int textColorContent = typedArray.getColor(R.styleable.ProfileLabelView_textColorContent, -1);
        String textContent = typedArray.getString(R.styleable.ProfileLabelView_textContent);

        int gap = typedArray.getDimensionPixelSize(R.styleable.ProfileLabelView_gap, -1);

        boolean contentSingleline = typedArray.getBoolean(R.styleable.ProfileLabelView_textContentSingleline, false);

        if (textSizeTitle > 0) {
            setTextSizeTitle(TypedValue.COMPLEX_UNIT_PX, textSizeTitle);
        }

        if (textSizeContent > 0) {
            setTextSizeContent(TypedValue.COMPLEX_UNIT_PX, textSizeContent);
        }

        if (textColorTitle > 0) {
            setTextColorTitle(textColorTitle);
        }

        if (textColorContent > 0) {
            setTextColorContent(textColorContent);
        }

        if (gap > 0) {
            setTextGap(gap);
        }

        setContentSingleLine(contentSingleline);

        setTextTitle(textTitle);
        setTextContent(textContent);

        typedArray.recycle();
    }

    private void setContentSingleLine(boolean contentSingleline) {
        tvContent.setSingleLine(contentSingleline);
        if (contentSingleline) {
            tvContent.setMaxLines(1);
            tvContent.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            tvContent.setMaxLines(Integer.MAX_VALUE);
            tvContent.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    public void setTextGap(int gap) {
        LayoutParams layoutParams = (LayoutParams) tvContent.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, gap, layoutParams.rightMargin, layoutParams.bottomMargin);

        tvContent.setLayoutParams(layoutParams);
    }

    public void setTextColorContent(int textColor) {
        tvContent.setTextColor(textColor);
    }

    public void setTextColorTitle(int textColor) {
        tvTitle.setTextColor(textColor);
    }

    public void setTextSizeContent(int unit, int textSize) {
        tvContent.setTextSize(unit, textSize);
    }

    public void setTextSizeTitle(int unit, int textSize) {
        tvTitle.setTextSize(unit, textSize);
    }

    public void setTextTitle(String label) {
        tvTitle.setText(label);

    }

    public void setTextTitle(int resId) {
        tvTitle.setText(resId);
    }

    public void setTextContent(String content) {
        tvContent.setText(content);
    }

    public void setTextContent(int resId) {
        tvContent.setText(resId);
    }
}
