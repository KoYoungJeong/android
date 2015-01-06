package com.tosslab.jandi.app.ui.maintab.chat;

import android.app.Fragment;
import android.os.Bundle;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;

import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment
public class MainChatListFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {
        EntityManager entityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();

    }
}
