package com.tosslab.jandi.app.ui.maintab.tabs.mypage.presenter;

/**
 * Created by tonyjs on 2016. 8. 30..
 */
public interface MyPagePresenter {

    void onInitializePollBadge();

    interface View {
        void setPollBadge(int count);
    }

}
