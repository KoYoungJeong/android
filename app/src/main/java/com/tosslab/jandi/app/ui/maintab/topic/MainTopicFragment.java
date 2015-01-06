package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.Fragment;
import android.os.Bundle;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_main_list)
public class MainTopicFragment extends Fragment {

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


}
