package com.tosslab.jandi.app.ui.signup.verify;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 15. 6. 1..
 */
public class VerificationCodeView extends LinearLayout
        implements TextWatcher, TextView.OnEditorActionListener {

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
    private TextView[] arrTvCode;
    private InputMethodManager inputMethodManager;

    private int colorValid;
    private int colorInvalid;

    private void init() {
        inputMethodManager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        colorValid = getContext().getResources().getColor(R.color.jandi_signup_valid);
        colorInvalid = getContext().getResources().getColor(R.color.jandi_signup_invalid);

        inflate(getContext(), R.layout.item_sign_up_verification_code, this);
        etInputCode = (EditText) findViewById(R.id.et_code);
        etInputCode.addTextChangedListener(this);
        etInputCode.setOnEditorActionListener(this);

        arrTvCode = new TextView[]{
                (TextView) findViewById(R.id.text1),
                (TextView) findViewById(R.id.text2),
                (TextView) findViewById(R.id.text3),
                (TextView) findViewById(R.id.text4)};
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideKeyboard();
            return true;
        }
        return false;
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

    public void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(
                etInputCode.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
