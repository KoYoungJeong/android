package com.tosslab.jandi.app.ui.signup.account;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.PasswordChecker;

/**
 * Created by justinygchoi on 14. 12. 15..
 */
public class PasswordStrengthBarometerView extends LinearLayout {
    Context mContext;
    View viewBarometerWeak;
    View viewBarometerAverage;
    View viewBarometerSafe;
    View viewBarometerStrong;
    TextView textViewMessage;

    public PasswordStrengthBarometerView(Context context) {
        super(context);
        initView(context);
    }

    public PasswordStrengthBarometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PasswordStrengthBarometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater li
                = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.view_password_strength_barometer, this, false);
        addView(v);
        viewBarometerWeak = findViewById(R.id.view_strength_barometer_weak);
        viewBarometerAverage = findViewById(R.id.view_strength_barometer_average);
        viewBarometerSafe = findViewById(R.id.view_strength_barometer_safe);
        viewBarometerStrong = findViewById(R.id.view_strength_barometer_strong);
        textViewMessage = (TextView) findViewById(R.id.txt_strength_barometer);
    }

    public void setStrengthBarometer(int level) {
        setBarometerVisibility(level);
        setBarometerColor(level);

        textViewMessage.setTextColor(
                mContext.getResources().getColor(
                        PasswordChecker.getBarometerColorRes(level)));
        textViewMessage.setText(PasswordChecker.getBarometerStringRes(level));
    }


    private void setBarometerColor(int level) {
        int colorRes = PasswordChecker.getBarometerColorRes(level);
        switch (level) {
            case PasswordChecker.STRONG:
                viewBarometerStrong.setBackgroundResource(colorRes);
            case PasswordChecker.SAFE:
                viewBarometerSafe.setBackgroundResource(colorRes);
            case PasswordChecker.AVERAGE:
                viewBarometerAverage.setBackgroundResource(colorRes);
            case PasswordChecker.WEAK:
            case PasswordChecker.TOO_SHORT:
                viewBarometerWeak.setBackgroundResource(colorRes);
                break;
        }
    }

    private void setBarometerVisibility(int level) {
        viewBarometerWeak.setVisibility((level >= PasswordChecker.TOO_SHORT) ? VISIBLE : INVISIBLE);
        viewBarometerAverage.setVisibility((level >= PasswordChecker.AVERAGE) ? VISIBLE : INVISIBLE);
        viewBarometerSafe.setVisibility((level >= PasswordChecker.SAFE) ? VISIBLE : INVISIBLE);
        viewBarometerStrong.setVisibility((level >= PasswordChecker.STRONG) ? VISIBLE : INVISIBLE);
    }
}
