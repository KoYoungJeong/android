package com.tosslab.jandi.app.ui.maintab.mypage.component;

import com.tosslab.jandi.app.ui.maintab.mypage.MyPageFragment;
import com.tosslab.jandi.app.ui.maintab.mypage.module.MyPageModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 17..
 */
@Component(modules = MyPageModule.class)
@Singleton
public interface MyPageComponent {

    void inject(MyPageFragment fragment);

}
