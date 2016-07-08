package com.tosslab.jandi.app.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

/**
 * Created by tonyjs on 16. 7. 7..
 */
public class BadgeTextView extends TextView {

    public BadgeTextView(Context context) {
        super(context);
        init();
    }

    public BadgeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
         addTextChangedListener(new SimpleTextWatcher(){
             @Override
             public void afterTextChanged(Editable s) {
                 if (TextUtils.isEmpty(s)) {
                     return;
                 }

                 removeTextChangedListener(this);

                 int textLength = s.length();

                 ViewGroup.MarginLayoutParams marginLayoutParams =
                         (ViewGroup.MarginLayoutParams) getLayoutParams();
                 int leftMargin;
                 if (textLength >= 3) {
                     leftMargin = (int) -UiUtils.getPixelFromDp(14);
                 } else if (textLength >= 2) {
                     leftMargin = (int) -UiUtils.getPixelFromDp(11);
                 } else {
                     leftMargin = (int) -UiUtils.getPixelFromDp(8);
                 }
                 marginLayoutParams.leftMargin = leftMargin;
                 setLayoutParams(marginLayoutParams);

                 addTextChangedListener(this);
             }
         });
    }
}
