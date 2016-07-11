package com.tosslab.jandi.app.ui.poll.detail.component;

import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.poll.detail.module.PollDetailModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 6. 14..
 */
@Component(modules = PollDetailModule.class)
@Singleton
public interface PollDetailComponent {

    void inject(PollDetailActivity activity);

}
