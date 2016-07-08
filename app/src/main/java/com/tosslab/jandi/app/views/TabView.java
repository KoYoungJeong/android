package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by tonyjs on 16. 7. 8..
 */
public class TabView extends FrameLayout {
    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvBadge;

    public TabView(Context context) {
        super(context);
        init(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_tab, this, true);
        ivIcon = (ImageView) root.findViewById(R.id.tab_icon);
        tvTitle = (TextView) root.findViewById(R.id.tab_title);
        tvBadge = (TextView) root.findViewById(R.id.tab_badge);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabView);

        String title = a.getString(R.styleable.TabView_tabTitle);
        tvTitle.setText(title);

        int iconResId = a.getResourceId(R.styleable.TabView_tabIcon, -1);
        if (iconResId != -1) {
            ivIcon.setImageResource(iconResId);
        }
    }

    public void setTitle(int resId) {
        tvTitle.setText(resId);
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public void setImageResource(int resId) {
        ivIcon.setImageResource(resId);
    }

    public void hideBadge() {
        tvBadge.setVisibility(View.GONE);
    }

    public void showBadge() {
        tvBadge.setVisibility(View.VISIBLE);
    }

    public void setBadgeText(String badgeText) {
        if (TextUtils.isEmpty(badgeText)) {
            return;
        }

        int textLength = badgeText.length();

        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) tvBadge.getLayoutParams();
        int leftMargin;
        if (textLength >= 3) {
            leftMargin = (int) -UiUtils.getPixelFromDp(14);
        } else if (textLength >= 2) {
            leftMargin = (int) -UiUtils.getPixelFromDp(11);
        } else {
            leftMargin = (int) -UiUtils.getPixelFromDp(8);
        }
        marginLayoutParams.leftMargin = leftMargin;
        tvBadge.setLayoutParams(marginLayoutParams);

        tvBadge.setText(badgeText);
    }
}
