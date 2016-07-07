package com.tosslab.jandi.app.ui.message.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.detail.view.ChatDetailFragment;
import com.tosslab.jandi.app.ui.message.detail.view.TopicDetailFragment;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_topic_detail)
public class TopicDetailActivity extends BaseAppCompatActivity {

    public static final int REQUEST_DETAIL = 0x11;
    public static final String EXTRA_LEAVE = "leave";
    @Extra
    long entityId;
    @Extra
    long teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        boolean isDirectMessage = TeamInfoLoader.getInstance().isUser(entityId);

        Fragment fragment;

        if (isDirectMessage) {
            fragment = ChatDetailFragment.createFragment(TopicDetailActivity.this, entityId);
        } else {
            fragment = TopicDetailFragment.createFragment(TopicDetailActivity.this, entityId);
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_topic_detail_content, fragment, "detail")
                .commit();

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

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
