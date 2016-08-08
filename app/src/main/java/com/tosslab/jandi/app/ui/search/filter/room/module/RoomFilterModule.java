package com.tosslab.jandi.app.ui.search.filter.room.module;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.RoomFilterAdapter;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.model.RoomFilterDataModel;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.view.RoomFilterDataView;
import com.tosslab.jandi.app.ui.search.filter.room.model.RoomFilterModel;
import com.tosslab.jandi.app.ui.search.filter.room.presenter.RoomFilterPresenter;
import com.tosslab.jandi.app.ui.search.filter.room.presenter.RoomFilterPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
@Module
public class RoomFilterModule {

    private RoomFilterAdapter roomFilterAdapter;
    private RoomFilterPresenter.View roomFilterView;

    public RoomFilterModule(RoomFilterAdapter roomFilterAdapter,
                            RoomFilterPresenter.View roomFilterView) {
        this.roomFilterAdapter = roomFilterAdapter;
        this.roomFilterView = roomFilterView;
    }

    @Provides
    public InputMethodManager providesInputMethodManager() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) JandiApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager;
    }

    @Provides
    public RoomFilterDataModel providesRoomFilterDataModel() {
        return roomFilterAdapter;
    }

    @Provides
    public RoomFilterDataView providesRoomFilterDataView() {
        return roomFilterAdapter;
    }

    @Provides
    public RoomFilterModel providesRoomFilterModel() {
        return new RoomFilterModel();
    }

    @Provides
    public RoomFilterPresenter.View providesRoomFilterView() {
        return roomFilterView;
    }

    @Provides
    public RoomFilterPresenter providesRoomFilterPresenter(RoomFilterPresenterImpl roomFilterPresenter) {
        return roomFilterPresenter;
    }

}
