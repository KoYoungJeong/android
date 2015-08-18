package com.tosslab.jandi.app.ui.selector.room;

import android.view.View;

import com.tosslab.jandi.app.lists.FormattedEntity;

public interface RoomSelector {

    void show(View roomView);

    void dismiss();

    void setOnRoomSelectListener(OnRoomSelectListener onRoomSelectListener);

    void setOnRoomDismissListener(OnRoomDismissListener onRoomDismissListener);

    interface OnRoomSelectListener {
        void onRoomSelect(FormattedEntity item);
    }

    interface OnRoomDismissListener {
        void onRoomDismiss();
    }

}
