package com.tosslab.jandi.app.ui.search.filter.room.component;

import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.search.filter.room.module.RoomFilterModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
@Component(modules = {RoomFilterModule.class})
public interface RoomFilterComponent {
    void inject(RoomFilterActivity activity);
}
