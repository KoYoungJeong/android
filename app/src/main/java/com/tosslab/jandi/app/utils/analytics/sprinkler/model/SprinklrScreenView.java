package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IScreenView;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrScreenView extends MainSprinklrModel
        implements IScreenView {

    private SprinklrScreenView() {
        super(SprinklerEvents.ScreenView, true, true);
    }

    public static void sendLog(int property) {
        new SprinklrScreenView()
                .setScreenView(property)
                .send();
    }

    @Override
    public MainSprinklrModel setScreenView(int property) {
        setProperty(PropertyKey.ScreenView, property);
        return this;
    }
}
