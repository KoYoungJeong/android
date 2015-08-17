package com.tosslab.jandi.app.ui.selector.room;

import android.view.View;

import com.tosslab.jandi.app.lists.FormattedEntity;

public interface RoomSelector {

    void show(View roomView);

    void dismiss();

    void setOnRoomSelectListener(OnRoomSelectListener onRoomSelectListener);

    interface OnRoomSelectListener {
        void onRoomSelect(FormattedEntity item);
    }

}
