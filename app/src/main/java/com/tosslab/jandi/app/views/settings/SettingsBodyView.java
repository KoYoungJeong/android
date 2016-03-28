package com.tosslab.jandi.app.views.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class SettingsBodyView extends LinearLayout {

    private TextView tvTitle;
    private TextView tvSummary;

    public SettingsBodyView(Context context) {
        this(context, null);
    }

    public SettingsBodyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsBodyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
        initAttrs(context, attrs);
    }

    public SettingsBodyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context);
        initAttrs(context, attrs);
    }

    private void initViews(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_settings_body, this, true);

        tvTitle = ((TextView) findViewById(R.id.tv_view_settings_body_title));
        tvSummary = ((TextView) findViewById(R.id.tv_view_settings_body_summary));

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingsBodyView);

        CharSequence title = a.getText(R.styleable.SettingsBodyView_sbvTitle);
        CharSequence summary = a.getText(R.styleable.SettingsBodyView_sbvSummary);
        ColorStateList summaryColor = a.getColorStateList(R.styleable.SettingsBodyView_sbvSummaryColor);

        setTitle(title);

        setSummary(summary);
        setSummaryColor(summaryColor);


        if (!TextUtils.isEmpty(summary)) {
            tvSummary.setText(summary);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_two_item));
        } else {
            tvSummary.setVisibility(View.GONE);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_one_item));
        }

        a.recycle();

    }

    public void setSummaryColor(ColorStateList summaryColor) {
        if (summaryColor != null) {
            tvSummary.setTextColor(summaryColor);
        }
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    public void setTitle(int titleResId) {
        setTitle(getResources().getText(titleResId));
    }

    public String getTitle() {
        return !TextUtils.isEmpty(tvTitle.getText()) ? tvTitle.getText().toString() : "";
    }

    public void setSummary(CharSequence summary) {
        if (!TextUtils.isEmpty(summary)) {
            tvSummary.setVisibility(View.VISIBLE);
            tvSummary.setText(summary);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_two_item));
        } else {
            tvSummary.setVisibility(View.GONE);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_one_item));
        }

        requestLayout();
    }

    public String getSummary() {
        return !TextUtils.isEmpty(tvSummary.getText()) ? tvSummary.getText().toString() : "";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tvTitle.setEnabled(enabled);
        tvSummary.setEnabled(enabled);
    }

    public void setSummary(int summaryResId) {
        setSummary(getResources().getText(summaryResId));
    }
}
