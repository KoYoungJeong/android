package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.MemberRecentKeyword;
import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.adapter.TeamViewPagerAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.adapter.MemberRecentKeywordAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain.MemberRecentEmptyKeyword;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain.MemberRecentSearchKeyword;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain.MemberSearchKeyword;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

public class TeamMemberSearchActivity extends BaseAppCompatActivity implements ToggledUserView {

    public static final String EXTRA_KEY_SELECT_MODE = "selectMode";
    public static final String EXTRA_KEY_HAS_HEADER = "has_header";
    public static final String EXTRA_KEY_ROOM_ID = "room_id";
    private static final int REQUEST_CODE_SPEECH = 102;

    @Bind(R.id.iv_search_mic)
    ImageView ivMic;

    @Bind(R.id.tv_search_keyword)
    EditText etSearch;

    @Bind(R.id.viewpager_team_member_search)
    ViewPager viewPager;
    @Bind(R.id.tabs_team_member_search)
    TabLayout tabLayout;

    @Bind(R.id.vg_team_member_toggled)
    android.view.View vgToggled;

    @Bind(R.id.vg_team_member_search_bar)
    android.view.View vgSearch;

    @Bind(R.id.tv_team_member_toggled_invite)
    TextView tvInvite;

    @Bind(R.id.actionbar_team_member_search)
    Toolbar toolbar;

    @Bind(R.id.lv_team_member_search_recent)
    RecyclerView lvRecentSearch;

    @Bind(R.id.vg_team_member_search_recent)
    android.view.View vgRecentSearch;

    @Nullable
    @InjectExtra
    int position = 0;

    @Nullable
    @InjectExtra
    boolean isSelectMode = false;

    @Nullable
    @InjectExtra(EXTRA_KEY_ROOM_ID)
    long roomId = -1;

    private TeamViewPagerAdapter adapter;
    private PublishSubject<String> keywordSubject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_search);

        ButterKnife.bind(this);
        Dart.inject(this);
        keywordSubject = PublishSubject.create();

        Observable<String> keywordObservable = keywordSubject.throttleLast(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged();

        viewPager.setOffscreenPageLimit(2);
        adapter = new TeamViewPagerAdapter(this, getSupportFragmentManager(), keywordObservable,
                isSelectMode, false, roomId);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(position);

        setUpToolbar();

    }

    private void setUpToolbar() {

        if (!isSelectMode) {
            toolbar.setVisibility(View.GONE);
            vgSearch.setVisibility(View.VISIBLE);
            showRecentKeyword();
        } else {
            setSupportActionBar(toolbar);
            vgSearch.setVisibility(View.GONE);

            ActionBar actionBar = getSupportActionBar();
            if (roomId > 0) {
                actionBar.setTitle(R.string.jandi_invite_member_to_topic);
            } else {
                actionBar.setTitle(R.string.jandi_choose_1_1_member);
            }
        }
    }

    private void showRecentKeyword() {
        vgRecentSearch.setVisibility(View.VISIBLE);

        MemberRecentKeywordAdapter adapter = new MemberRecentKeywordAdapter();
        lvRecentSearch.setLayoutManager(new LinearLayoutManager(lvRecentSearch.getContext()));
        lvRecentSearch.setAdapter(adapter);

        adapter.setOnDeleteAll(() -> {
            MemberRecentKeywordRepository.getInstance().removeAll();
            adapter.clear();
            adapter.add(new MemberRecentEmptyKeyword());
            adapter.notifyDataSetChanged();
        });

        adapter.setOnDeleteItem(position -> {
            MemberSearchKeyword item = adapter.getActualItem(position);
            long id = ((MemberRecentSearchKeyword) item).getId();
            MemberRecentKeywordRepository.getInstance().remove(id);

            adapter.remove(id);

            if (adapter.getActualItemCount() == 0) {
                adapter.add(new MemberRecentEmptyKeyword());
            }

            adapter.notifyDataSetChanged();


        });

        adapter.setItemClickListener((view, adapter1, position1) -> {
            MemberRecentSearchKeyword item = (MemberRecentSearchKeyword) adapter.getActualItem(position1);
            etSearch.setText(item.getKeyword());
            etSearch.setSelection(etSearch.length());
        });

        List<MemberRecentKeyword> keywords = MemberRecentKeywordRepository.getInstance().getKeywords();
        Observable.from(keywords)
                .map((raw) -> ((MemberSearchKeyword) new MemberRecentSearchKeyword(raw)))
                .defaultIfEmpty(new MemberRecentEmptyKeyword())
                .collect((Func0<ArrayList<MemberSearchKeyword>>) ArrayList::new, List::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((keywords1) -> {
                    adapter.addAll(keywords1);
                    adapter.notifyDataSetChanged();
                }, t -> {});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (isSelectMode) {
            MenuInflater menuInflater = getMenuInflater();

            if (roomId > 0) {
                menuInflater.inflate(R.menu.invite_to_topic, menu);
            } else {
                menuInflater.inflate(R.menu.invite_to_direct_message, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @OnEditorAction(R.id.tv_search_keyword)
    boolean onSearchImeAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            MemberRecentKeywordRepository.getInstance().upsertKeyword(etSearch.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                vgSearch.setVisibility(View.VISIBLE);
                showRecentKeyword();
                break;
            case R.id.action_select_all:
                if (adapter.getItem(0) instanceof OnToggledUser) {
                    OnToggledUser onToggledUser = (OnToggledUser) adapter.getItem(0);
                    onToggledUser.onAddAllUser();

                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.tv_search_keyword)
    void onSearchTextChanged(CharSequence text) {
        if (text.length() > 0) {
            ivMic.setImageResource(R.drawable.search_word_delete);
            vgRecentSearch.setVisibility(View.GONE);
        } else {
            ivMic.setImageResource(R.drawable.btn_search_voice);
        }
        keywordSubject.onNext(text.toString());

    }

    @Override
    protected void onDestroy() {
        keywordSubject.onCompleted();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isSelectMode && vgSearch.getVisibility() == View.VISIBLE) {
            onBackImageClick();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.iv_search_backkey)
    public void onBackImageClick() {
        if (isSelectMode) {
            etSearch.setText("");
            vgSearch.setVisibility(View.GONE);
            vgRecentSearch.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        } else {
            finish();
        }
    }

    @OnClick(R.id.iv_search_mic)
    void onVoiceSearch() {

        if (etSearch.length() == 0) {

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            etSearch.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_SPEECH) {

            List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (voiceSearchResults != null && !voiceSearchResults.isEmpty()) {
                String searchText = voiceSearchResults.get(0);
                etSearch.setText(searchText);
                etSearch.setSelection(searchText.length());
            }
        }
    }

    @OnClick(R.id.tv_team_member_toggled_invite)
    void onInviteClick() {
        if (adapter.getItem(0) instanceof OnToggledUser) {
            OnToggledUser onToggledUser = ((OnToggledUser) adapter.getItem(0));
            onToggledUser.onInvite();
        }
    }

    @OnClick(R.id.tv_team_member_toggled_unselect_all)
    void onUnselectAllClick() {
        if (adapter.getItem(0) instanceof OnToggledUser) {
            OnToggledUser onToggledUser = ((OnToggledUser) adapter.getItem(0));
            onToggledUser.onUnselectAll();
        }
    }

    @Override
    public void toggle(int count) {
        if (count <= 0) {
            vgToggled.setVisibility(View.GONE);
        } else {
            vgToggled.setVisibility(View.VISIBLE);
        }

        tvInvite.setText(String.format("%d명 초대하기", count));
    }

    @Override
    public void addToggledUser(long[] users) {
        if (adapter.getItem(0) instanceof OnToggledUser) {
            OnToggledUser item = (OnToggledUser) adapter.getItem(0);
            item.onAddToggledUser(users);
        }
    }

}