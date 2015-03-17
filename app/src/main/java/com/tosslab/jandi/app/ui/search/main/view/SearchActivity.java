package com.tosslab.jandi.app.ui.search.main.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment_;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenterImpl;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment_;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.SystemService;
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

    @ViewById(R.id.txt_search_keyword)
    AutoCompleteTextView searchEditText;

    @ViewById(R.id.layout_search_bar)
    View searchLayout;

    @ViewById(R.id.txt_search_category_messages)
    View messageTabView;

    @ViewById(R.id.txt_search_category_files)
    View filesTabView;

    @ViewById(R.id.img_search_mic)
    ImageView micImageView;

    @SystemService
    InputMethodManager inputMethodManager;

    private SearchQueryAdapter searchQueryAdapter;

    private int searchMaxY = 0;
    private int searchMinY;

    private SearchSelectView searchSelectView;
    private MessageSearchFragment messageSearchFragment;
    private FileListFragment fileListFragment;

    private String[] searchQueries;

    @AfterViews
    void initObject() {

        searchPresenter.setView(this);

        searchQueryAdapter = new SearchQueryAdapter(SearchActivity.this);
        searchEditText.setAdapter(searchQueryAdapter);
        searchEditText.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        searchMinY = -(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());

        searchQueries = new String[]{"", ""};
        addFragments();
        onMessageTabClick();

        initSearchSelectView();

    }

    private void addFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fileListFragment = FileListFragment_.builder().build();
        fragmentTransaction.add(R.id.layout_search_content, fileListFragment);

        messageSearchFragment = MessageSearchFragment_.builder().build();
        fragmentTransaction.add(R.id.layout_search_content, messageSearchFragment);

        fragmentTransaction.commit();
    }

    private void initSearchSelectView() {
        searchLayout.setY(searchMaxY);
        searchSelectView.onSearchHeaderReset();
        searchSelectView.initSearchLayoutIfFirst();
    }

    private void setSelectTab(View selectView, View unselectView) {
        selectView.setSelected(true);
        unselectView.setSelected(false);
    }

    @Click(R.id.txt_search_category_messages)
    void onMessageTabClick() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (messageSearchFragment == null) {
            messageSearchFragment = MessageSearchFragment_.builder().build();
            fragmentTransaction.add(R.id.layout_search_content, messageSearchFragment);
        } else {
            if (fileListFragment != null) {
                fragmentTransaction.hide(fileListFragment);
            }
            fragmentTransaction.show(messageSearchFragment);
        }
        fragmentTransaction.commit();

        searchSelectView = messageSearchFragment;

        setSelectTab(messageTabView, filesTabView);
        setSearchText(searchQueries[0]);
        initSearchSelectView();
    }

    @Click(R.id.txt_search_category_files)
    void onFileTabClick() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fileListFragment == null) {

            fileListFragment = FileListFragment_.builder().build();
            fragmentTransaction.add(R.id.layout_search_content, fileListFragment);
        } else {
            if (messageSearchFragment != null) {
                fragmentTransaction.hide(messageSearchFragment);
            }
            fragmentTransaction.show(fileListFragment);
        }
        fragmentTransaction.commit();

        searchSelectView = fileListFragment;

        setSelectTab(filesTabView, messageTabView);
        setSearchText(searchQueries[1]);
        initSearchSelectView();
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

        if (event.getFrom() != searchSelectView.getClass()) {
            return;
        }

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
        searchEditText.dismissDropDown();
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        sendNewQuery(textView.getText().toString());
    }

    @Override
    public void setOldQueries(List<SearchKeyword> searchKeywords) {
        searchQueryAdapter.clear();

        for (SearchKeyword searchKeyword : searchKeywords) {
            searchQueryAdapter.add(searchKeyword);
        }
        searchQueryAdapter.notifyDataSetChanged();
    }

    @Override
    public void startVoiceActivity() {

        searchEditText.dismissDropDown();
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

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
    public void showNoVoiceSearchItem() {
        ColoredToast.showWarning(SearchActivity.this, getString(R.string.jandi_retry_voice_search));
    }

    @Override
    public void sendNewQuery(String searchText) {

        if (searchSelectView == messageSearchFragment) {
            searchQueries[0] = searchText;
        } else {
            searchQueries[1] = searchText;
        }

        searchSelectView.onNewQuery(searchText);
    }

    @Override
    public void setMicToClearImage() {
        micImageView.setImageResource(R.drawable.jandi_account_close);
    }

    @Override
    public void setClearToMicImage() {
        micImageView.setImageResource(R.drawable.account_mic);
    }

    @Override
    public CharSequence getSearchText() {
        return searchEditText.getText();
    }

    @Override
    public void setSearchText(String searchText) {
        searchEditText.setText(searchText);
        searchEditText.setSelection(searchText.length());
        searchEditText.dismissDropDown();
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
