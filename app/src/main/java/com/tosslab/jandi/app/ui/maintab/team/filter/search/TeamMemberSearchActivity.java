package com.tosslab.jandi.app.ui.maintab.team.filter.search;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.team.adapter.TeamViewPagerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.subjects.PublishSubject;

public class TeamMemberSearchActivity extends BaseAppCompatActivity implements ToggledUser {

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

    @Bind(R.id.tv_team_member_toggled_invite)
    TextView tvInvite;

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

    }

    @OnTextChanged(R.id.tv_search_keyword)
    void onSearchTextChanged(CharSequence text) {
        keywordSubject.onNext(text.toString());
    }

    @Override
    protected void onDestroy() {
        keywordSubject.onCompleted();
        super.onDestroy();
    }


    @OnClick(R.id.iv_search_backkey)
    public void onBackImageClick() {
        finish();
    }

    @OnClick(R.id.iv_search_mic)
    void onVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
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
        if (adapter.getItem(0) instanceof OnAddToggledUser) {
            OnAddToggledUser item = (OnAddToggledUser) adapter.getItem(0);
            item.onAddToggledUser(users);
        }
    }

}
