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

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static final int CANCEL = 9;
    public static final int DELETE = 11;

    private Context context;
    private OnItemClickListener onItemClickListener;
    private boolean showCancel = false;

    public PassCodeAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public PassCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_passcode, parent, false);
        return new PassCodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PassCodeViewHolder holder, final int position) {
        if (position == CANCEL) {
            holder.ivNumber.setVisibility(View.INVISIBLE);
            holder.tvCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        } else {
            holder.ivNumber.setVisibility(View.VISIBLE);
            holder.tvCancel.setVisibility(View.INVISIBLE);
            holder.ivNumber.setImageResource(getNumberResId(position));
        }
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    public void showCancel(boolean showCancel) {
        this.showCancel = showCancel;
    }

    private int getNumberResId(int position) {
        switch (position) {
            case 0:
                return R.drawable.btn_numberpad_1;
            case 1:
                return R.drawable.btn_numberpad_2;
            case 2:
                return R.drawable.btn_numberpad_3;
            case 3:
                return R.drawable.btn_numberpad_4;
            case 4:
                return R.drawable.btn_numberpad_5;
            case 5:
                return R.drawable.btn_numberpad_6;
            case 6:
                return R.drawable.btn_numberpad_7;
            case 7:
                return R.drawable.btn_numberpad_8;
            case 8:
                return R.drawable.btn_numberpad_9;
            case 10:
                return R.drawable.btn_numberpad_0;
            case DELETE:
                return R.drawable.btn_numberpad_del;
        }
        return 0;
    }

}
