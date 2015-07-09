package com.tosslab.jandi.app.ui.message.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.message.detail.view.ChatDetailFragment_;
import com.tosslab.jandi.app.ui.message.detail.view.TopicDetailFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity
public class TopicDetailActivity extends AppCompatActivity {

    public static final int REQUEST_DETAIL = 0x11;
    @Extra
    int entityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FormattedEntity entity = EntityManager.getInstance(TopicDetailActivity.this).getEntityById(entityId);

        boolean isDirectMessage = entity.isUser();

        Fragment fragment;

        if (isDirectMessage) {
            fragment = ChatDetailFragment_.builder()
                    .entityId(entityId)
                    .build();
        } else {
            fragment = TopicDetailFragment_.builder()
                    .entityId(entityId)
                    .build();
        }

        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, "detail")
                .commit();

    }
}
