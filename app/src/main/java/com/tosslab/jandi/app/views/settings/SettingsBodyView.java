package com.tosslab.jandi.app.views.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

public class SettingsBodyView extends LinearLayout {

    private TextView tvTitle;
    private TextView tvSummary;

    public SettingsBodyView(Context context) {
        this(context, null);
    }

    public SettingsBodyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initViews(context);
        initAttrs(context, attrs);
    }

    public SettingsBodyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
        initAttrs(context, attrs);
    }

    private void initViews(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_settings_body, this, true);

        tvTitle = ((TextView) findViewById(R.id.tv_view_settings_body_title));
        tvSummary = ((TextView) findViewById(R.id.tv_view_settings_body_summary));

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingsBodyView);

        CharSequence title = a.getText(R.styleable.SettingsBodyView_sbvTitle);
        CharSequence summary = a.getText(R.styleable.SettingsBodyView_sbvSummary);

        setTitle(title);

        setSummary(summary);

        if (!TextUtils.isEmpty(summary)) {
            tvSummary.setText(summary);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_two_item));
        } else {
            tvSummary.setVisibility(View.GONE);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_one_item));
        }

        a.recycle();
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }
    public void setTitle(int titleResId) {
        setTitle(getResources().getText(titleResId));
    }

    public void setSummary(CharSequence summary) {
        if (!TextUtils.isEmpty(summary)) {
            tvSummary.setText(summary);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_two_item));
        } else {
            tvSummary.setVisibility(View.GONE);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_one_item));
        }

        requestLayout();
    }

    public void setSummary(int summaryResId) {
        setSummary(getResources().getText(summaryResId));
    }
}
