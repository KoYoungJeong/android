package com.tosslab.jandi.app.ui.selector.room;

import android.view.View;

public interface RoomSelector {

    void show(View roomView);

    void dismiss();

    void setOnRoomSelectListener(OnRoomSelectListener onRoomSelectListener);

    void setOnRoomDismissListener(OnRoomDismissListener onRoomDismissListener);

    interface OnRoomSelectListener {
        void onRoomSelect(RoomSelectorImpl.ExpandRoomData item);
    }

    interface OnRoomDismissListener {
        void onRoomDismiss();
    }

}
