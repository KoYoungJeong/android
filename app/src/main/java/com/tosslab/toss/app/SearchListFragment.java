package com.tosslab.toss.app;

import android.app.Fragment;
import android.widget.ListView;

import com.tosslab.toss.app.lists.SearchedFileItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 4..
 */
@EFragment(R.layout.fragment_search)
public class SearchListFragment extends Fragment {
    private final Logger log = Logger.getLogger(SearchListFragment.class);

    @RestService
    TossRestClient tossRestClient;
    @FragmentArg
    int searchType;
    @ViewById(R.id.list_searched_file)
    ListView listSearchedFiles;
    @Bean
    SearchedFileItemListAdapter mAdapter;

    private ProgressWheel mProgressWheel;

    @AfterViews
    void bindAdapter() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();

        listSearchedFiles.setAdapter(mAdapter);

        log.debug("bind adapter for " + searchType);
    }



}
