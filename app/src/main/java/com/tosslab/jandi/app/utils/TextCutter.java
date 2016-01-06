package com.tosslab.jandi.app.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 16. 1. 6..
 */
public class TextCutter {
    public static final String TAG = TextCutter.class.getSimpleName();

    public static final int MAX_TEXT_LENGTH = 20;
    private MaxLengthTextWatcher maxLengthTextWatcher;

    private TextCutter(TextView textView) {
        maxLengthTextWatcher = new MaxLengthTextWatcher(textView);
        // 중복 방지
        textView.removeTextChangedListener(maxLengthTextWatcher);
        textView.addTextChangedListener(maxLengthTextWatcher);
    }

    public static TextCutter with(TextView textView) {
        return new TextCutter(textView);
    }

    public TextCutter maxLength(int maxLength) {
        maxLengthTextWatcher.setMaxLength(maxLength);
        return this;
    }

    public TextCutter autoCut(boolean autoCut) {
        maxLengthTextWatcher.setAutoCut(autoCut);
        return this;
    }

    public TextCutter listener(OnMaxTextLengthReachedListener listener) {
        maxLengthTextWatcher.setOnMaxTextLengthReachedListener(listener);
        return this;
    }

    public interface OnMaxTextLengthReachedListener {
        void onReached(CharSequence text);
    }

    private static class MaxLengthTextWatcher implements TextWatcher {
        private TextView textView;
        private int maxLength = MAX_TEXT_LENGTH;
        private boolean autoCut = true;
        private OnMaxTextLengthReachedListener onMaxTextLengthReachedListener;

        public MaxLengthTextWatcher(TextView textView) {
            this.textView = textView;
        }

        public void setOnMaxTextLengthReachedListener(
                OnMaxTextLengthReachedListener onMaxTextLengthReachedListener) {
            this.onMaxTextLengthReachedListener = onMaxTextLengthReachedListener;
        }

        public void setAutoCut(boolean autoCut) {
            this.autoCut = autoCut;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int textLength = s.length();
            LogUtil.d(TAG, String.format("onTextChanged - length(%d)", textLength));
            if (textLength > maxLength) {
                onMaxTextLengthReachedListener.onReached(s);
                if (autoCut) {
                    textView.removeTextChangedListener(this);

                    CharSequence subSequence = s.subSequence(0, maxLength - 1);
                    textView.setText(subSequence);

                    setCursorIfNeed();
                    dismissDropDownIfNeed();

                    textView.addTextChangedListener(this);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        private void setCursorIfNeed() {
            if (textView instanceof EditText) {
                EditText editText = (EditText) this.textView;
                editText.setSelection(maxLength - 1);
            }
        }

        private void dismissDropDownIfNeed() {
            if (textView instanceof AutoCompleteTextView) {
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textView;
                if (autoCompleteTextView.isPopupShowing()) {
                    autoCompleteTextView.dismissDropDown();
                }
            }
        }

        // 중복 방지
        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof MaxLengthTextWatcher;
        }
    }

}
