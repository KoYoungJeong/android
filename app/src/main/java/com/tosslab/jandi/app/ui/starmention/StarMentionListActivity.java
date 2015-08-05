package com.tosslab.jandi.app.ui.starmention;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.views.StarMentionListFragment;
import com.tosslab.jandi.app.ui.starmention.views.StarMentionListFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by tee on 15. 7. 28..
 */
@EActivity(R.layout.activity_star_mention)
public class StarMentionListActivity extends AppCompatActivity {

    public static final String TYPE_STAR_LIST = "type_star_list";
    public static final String TYPE_MENTION_LIST = "type_mention_list";
    public static final String TYPE_STAR_ALL = "type_star_all";
    public static final String TYPE_STAR_FILES = "type_star_files";

    @Extra
    String type;

    View allTabView;

    View filesTabView;

    private StarMentionListFragment allStarListFragment;
    private StarMentionListFragment filesStarListFragment;
    private StarMentionListFragment mentionListFragment;

    @AfterViews
    void initViews() {
        setupActionBar();
        initFragments();
        boolean isShowTab = showTabButton();
        if (isShowTab) {
            setupTabButton();
            onTabClick(allTabView);
        }
    }

    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);

        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        if (type.equals(TYPE_MENTION_LIST)) {
            actionBar.setTitle(R.string.jandi_mention_mentions);
        } else if (type.equals(TYPE_STAR_LIST)) {
            actionBar.setTitle(R.string.jandi_starred_stars);
        }

    }


    void setupTabButton() {
        allTabView = findViewById(R.id.tv_star_tab_all);
        allTabView.setOnClickListener(v -> {
            onTabClick(v);
        });
        filesTabView = findViewById(R.id.tv_star_tab_file);
        filesTabView.setOnClickListener(v -> {
            onTabClick(v);
        });
    }

    boolean showTabButton() {
        LinearLayout tab = (LinearLayout) findViewById(R.id.ll_starred_tab);
        if (type.equals(TYPE_STAR_LIST)) {
            tab.setVisibility(View.VISIBLE);
            return true;
        } else {
            tab.setVisibility(View.GONE);
            return false;
        }
    }

    private void initFragments() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (type.equals(TYPE_MENTION_LIST)) {
            mentionListFragment = (StarMentionListFragment) fragmentManager
                    .findFragmentByTag(TYPE_MENTION_LIST);
            if (mentionListFragment != null) {
                return;
            }

            mentionListFragment = StarMentionListFragment_.builder()
                    .listType(TYPE_MENTION_LIST).build();
            fragmentTransaction.add(R.id.star_mention_content,
                    mentionListFragment, TYPE_MENTION_LIST);

        } else if (type.equals(TYPE_STAR_LIST)) {

            allStarListFragment = (StarMentionListFragment) fragmentManager
                    .findFragmentByTag(TYPE_STAR_ALL);
            filesStarListFragment = (StarMentionListFragment) fragmentManager
                    .findFragmentByTag(TYPE_STAR_FILES);

            if (filesStarListFragment != null && allStarListFragment != null) {
                return;
            }

            if (allStarListFragment == null) {
                allStarListFragment = StarMentionListFragment_
                        .builder().listType(TYPE_STAR_ALL).build();
                fragmentTransaction.add(R.id.star_mention_content,
                        allStarListFragment, TYPE_STAR_ALL);
            }

            if (filesStarListFragment == null) {
                filesStarListFragment = StarMentionListFragment_
                        .builder().listType(TYPE_STAR_FILES).build();
                fragmentTransaction.add(R.id.star_mention_content,
                        filesStarListFragment, TYPE_STAR_FILES);
            }

        }

        fragmentTransaction.commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onTabClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (view.hashCode() == allTabView.hashCode()) {
            setSelectTab(allTabView, filesTabView);
            fragmentTransaction.hide(filesStarListFragment);
            fragmentTransaction.show(allStarListFragment);
        } else if (view.hashCode() == filesTabView.hashCode()) {
            setSelectTab(filesTabView, allTabView);
            fragmentTransaction.hide(allStarListFragment);
            fragmentTransaction.show(filesStarListFragment);
        }
        fragmentTransaction.commit();
    }

    private void setSelectTab(View selectView, View unselectView) {
        selectView.setSelected(true);
        unselectView.setSelected(false);
    }

}
