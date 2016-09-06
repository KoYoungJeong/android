package com.tosslab.jandi.app.ui.maintab.tabs.mypage.component;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.MyPageFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.module.MyPageModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 30..
 */
@Component(modules = {MyPageModule.class})
public interface MyPageComponent {

    void inject(MyPageFragment fragment);

}
