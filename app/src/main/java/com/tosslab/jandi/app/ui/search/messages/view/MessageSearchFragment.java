package com.tosslab.jandi.app.ui.search.messages.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.search.messages.adapter.MessageSearchResultAdapter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenterImpl;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EFragment(R.layout.fragment_message_search)
public class MessageSearchFragment extends Fragment implements MessageSearchPresenter.View {

    @Bean(MessageSearchPresenterImpl.class)
    MessageSearchPresenter messageSearchPresenter;

    @ViewById(R.id.list_search_messages)
    RecyclerView searchListView;

    @AfterViews
    void initObject() {
        messageSearchPresenter.setView(this);

        FragmentActivity parentActivity = getActivity();
        searchListView.setLayoutManager(new LinearLayoutManager(parentActivity));
        searchListView.setAdapter(new MessageSearchResultAdapter(parentActivity));
    }
}
