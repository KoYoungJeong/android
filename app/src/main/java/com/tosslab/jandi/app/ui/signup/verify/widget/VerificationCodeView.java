package com.tosslab.jandi.app.ui.signup.verify.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 15. 6. 1..
 */
public class VerificationCodeView extends LinearLayout
        implements TextWatcher, TextView.OnEditorActionListener {

    private OnVerificationCodeChangeListener listener;
    private EditText etInputCode;
    private TextView[] arrTvCode;
    private int colorValid;
    private int colorInvalid;
    private OnActionDoneListener onActionDoneListener;
    private String verificationCode;

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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (onActionDoneListener != null) {
                onActionDoneListener.onActionDone();
            }
            return true;
        }
        return false;
    }

    public void setOnActionDoneListener(OnActionDoneListener onActionDoneListener) {
        this.onActionDoneListener = onActionDoneListener;
    }

    public void setOnVerificationCodeChangedListener(OnVerificationCodeChangeListener listener) {
        this.listener = listener;
    }

    private void init() {
        colorValid = getContext().getResources().getColor(R.color.jandi_signup_valid);
        colorInvalid = getContext().getResources().getColor(R.color.jandi_signup_invalid);

        inflate(getContext(), R.layout.item_sign_up_verification_code, this);
        etInputCode = (EditText) findViewById(R.id.et_code);
        etInputCode.addTextChangedListener(this);

        arrTvCode = new TextView[]{
                (TextView) findViewById(R.id.tv_verification_code_1),
                (TextView) findViewById(R.id.tv_verification_code_2),
                (TextView) findViewById(R.id.tv_verification_code_3),
                (TextView) findViewById(R.id.tv_verification_code_4)};
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        verificationCode = s.toString();

        addVerificationCode();
        if (listener != null) {
            listener.onChanged();
        }
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
            TextView tv = arrTvCode[i];
            tv.setText(Character.toString(c));
        }
    }

    private void clear() {
        for (int i = 0; i < arrTvCode.length; i++) {
            TextView tv = arrTvCode[i];
            tv.setText("");
            tv.setTextColor(colorValid);
        }
    }

    public void clearAll() {
        etInputCode.setText("");
    }

    public void setTextColorInvalidate() {
        setTextColor(colorInvalid);
    }

    public void setTextColorValidate() {
        setTextColor(colorValid);
    }

    private void setTextColor(int color) {
        for (int i = 0; i < arrTvCode.length; i++) {
            TextView tv = arrTvCode[i];
            tv.setTextColor(color);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnActionDoneListener {
        void onActionDone();
    }

    public interface OnVerificationCodeChangeListener {
        void onChanged();
    }
}
