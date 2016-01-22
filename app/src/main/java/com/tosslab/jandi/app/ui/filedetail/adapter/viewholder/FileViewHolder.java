package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

/**
 * Created by tonyjs on 16. 1. 19..
 */
public class FileViewHolder extends BaseViewHolder<ResMessages.FileMessage> {

    private TextView tvUserName;
    private SimpleDraweeView ivUserProfile;
    private TextView tvCreatedTime;
    private View btnStar;


    public FileViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindView(ResMessages.FileMessage fileMessage) {
        bindFileInfo(fileMessage);

        bindFileContent(fileMessage);
    }

    public void bindFileContent(ResMessages.FileMessage fileMessage) {

    }

    protected void bindFileInfo(ResMessages.FileMessage fileMessage) {


    }
}
