package com.tosslab.jandi.app.ui.search.main.view;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.AutoCompleteTextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenterImpl;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EActivity(R.layout.activity_search)
public class SearchActivity extends ActionBarActivity implements SearchPresenter.View {

    @Bean(SearchPresenterImpl.class)
    SearchPresenter searchPresenter;

    @ViewById(R.id.pager_search)
    ViewPager searchViewPager;

    @ViewById(R.id.txt_search_keyword)
    AutoCompleteTextView searchEditText;

    SearchAdapter searchAdapter;

    @AfterViews
    void initObject() {

        searchAdapter = new SearchAdapter(getSupportFragmentManager());
        searchViewPager.setAdapter(searchAdapter);

        searchPresenter.setView(this);
    }

}
