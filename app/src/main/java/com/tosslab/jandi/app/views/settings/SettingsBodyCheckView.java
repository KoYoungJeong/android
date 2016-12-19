package com.tosslab.jandi.app.views.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

public class SettingsBodyCheckView extends LinearLayout {

    private TextView tvTitle;
    private TextView tvSummary;
    private CheckBox cbCheck;

    public SettingsBodyCheckView(Context context) {
        this(context, null);
    }

    public SettingsBodyCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsBodyCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
        initAttrs(context, attrs);
    }

    private void initViews(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_settings_body_check, this, true);

        tvTitle = ((TextView) findViewById(R.id.tv_view_settings_body_check_title));
        tvSummary = ((TextView) findViewById(R.id.tv_view_settings_body_check_summary));
        cbCheck = ((CheckBox) findViewById(R.id.tv_view_settings_body_check_checkbox));
    }

    public boolean isChecked() {
        return cbCheck.isChecked();
    }

    public void setChecked(boolean checked) {
        cbCheck.setChecked(checked);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a;
        CharSequence summary;
        {
            a = context.obtainStyledAttributes(attrs, R.styleable.SettingsBodyView);
            CharSequence title = a.getText(R.styleable.SettingsBodyView_sbvTitle);
            summary = a.getText(R.styleable.SettingsBodyView_sbvSummary);

            setTitle(title);
            setSummary(summary);

            a.recycle();
        }

        {
            a = context.obtainStyledAttributes(attrs, R.styleable.SettingsBodyCheckView);
            boolean checked = a.getBoolean(R.styleable.SettingsBodyCheckView_sbvChcked, false);

            setChecked(checked);
            a.recycle();
        }

        if (!TextUtils.isEmpty(summary)) {
            tvSummary.setText(summary);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_two_item));
        } else {
            tvSummary.setVisibility(View.GONE);
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.jandi_settings_text_body_height_one_item));
        }

        a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.enabled});
        if (a.hasValue(0)) {
            boolean enabled = a.getBoolean(0, true);
            setEnabled(enabled);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tvTitle.setEnabled(enabled);
        tvSummary.setEnabled(enabled);
        cbCheck.setEnabled(enabled);
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
