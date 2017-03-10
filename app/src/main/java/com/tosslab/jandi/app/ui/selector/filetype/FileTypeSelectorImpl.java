package com.tosslab.jandi.app.ui.selector.filetype;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.utils.SdkUtils;

public class FileTypeSelectorImpl implements FileTypeSelector {

    private PopupWindow popupWindow;
    private OnFileTypeSelectListener onFileTypeSelectListener;
    private OnFileTypeDismissListener onFileTypeDismissListener;

    @Override
    public void show(View roomView) {

        dismiss();

        Context context = roomView.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_file_type_selector, null);

        popupWindow = new PopupWindow(rootView);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_file_type_selector);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        FileTypeRecyclerAdapter adapter = new FileTypeRecyclerAdapter(context);

        adapter.setOnFileTypeSelectListener(onFileTypeSelectListener);
        recyclerView.setAdapter(adapter);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onFileTypeDismissListener != null) {
                    onFileTypeDismissListener.onFileTypeDimiss();
                }
            }
        });
        if (SdkUtils.isOverNougat()) {
            int[] a = new int[2];
            roomView.getLocationInWindow(a);
            popupWindow.showAtLocation(((Activity) roomView.getContext()).getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, a[1] + roomView.getHeight());
        } else {
            PopupWindowCompat.showAsDropDown(popupWindow, roomView, 0, 0, Gravity.TOP | Gravity.LEFT);
        }

    }

    @Override
    public void dismiss() {

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

    }

    @Override
    public void setOnFileTypeSelectListener(OnFileTypeSelectListener onFileTypeSelectListener) {
        this.onFileTypeSelectListener = onFileTypeSelectListener;
    }

    @Override
    public void setOnFileTypeDismissListener(OnFileTypeDismissListener onFileTypeDismissListener) {
        this.onFileTypeDismissListener = onFileTypeDismissListener;
    }

    private static class FileTypeRecyclerAdapter extends RecyclerView.Adapter<FileTypeViewHolder> {
        private final Context context;
        private OnFileTypeSelectListener onFileTypeSelectListener;

        public FileTypeRecyclerAdapter(Context context) {

            this.context = context;
        }

        @Override
        public FileTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_file_select, parent, false);

            FileTypeViewHolder viewHolder = new FileTypeViewHolder(itemView);

            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_file_selector_item_name);
            viewHolder.ivIcon = (ImageView) itemView.findViewById(R.id.iv_file_selector_item_icon);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FileTypeViewHolder holder, int position) {

            holder.ivIcon.setImageResource(CategorizedMenuOfFileType.drawableResourceList[position]);
            holder.tvName.setText(CategorizedMenuOfFileType.stringTitleResourceList[position]);


            holder.itemView.setOnClickListener(v -> {
                if (onFileTypeSelectListener != null) {
                    onFileTypeSelectListener.onFileTypeSelect(position);
                }
            });

        }

        @Override
        public int getItemCount() {
            return CategorizedMenuOfFileType.stringTitleResourceList.length;
        }

        public void setOnFileTypeSelectListener(OnFileTypeSelectListener onFileTypeSelectListener) {
            this.onFileTypeSelectListener = onFileTypeSelectListener;
        }
    }

    private static class FileTypeViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public ImageView ivIcon;

        public FileTypeViewHolder(View itemView) {
            super(itemView);
        }
    }
}
