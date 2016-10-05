package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ISearchKeyword;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrFileKeywordSearch extends MainSprinklrModel
        implements ISearchKeyword {

    private SprinklrFileKeywordSearch() {
        super(SprinklerEvents.FileKeywordSearch, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrFileKeywordSearch().sendFail(errorCode);
    }

    public static void sendLog(String searchKeyword) {
        new SprinklrFileKeywordSearch()
                .setSearchKeyword(searchKeyword)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setSearchKeyword(String searchKeyword) {
        setProperty(PropertyKey.SearchKeyword, searchKeyword);
        return this;
    }
}
