package com.tosslab.jandi.app.ui.selector.user;

import android.view.View;

import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;

public interface UserSelector {

    void show(View roomView);

    void dismiss();

    void setOnUserSelectListener(OnUserSelectListener onRoomSelectListener);

    void setOnUserDismissListener(OnUserDismissListener onUserDismissListener);

    interface OnUserSelectListener {
        void onUserSelect(ExpandRoomData item);
    }

    interface OnUserDismissListener {
        void onUserDismiss();
    }

}

