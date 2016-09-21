package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ISearchKeyword;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrMessageKeywordSearch extends MainSprinklrModel
        implements ISearchKeyword {

    private SprinklrMessageKeywordSearch() {
        super(SprinklerEvents.MessageKeywordSearch, true, true);
    }

    public static void trackFail(int errorCode) {
        new SprinklrMessageKeywordSearch().sendFail(errorCode);
    }

    public static void sendLog(String searchKeyword) {
        new SprinklrMessageKeywordSearch()
                .setSearchKeyword(searchKeyword)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setSearchKeyword(String searchKeyword) {
        setProperty(PropertyKey.SearchKeyword, searchKeyword);
        return this;
    }
}
