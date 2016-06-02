package com.tosslab.jandi.app.ui.base.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tonyjs on 16. 1. 19..
 * 뷰를 그리는 것 자체에 대한 집중을 위해 onBindView 메소드를 추상화,
 * RecyclerView.Adapter class 에서는 성격에 맞게 상황에 맞는 뷰를 로드하는 것에 집중하고
 * 실제 그리는 작업은 RecyclerView.Adapter.onBindViewHolder 에서 BaseViewHolder.onBindView 호출
 *
 * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
 */
public abstract class BaseViewHolder<ITEM> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindView(ITEM item);

    public void onViewDetachedFromWindow() {
    }
}
