package com.tosslab.jandi.app.ui.passcode.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 15. 10. 7..
 */
public class PassCodeViewHolder  extends RecyclerView.ViewHolder {

    public TextView tvCancel;
    public ImageView ivNumber;

    public PassCodeViewHolder(View itemView) {
        super(itemView);
        tvCancel = (TextView) itemView.findViewById(R.id.tv_item_passcode_cancel);
        ivNumber = (ImageView) itemView.findViewById(R.id.iv_item_passcode_number);
    }
}
