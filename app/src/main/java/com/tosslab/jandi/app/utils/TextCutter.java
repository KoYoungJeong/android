package com.tosslab.jandi.app.utils;

import android.support.annotation.VisibleForTesting;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tonyjs on 16. 1. 6..
 */
public class TextCutter {
    public static final String TAG = TextCutter.class.getSimpleName();

    public static final int MAX_TEXT_LENGTH = 5000;
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

    @VisibleForTesting
    static class MaxLengthTextWatcher implements TextWatcher {
        private TextView textView;
        private int maxLength = MAX_TEXT_LENGTH;
        private boolean autoCut = true;
        private OnMaxTextLengthReachedListener onMaxTextLengthReachedListener;

        @VisibleForTesting
        MaxLengthTextWatcher() {

        }

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

                if (!autoCut) {
                    return;
                }

                textView.removeTextChangedListener(this);

                CharSequence text = cutText(s);
                textView.setText(text);

                setCursorIfNeed();
                dismissDropDownIfNeed();

                textView.addTextChangedListener(this);
            }
        }

        @VisibleForTesting
        CharSequence cutText(CharSequence text) {
            if (TextUtils.isEmpty(text)) {
                LogUtil.i(TAG, "text is empty");
                return "";
            }

            if (text.length() <= maxLength) {
                LogUtil.i(TAG, stringForLog(text));
                return text;
            }

            CharSequence result = text.subSequence(0, maxLength);
            Pattern pattern = Pattern.compile("(?:@)(.+)");
            Matcher matcher = pattern.matcher(result);

            int start = 0;
            int end = 0;
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
            }
            if (end == maxLength) {
                result = result.subSequence(0, start);
            }

            LogUtil.i(TAG, String.format("%s\n%s", stringForLog(text), stringForLog(result)));
            return result;
        }

        private String stringForLog(CharSequence text) {
            return text.toString().replace(" ", "&npsp;");
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
