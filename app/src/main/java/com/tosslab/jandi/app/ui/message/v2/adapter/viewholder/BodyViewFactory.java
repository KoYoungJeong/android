package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class BodyViewFactory {

    public static BodyViewHolder createViewHolder(int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case File:
                return FileViewHolder.createFileViewHolder();
            case Image:
                return new ImageViewHolder();
            case PureComment:
                return new PureCommentViewHolder();
            case FileComment:
                return new FileCommentViewHolder();
            case Dummy:
                return new DummyViewHolder();
            case Event:
                return new EventViewHolder();
            case Message:
            default:
                return new MessageViewHolder();
        }
    }

    public static int getViewHolderId(int viewType) {
        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case File:
                return R.id.message_file;
            case Image:
                return R.id.message_img;
            case PureComment:
                return R.id.message_cmt_without_file;
            case FileComment:
                return R.id.message_cmt_with_file;
            case Dummy:
                return R.id.message_dummy;
            case Event:
                return R.id.message_event;
            case Message:
            default:
                return R.id.message_msg;
        }
    }

    public static View createItemView(Context context, ViewGroup parent, int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        int layoutId = 0;

        switch (type) {
            case Message:
                break;
            case File:
                break;
            case Image:
                break;
            case PureComment:
                break;
            case FileComment:
                break;
            case Dummy:
                break;
            case Event:
                break;
        }

        return null;
    }
}
