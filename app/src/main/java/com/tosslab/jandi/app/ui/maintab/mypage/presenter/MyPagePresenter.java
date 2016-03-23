package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public interface MyPagePresenter {

    void onInitialize();

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);
}
