package com.tosslab.jandi.app.ui.poll.util;

import com.tosslab.jandi.app.events.poll.PollDataChangedEvent;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.network.models.poll.Poll;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class PollUtil {

    public static void upsertPoll(Poll poll, boolean shouldNotifyDataChanged) {
        Observable.just(poll)
                .subscribeOn(Schedulers.io())
                .subscribe(poll1 -> {
                    PollRepository.getInstance().upsertPoll(poll1);
                    if (shouldNotifyDataChanged) {
                        EventBus.getDefault().post(PollDataChangedEvent.create(poll1));
                    }
                });
    }


}
