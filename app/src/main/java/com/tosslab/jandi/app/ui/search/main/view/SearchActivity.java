package com.tosslab.jandi.app.ui.search.main.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment_;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenter;
import com.tosslab.jandi.app.ui.search.main.presenter.SearchPresenterImpl;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment_;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
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
public class SearchActivity extends BaseAppCompatActivity implements SearchPresenter.View {

    private static final int SPEECH_REQUEST_CODE = 201;
    @Bean(SearchPresenterImpl.class)
    SearchPresenter searchPresenter;

    @Extra
    boolean isFromFiles;

    @Extra
    int entityId = -1;

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
    private boolean isForeground;

    @AfterViews
    void initObject() {

        searchPresenter.setView(this);

        searchQueryAdapter = new SearchQueryAdapter(SearchActivity.this);
        searchEditText.setAdapter(searchQueryAdapter);
        Resources resources = searchEditText.getResources();
        searchEditText.setOnItemClickListener((parent, view, position, id) ->
                onSearchTextAction(searchEditText));

        searchMinY = -(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                64,
                resources.getDisplayMetrics());

        searchQueries = new String[]{"", ""};
        addFragments();

        if (!isFromFiles) {
            onMessageTabClick();
        } else {
            onFileTabClick();
        }

    }

    private void addFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fileListFragment = (FileListFragment) fragmentManager
                .findFragmentByTag(FileListFragment.class.getName());

        messageSearchFragment = (MessageSearchFragment) fragmentManager
                .findFragmentByTag(MessageSearchFragment.class.getName());

        if (fileListFragment != null && messageSearchFragment != null) {
            return;
        }
        if (fileListFragment == null) {
            fileListFragment = FileListFragment_.builder().entityIdForCategorizing(entityId).build();
            fragmentTransaction.add(R.id.layout_search_content,
                    fileListFragment, FileListFragment.class.getName());
        }
        if (messageSearchFragment == null) {
            messageSearchFragment = MessageSearchFragment_.builder().entityId(entityId).build();
            fragmentTransaction.add(R.id.layout_search_content,
                    messageSearchFragment, MessageSearchFragment.class.getName());
        }
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
        hideSoftInput();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (messageSearchFragment == null) {
            messageSearchFragment = MessageSearchFragment_.builder().build();
            fragmentTransaction.add(R.id.layout_search_content,
                    messageSearchFragment, MessageSearchFragment.class.getName());
        } else {
            if (fileListFragment != null) {
                fragmentTransaction.hide(fileListFragment);
            }
            fragmentTransaction.show(messageSearchFragment);
        }
        fragmentTransaction.commit();

        if (searchSelectView != null) {
            // 메세지 -> 파일 검색 선택
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.GoToMsgSearch);
            searchSelectView.setOnSearchItemSelect(null);
        }
        searchSelectView = messageSearchFragment;
        searchSelectView.setOnSearchItemSelect(this::finish);
        searchSelectView.setOnSearchText(() -> {
            String searchText = searchEditText.getText().toString();
            searchQueries[0] = searchText;
            return searchText;
        });

        setSelectTab(messageTabView, filesTabView);
        setSearchText(searchQueries[0]);
        initSearchSelectView();

    }

    @Click(R.id.txt_search_category_files)
    void onFileTabClick() {
        hideSoftInput();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fileListFragment == null) {

            fileListFragment = FileListFragment_.builder().build();
            fragmentTransaction.add(R.id.layout_search_content,
                    fileListFragment, FileListFragment.class.getName());
        } else {
            if (messageSearchFragment != null) {
                fragmentTransaction.hide(messageSearchFragment);
            }
            fragmentTransaction.show(fileListFragment);
        }
        fragmentTransaction.commit();

        if (searchSelectView != null) {
            searchSelectView.setOnSearchItemSelect(null);
            // 메세지 -> 파일 검색 선택
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.GoToFilesSearch);
        }
        searchSelectView = fileListFragment;
        searchSelectView.setOnSearchItemSelect(this::finish);
        searchSelectView.setOnSearchText(() -> {
            String searchText = searchEditText.getText().toString();
            searchQueries[1] = searchText;
            return searchText;
        });


        setSelectTab(filesTabView, messageTabView);
        setSearchText(searchQueries[1]);
        initSearchSelectView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        EventBus.getDefault().register(this);
        ActivityHelper.setOrientation(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        isForeground = false;
        super.onPause();
    }

    public void onEvent(SearchResultScrollEvent event) {

        if (!isForeground) {
            return;
        }

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

        if (TextUtils.isEmpty(getSearchText())) {
            if (searchSelectView instanceof MessageSearchFragment) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.DeleteInputField);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.DeleteInputField);
            }
        }
    }

    @TextChange(R.id.txt_search_keyword)
    void onSearchTextChanged(TextView textView, CharSequence text) {
        searchPresenter.onSearchTextChange(text.toString());
    }

    @EditorAction(R.id.txt_search_keyword)
    void onSearchTextAction(TextView textView) {
        String text = textView.getText().toString();
        if (!TextUtils.isEmpty(text) && TextUtils.getTrimmedLength(text) > 0) {
            searchPresenter.onSearchAction(text);
            sendNewQuery(text);
        } else {
            inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        }
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

        if (searchSelectView instanceof MessageSearchFragment) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.SearchInputField);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.SearchInputField);
        }

    }

    @Override
    public void setMicToClearImage() {
        micImageView.setImageResource(R.drawable.account_icon_close);
    }

    @Override
    public void setClearToMicImage() {
        micImageView.setImageResource(R.drawable.account_icon_mic);
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

    @Override
    public void dismissDropDown() {
        searchEditText.dismissDropDown();
    }

    @Override
    public void hideSoftInput() {
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    @Override
    public void showSoftInput() {
        searchEditText.requestFocus();
        inputMethodManager.showSoftInput(searchEditText, 0);
    }

    @OnActivityResult(SPEECH_REQUEST_CODE)
    void onVoidceSearchResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        searchPresenter.onVoiceSearchResult(voiceSearchResults);

    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public interface SearchSelectView {
        void onNewQuery(String query);

        void onSearchHeaderReset();

        void initSearchLayoutIfFirst();

        void setOnSearchItemSelect(OnSearchItemSelect onSearchItemSelect);

        void setOnSearchText(OnSearchText onSearchText);
    }

    public interface OnSearchItemSelect {
        void onSearchItemSelect();
    }

    public interface OnSearchText {
        String getSearchText();
    }
}
