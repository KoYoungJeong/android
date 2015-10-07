package com.tosslab.jandi.app.ui.passcode.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 15. 10. 7..
 */
public class PassCodeAdapter extends RecyclerView.Adapter<PassCodeViewHolder> {
    private static final int CANCEL = 9;
    private static final int DELETE = 11;

    private Context context;

    public PassCodeAdapter(Context context) {
        this.context = context;
    }

    @Override
    public PassCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_passcode, parent, false);
        return new PassCodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PassCodeViewHolder holder, int position) {
        holder.tvCancel.setVisibility(View.VISIBLE);
        holder.tvCancel.setText(Integer.toString(position + 1));
    }

    @Override
    public int getItemCount() {
        return 12;
    }
}
