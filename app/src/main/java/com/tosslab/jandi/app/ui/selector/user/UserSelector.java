package com.tosslab.jandi.app.ui.selector.user;

import android.view.View;

import com.tosslab.jandi.app.lists.FormattedEntity;

public interface UserSelector {

    void show(View roomView);

    void dismiss();

    void setOnUserSelectListener(OnUserSelectListener onRoomSelectListener);

    void setOnUserDismissListener(OnUserDismissListener onUserDismissListener);

    interface OnUserSelectListener {
        void onUserSelect(FormattedEntity item);
    }

    interface OnUserDismissListener {
        void onUserDismiss();
    }

}

