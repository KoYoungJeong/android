package com.tosslab.jandi.app.ui.signup.verify;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;

/**
 * Created by tonyjs on 15. 6. 1..
 */
public class VerificationCodeView extends LinearLayout implements TextWatcher {

    public interface OnVerificationCodeChangeListener {
        public void onChanged();
    }

    public VerificationCodeView(Context context) {
        super(context);
        init();
    }

    public VerificationCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerificationCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private OnVerificationCodeChangeListener listener;

    public void setListener(OnVerificationCodeChangeListener listener) {
        this.listener = listener;
    }

    private EditText etInputCode;
    private int[] textViewResIds = new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4};

    private void init() {
        View root = inflate(getContext(), R.layout.item_sign_up_verification_code, this);
        etInputCode = (EditText) findViewById(R.id.et_code);
        etInputCode.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    private String verificationCode;

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        verificationCode = s.toString();
        addVerificationCode();
        if (listener != null) {
            listener.onChanged();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public String getVerificationCode() {
        return verificationCode;
    }

    private void addVerificationCode() {
        clear();

        char[] chars = verificationCode.toCharArray();
        int length = chars.length;

        for (int i = 0; i < length; i++) {
            char c = chars[i];
            TextView tv = (TextView) findViewById(textViewResIds[i]);
            tv.setText(Character.toString(c));
        }
    }

    private void clear() {
        for (int i = 0; i < textViewResIds.length; i++) {
            TextView tv = (TextView) findViewById(textViewResIds[i]);
            tv.setText("");
        }
    }
}
