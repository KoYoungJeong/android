package com.tosslab.jandi.app.ui.search.main.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.search.main.adapter.SearchQueryAdapter;
import com.tosslab.jandi.app.ui.search.main.dagger.DaggerFileSearchComponent;
import com.tosslab.jandi.app.ui.search.main.dagger.FileSearchModule;
import com.tosslab.jandi.app.ui.search.main.presenter.FileSearchPresenter;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;

public class FileSearchActivity extends BaseAppCompatActivity implements FileSearchPresenter.View {

    private static final int SPEECH_REQUEST_CODE = 201;
    public SearchQueryAdapter searchQueryAdapter;
    long entityId = -1;

    @Bind(R.id.iv_search_mic)
    ImageView ivMic;
    @Bind(R.id.tv_search_keyword)
    AutoCompleteTextView etSearch;
    @Bind(R.id.layout_search_bar)
    View searchLayout;

    @Inject
    FileSearchPresenter fileSearchPresenter;

    InputMethodManager inputMethodManager;

    private int searchMaxY = 0;
    private int searchMinY;

    private SearchSelectView searchSelectView;

    private boolean isForeground;

    public static void start(Context context, long entityId) {
        Intent intent = new Intent(context, FileSearchActivity.class);
        intent.putExtra("entityId", entityId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);
        DaggerFileSearchComponent.builder()
                .fileSearchModule(new FileSearchModule(this))
                .build()
                .inject(this);

        initObject();
    }

    void initObject() {

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        searchQueryAdapter = new SearchQueryAdapter(FileSearchActivity.this);
        etSearch.setAdapter(searchQueryAdapter);
        Resources resources = etSearch.getResources();
        etSearch.setOnItemClickListener((parent, view, position, id) -> onSearchTextAction());

        searchMinY = -(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                64,
                resources.getDisplayMetrics());

        addFragments();
        initSearchSelectView();
        hideSoftInput();

    }

    public void addFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FileListFragment fileListFragment = (FileListFragment) fragmentManager
                .findFragmentByTag(FileListFragment.class.getName());

        if (fileListFragment != null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putLong(FileListFragment.PARAM_ENTITY_ID, entityId);
        fileListFragment = new FileListFragment();
        fileListFragment.setArguments(bundle);

        fragmentTransaction.add(R.id.layout_search_content,
                fileListFragment, FileListFragment.class.getName());
        fragmentTransaction.commit();

        searchSelectView = fileListFragment;
        searchSelectView.setOnSearchItemSelect(this::finish);
        searchSelectView.setOnSearchText(() -> etSearch.getText().toString().trim());
    }

    private void initSearchSelectView() {
        searchLayout.setY(searchMaxY);
        searchSelectView.onSearchHeaderReset();
        searchSelectView.initSearchLayoutIfFirst();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        EventBus.getDefault().register(this);
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

    @OnClick(R.id.img_search_backkey)
    void onBackClick() {
        finish();
    }

    @OnClick(R.id.iv_search_mic)
    void onVoiceSearch() {
        fileSearchPresenter.onSearchVoice();

        if (TextUtils.isEmpty(getSearchText())) {
            if (searchSelectView instanceof MessageSearchFragment) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.DeleteInputField);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.DeleteInputField);
            }
        }
    }

    @OnTextChanged(R.id.tv_search_keyword)
    void onSearchTextChanged(CharSequence text) {
        fileSearchPresenter.onSearchTextChange(text.toString());
    }

    @OnEditorAction(R.id.tv_search_keyword)
    boolean onSearchTextAction() {
        String text = etSearch.getText().toString();
        if (!TextUtils.isEmpty(text) && TextUtils.getTrimmedLength(text) > 0) {
            fileSearchPresenter.onSearchAction(text);
            sendNewQuery(text);
        } else {
            inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        return true;
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
        ColoredToast.showWarning(getString(R.string.jandi_retry_voice_search));
    }

    @Override
    public void sendNewQuery(String searchText) {

        searchSelectView.onNewQuery(searchText);

        if (searchSelectView instanceof MessageSearchFragment) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.SearchInputField);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.SearchInputField);
        }

    }

    @Override
    public void setMicToClearImage() {
        ivMic.setImageResource(R.drawable.account_icon_close);
    }

    @Override
    public void setClearToMicImage() {
        ivMic.setImageResource(R.drawable.account_icon_mic);
    }

    @Override
    public CharSequence getSearchText() {
        return etSearch.getText();
    }

    @Override
    public void setSearchText(String searchText) {
        etSearch.setText(searchText);
        etSearch.setSelection(searchText.length());
        etSearch.dismissDropDown();
    }

    @Override
    public void dismissDropDown() {
        etSearch.dismissDropDown();
    }

    @Override
    public void hideSoftInput() {
        inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    @Override
    public void showSoftInput() {
        etSearch.requestFocus();
        inputMethodManager.showSoftInput(etSearch, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SPEECH_REQUEST_CODE) {
            onVoiceSearchResult(resultCode, data);
        }
    }

    void onVoiceSearchResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        fileSearchPresenter.onVoiceSearchResult(voiceSearchResults);

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
