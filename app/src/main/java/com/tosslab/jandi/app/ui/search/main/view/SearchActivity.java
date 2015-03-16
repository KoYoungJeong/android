package com.tosslab.jandi.app.ui.search.main.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
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
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EActivity(R.layout.activity_search)
public class SearchActivity extends ActionBarActivity implements SearchPresenter.View {

    private static final int SPEECH_REQUEST_CODE = 201;
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

    private SearchSelectView searchSelectView;

    @AfterViews
    void initObject() {

        searchAdapter = new SearchAdapter(getSupportFragmentManager());
        searchViewPager.setAdapter(searchAdapter);

        searchPresenter.setView(this);

        adapter = new SearchQueryAdapter(SearchActivity.this);
        searchEditText.setAdapter(adapter);
        searchEditText.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        searchSelectView = (SearchSelectView) searchAdapter.getItem(0);

        searchMinY = -(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());

        searchViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                searchSelectView.onSearchHeaderReset();
                searchLayout.setY(searchMaxY);

                Fragment item = searchAdapter.getItem(position);

                if (item instanceof SearchSelectView) {
                    searchSelectView = ((SearchSelectView) item);
                    searchSelectView.onSearchHeaderReset();

                    searchSelectView.initSearchLayoutIfFirst();
                }
            }
        });

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

    @Click(R.id.img_search_backkey)
    void onBackClick() {
        finish();
    }

    @Click(R.id.img_search_mic)
    void onVoiceSearch() {
        searchPresenter.onSearchVoice();
    }

    @TextChange(R.id.txt_search_keyword)
    void onSearchTextChanged(TextView textView, CharSequence text) {
        searchPresenter.onSearchTextChange(text.toString());
    }

    @EditorAction(R.id.txt_search_keyword)
    void onSearchTextAction(TextView textView) {
        sendNewQuery(textView.getText().toString());
    }

    @Override
    public void setOldQueries(List<SearchKeyword> searchKeywords) {
        adapter.clear();

        for (SearchKeyword searchKeyword : searchKeywords) {
            adapter.add(searchKeyword);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void startVoiceActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start the activity, the intent will be populated with the speech text
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
        }
    }

    @Override
    public void setSearchText(String searchText) {
        searchEditText.setText(searchText);
        searchEditText.setSelection(searchText.length());
    }

    @Override
    public void showNoVoiceSearchItem() {
        ColoredToast.showWarning(SearchActivity.this, getString(R.string.jandi_retry_voice_search));
    }

    @Override
    public void sendNewQuery(String searchText) {
        searchSelectView.onNewQuery(searchText);
    }

    @OnActivityResult(SPEECH_REQUEST_CODE)
    void onVoidceSearchResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        searchPresenter.onVoiceSearchResult(voiceSearchResults);

    }

    public interface SearchSelectView {
        void onNewQuery(String query);

        void onSearchHeaderReset();

        void initSearchLayoutIfFirst();
    }
}
