package com.tosslab.jandi.app.ui.search.main.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
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

import de.greenrobot.event.EventBus;

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

    @ViewById(R.id.layout_search_bar)
    View searchLayout;

    SearchAdapter searchAdapter;
    private SearchQueryAdapter adapter;

    private int searchMaxY = 0;
    private int searchMinY;

    @AfterViews
    void initObject() {

        searchAdapter = new SearchAdapter(getSupportFragmentManager());
        searchViewPager.setAdapter(searchAdapter);

        searchPresenter.setView(this);

        adapter = new SearchQueryAdapter(SearchActivity.this);
        searchEditText.setAdapter(adapter);
        searchEditText.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        searchMinY = -(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(SearchResultScrollEvent event) {
        int offset = event.getOffset();

        float futureLayoutY = searchLayout.getY() - offset;

        if (futureLayoutY <= searchMinY) {
            searchLayout.setY(searchMinY);
        } else if (futureLayoutY >= searchMaxY) {
            searchLayout.setY(searchMaxY);
        } else {
            searchLayout.setY(futureLayoutY);
        }
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
