package com.tosslab.jandi.app.ui.search.main.view;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenterImpl;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.util.List;

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
    private SearchQueryAdapter adapter;

    @AfterViews
    void initObject() {

        searchAdapter = new SearchAdapter(getSupportFragmentManager());
        searchViewPager.setAdapter(searchAdapter);

        searchPresenter.setView(this);

        adapter = new SearchQueryAdapter(SearchActivity.this);
        searchEditText.setAdapter(adapter);

    }

    @TextChange(R.id.txt_search_keyword)
    void onSearchTextChanged(TextView textView, CharSequence text) {
        searchPresenter.onSearchTextChange(text.toString());
    }

    @EditorAction(R.id.txt_search_keyword)
    void onSearchTextAction(TextView textView) {
        searchPresenter.onSearchAction(textView.getText().toString());
    }

    @Override
    public void setOldQueries(List<SearchKeyword> searchKeywords) {
        adapter.clear();

        for (SearchKeyword searchKeyword : searchKeywords) {
            adapter.add(searchKeyword);
        }
        adapter.notifyDataSetChanged();
    }
}
