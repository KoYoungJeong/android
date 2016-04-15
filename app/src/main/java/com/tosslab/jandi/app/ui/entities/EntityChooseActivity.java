package com.tosslab.jandi.app.ui.entities;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.view.ChatsChooseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
@EActivity(R.layout.activity_entity_choose)
public class EntityChooseActivity extends BaseAppCompatActivity {

    @AfterViews
    void initViews() {

        initActionBarTitle();
        ChatsChooseFragment chooseFragment = new ChatsChooseFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vg_entity_choose, chooseFragment)
                .commit();
    }

    void initActionBarTitle() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
            actionBar.setTitle(R.string.jandi_team_member);
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
    }
}
