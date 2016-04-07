package com.tosslab.jandi.app.ui.members.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.members.adapter.searchable.SearchableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.search.component.DaggerMemberSearchComponent;
import com.tosslab.jandi.app.ui.members.search.module.MemberSearchModule;
import com.tosslab.jandi.app.ui.members.search.presenter.MemberSearchPresenter;
import com.tosslab.jandi.app.ui.members.search.view.MemberSearchView;
import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public class MemberSearchActivity extends BaseAppCompatActivity implements MemberSearchView {

    @Inject
    InputMethodManager inputMethodManager;

    @Inject
    MemberSearchPresenter memberSearchPresenter;

    @Inject
    MemberSearchableDataView memberSearchableDataView;

    @Bind(R.id.progress_member_search)
    ProgressBar pbMemberSearch;
    @Bind(R.id.et_member_search)
    EditText etMemberSearch;

    @Bind(R.id.layout_search_bar)
    ViewGroup vgSearchBar;

    @Bind(R.id.lv_member_search)
    RecyclerView lvMemberSearch;
    @Bind(R.id.toolbar_member_search)
    Toolbar toolbar;

    public static void start(Activity activity, ActivityOptionsCompat activityOptions) {
        Intent intent = new Intent(activity, MemberSearchActivity.class);
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(0, 0);
        }

        SearchableMemberListAdapter adapter = new SearchableMemberListAdapter();
        DaggerMemberSearchComponent.builder()
                .memberSearchModule(new MemberSearchModule(this, adapter))
                .build()
                .inject(this);

        setContentView(R.layout.activity_member_search);
        ButterKnife.bind(this);

        initEnterAnimation();

        setupActionBar();

        initMemberSearchListView(adapter);

        memberSearchPresenter.onInitializeWholeMembers();
    }

    private void initEnterAnimation() {
        ViewCompat.setTransitionName(vgSearchBar, getString(R.string.jandi_action_search));
    }

    private void initMemberSearchListView(SearchableMemberListAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        lvMemberSearch.setLayoutManager(layoutManager);
        lvMemberSearch.setAdapter(adapter);

        memberSearchableDataView.setOnMemberClickListener(member -> {
            showUserProfile(member.getId());
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.SelectMember);
        });
    }

    private void showUserProfile(long userId) {
        MemberProfileActivity_.intent(this)
                .memberId(userId)
                .start();
    }

    @OnTextChanged(R.id.et_member_search)
    void onSearchMember(CharSequence text) {
        memberSearchPresenter.onSearchMember(text.toString());
    }

    @OnEditorAction(R.id.et_member_search)
    boolean onSearchAction(TextView view, int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(getString(R.string.jandi_team_member));
    }

    @Override
    public void showProgress() {
        pbMemberSearch.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        pbMemberSearch.setVisibility(View.GONE);
    }

    @Override
    public void notifyDataSetChanged() {
        memberSearchableDataView.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        memberSearchPresenter.stopMemberSearchQueue();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
