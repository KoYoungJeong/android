package com.tosslab.jandi.app.ui.selector.filetype;

import android.view.View;

public interface FileTypeSelector {
    void show(View roomView);

    void dismiss();

    void setOnFileTypeSelectListener(OnFileTypeSelectListener onFileTypeSelectListener);

    void setOnFileTypeDismissListener(OnFileTypeDismissListener onFileTypeDismissListener);

    interface OnFileTypeSelectListener {
        void onFileTypeSelect(int position);
    }

    interface OnFileTypeDismissListener {
        void onFileTypeDimiss();
    }


}
