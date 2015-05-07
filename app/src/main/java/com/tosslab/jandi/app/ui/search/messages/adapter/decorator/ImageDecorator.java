package com.tosslab.jandi.app.ui.search.messages.adapter.decorator;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.spannable.TypeImageSpannable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class ImageDecorator extends SearchTextDecorator {

    private ContentType contentType;
    private Context context;

    public ImageDecorator(TextDecorator textDecorator) {
        super(textDecorator);
    }

    @Override
    public void appendText() {
        TypeImageSpannable imageSpannable;
        switch (contentType) {
            case File:
                imageSpannable = new TypeImageSpannable(context, R.drawable.jandi_account_upload);
                break;
            case Shared:
                imageSpannable = new TypeImageSpannable(context, R.drawable.jandi_account_share);
                break;
            case Unshared:
                imageSpannable = new TypeImageSpannable(context, R.drawable.jandi_icon_add);
                break;
            case Comment:
                imageSpannable = new TypeImageSpannable(context, R.drawable.jandi_account_comment);
                break;
        }
    }

    public void setContentType(ContentType contentType) {

        this.contentType = contentType;
    }

    public void setContext(Context context) {

        this.context = context;
    }
}
