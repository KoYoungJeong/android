package com.tosslab.jandi.app.ui.tablet;

import android.app.Fragment;
import android.widget.ListView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 8. 12..
 */
@EFragment(R.layout.fragment_navigation)
public class LeftSideFragment extends Fragment {
    public static final String TAG = LeftSideFragment.class.getSimpleName();

    @ViewById(R.id.lv_topic)
    ListView lvTopic;

    @AfterViews
    void initViews() {
    }

}
