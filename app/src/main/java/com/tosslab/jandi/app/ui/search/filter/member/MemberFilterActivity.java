package com.tosslab.jandi.app.ui.search.filter.member;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.SearchableMemberFilterAdapter;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.view.MemberFilterableDataView;
import com.tosslab.jandi.app.ui.search.filter.member.component.DaggerMemberFilterComponent;
import com.tosslab.jandi.app.ui.search.filter.member.module.MemberFilterModule;
import com.tosslab.jandi.app.ui.search.filter.member.presenter.MemberFilterPresenter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * Created by tonyjs on 16. 7. 22..
 */
public class MemberFilterActivity extends BaseAppCompatActivity implements MemberFilterPresenter.View {

    public static final int REQUEST_CODE_VOICE = 8812;

    public static final String KEY_SELECTED_MEMBER_ID = "selectedMemberId";

    public static final String KEY_FILTERED_MEMBER_ID = "memberId";

    public static final long MEMBER_ID_ALL = -1l;

    @Inject
    InputMethodManager inputMethodManager;

    @Inject
    MemberFilterPresenter memberFilterPresenter;

    @Inject
    MemberFilterableDataView memberFilterableDataView;

    @Bind(R.id.progress_member_filter)
    ProgressBar pbMemberFilter;

    @Bind(R.id.et_member_filter)
    EditText etMemberFilter;

    @Bind(R.id.lv_member_filter)
    RecyclerView lvMembers;

    @Bind(R.id.toolbar_member_filter)
    Toolbar toolbar;

    private MenuItem menuDeleteQuery;
    private MenuItem menuVoiceInput;

    public static void startForResult(Activity activity, long selectedMemberId, int requestCode) {
        Intent intent = new Intent(activity, MemberFilterActivity.class);
        intent.putExtra(KEY_SELECTED_MEMBER_ID, selectedMemberId);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_bottom_with_alpha, 0);
        super.onCreate(savedInstanceState);

        SearchableMemberFilterAdapter adapter = new SearchableMemberFilterAdapter();
        DaggerMemberFilterComponent.builder()
                .memberFilterModule(new MemberFilterModule(this, adapter))
                .build()
                .inject(this);

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.ChooseMember);

        setContentView(R.layout.activity_member_filter);

        ButterKnife.bind(this);

        setupActionBar();

        initMemberFilterListView(adapter);

        memberFilterPresenter.onInitializeWholeMembers();
        memberFilterPresenter.initSelectedMemberId(getIntent().getLongExtra(KEY_SELECTED_MEMBER_ID, -1));
    }

    private void initMemberFilterListView(SearchableMemberFilterAdapter adapter) {
        lvMembers.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        lvMembers.setAdapter(adapter);

        memberFilterableDataView.setOnAllMemberClickListener(() -> {
            Intent data = new Intent();
            data.putExtra(KEY_FILTERED_MEMBER_ID, MEMBER_ID_ALL);
            setResult(RESULT_OK, data);
            finish();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseMember,
                    AnalyticsValue.Action.ChooseAllMember);
        });

        memberFilterableDataView.setOnMemberClickListener(member -> {
            Intent data = new Intent();
            data.putExtra(KEY_FILTERED_MEMBER_ID, member.getId());
            setResult(RESULT_OK, data);
            finish();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseMember,
                    AnalyticsValue.Action.ChooseMember);
        });
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.account_icon_back);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_filter_activity, menu);
        menuDeleteQuery = menu.findItem(R.id.action_close);
        menuVoiceInput = menu.findItem(R.id.action_voice);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_close:
                etMemberFilter.setText("");
                return true;
            case R.id.action_voice:
                startVoiceInput();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.et_member_filter)
    void onSearchMember(CharSequence text) {
        memberFilterPresenter.onSearchMember(text.toString());

        boolean isEmpty = TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) <= 0;
        menuDeleteQuery.setVisible(!isEmpty);
        menuVoiceInput.setVisible(isEmpty);
    }

    @OnEditorAction(R.id.et_member_filter)
    boolean onSearchAction(TextView view, int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideKeyboard();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseMember,
                    AnalyticsValue.Action.KeywordSearch);
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(etMemberFilter.getWindowToken(), 0);
    }

    @Override
    public void showProgress() {
        pbMemberFilter.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        pbMemberFilter.setVisibility(View.GONE);
    }

    @Override
    public void notifyDataSetChanged() {
        memberFilterableDataView.notifyDataSetChanged();
    }

    public void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start the activity, the intent will be populated with the speech text
        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE);
        } catch (ActivityNotFoundException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VOICE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (voiceSearchResults != null && voiceSearchResults.isEmpty()) {
                return;
            }

            memberFilterPresenter.onSearchMember(voiceSearchResults.get(0));
        }
    }

    @Override
    protected void onDestroy() {
        memberFilterPresenter.stopMemberSearchQueue();
        hideKeyboard();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_to_bottom);
    }
}
