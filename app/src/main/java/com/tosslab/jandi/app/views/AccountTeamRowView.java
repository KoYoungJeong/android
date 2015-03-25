package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
public class AccountTeamRowView extends LinearLayout {

    private TextView nameTextView;
    private TextView badgeTextView;
    private ImageView iconImageView;

    public AccountTeamRowView(Context context) {
        this(context, null);
    }

    public AccountTeamRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
        initView(context, attrs);
    }

    private void inflateView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_account_home_team, this, true);
    }

    private void initView(Context context, AttributeSet attrs) {

        iconImageView = ((ImageView) findViewById(R.id.img_account_home_team_icon));
        nameTextView = (TextView) findViewById(R.id.txt_account_home_team_name);
        badgeTextView = (TextView) findViewById(R.id.txt_account_home_team_badge);
    }

    public void setTeamName(String name) {
        nameTextView.setText(name);
    }

    public void setBadgeCount(int badgeCount) {
        badgeTextView.setText(String.valueOf(badgeCount));
        if (badgeCount > 0) {
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }
    }

    public void setNameTextColor(int color) {
        nameTextView.setTextColor(color);
    }

    public void setNameTextColor(ColorStateList colorStateList) {
        nameTextView.setTextColor(colorStateList);
    }

    public void setIcon(int resId) {
        iconImageView.setImageResource(resId);
    }

}
