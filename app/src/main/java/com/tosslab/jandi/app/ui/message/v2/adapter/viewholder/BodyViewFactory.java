package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class BodyViewFactory {

    public static BodyViewHolder createViewHolder(int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case File:
                return new FileViewHolder();
            case Image:
                return new ImageViewHolder();
            case PureComment:
                return new PureCommentViewHolder();
            case FileComment:
                return new FileCommentViewHolder();
            case Dummy:
                return new DummyViewHolder();
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
            case Message:
            default:
                return R.id.message_msg;
        }
    }

}
