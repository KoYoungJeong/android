package com.tosslab.jandi.app.ui.maintab.navigation.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class BadgeOverFlowMenu extends FrameLayout {
    private TextView tvBadge;

    public BadgeOverFlowMenu(Context context) {
        super(context);
        init();
    }

    public BadgeOverFlowMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BadgeOverFlowMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        View root = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_main_tab_overflow_menu, this, true);
        tvBadge = (TextView) root.findViewById(R.id.tv_badge);

        if (isInEditMode()) {
            return;
        }

        setMinimumWidth((int) UiUtils.getPixelFromDp(72));
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
