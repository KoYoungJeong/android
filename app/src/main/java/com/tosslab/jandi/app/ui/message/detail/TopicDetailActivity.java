package com.tosslab.jandi.app.ui.message.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.message.detail.view.ChatDetailFragment_;
import com.tosslab.jandi.app.ui.message.detail.view.TopicDetailFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_topic_detail)
public class TopicDetailActivity extends AppCompatActivity {

    public static final int REQUEST_DETAIL = 0x11;
    public static final String EXTRA_LEAVE = "leave";
    @Extra
    int entityId;
    @Extra
    int teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        boolean isDirectMessage = entity.isUser();

        Fragment fragment;

        if (isDirectMessage) {
            fragment = ChatDetailFragment_.builder()
                    .entityId(entityId)
                    .build();
        } else {
            fragment = TopicDetailFragment_.builder()
                    .entityId(entityId)
                    .teamId(teamId)
                    .build();
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
}
