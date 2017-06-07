package com.tosslab.jandi.app.ui.message.detail.edit;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TopicDescriptionEditActivity extends BaseAppCompatActivity {

    public static final int DESCRIPTION_MAX_LENGTH = 300;
    public static final int REQUEST_EDIT = 321;

    @InjectExtra
    long entityId;

    @Bind(R.id.et_topic_description_edit_content)
    EditText etDescpription;

    @Bind(R.id.tv_topic_description_edit_count)
    TextView tvDescpriptionLength;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_description_edit);

        ButterKnife.bind(this);
        Dart.inject(this);
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.topic_description_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    void initViews() {

        setUpActionbar();


        if (!TeamInfoLoader.getInstance().isTopic(entityId)) {
            finish();
            return;
        }
        TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(entityId);

        String description = topicRoom.getDescription();

        etDescpription.setText(description);
        etDescpription.setSelection(etDescpription.length());

    }

    private void setUpActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_topic_description_edit);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        }

    }

    @OnTextChanged(R.id.et_topic_description_edit_content)
    void onChangeDescriptionText(CharSequence text) {
        tvDescpriptionLength.setText(String.format("%d/%d", text.length(), DESCRIPTION_MAX_LENGTH));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_topic_description_save) {
            onSaveOptionSelected();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    void onSaveOptionSelected() {
        Completable.defer(() -> {

            try {
                TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(entityId);

                String description = etDescpription.getText().toString().trim();
                ReqModifyTopicDescription reqModifyTopicDescription = new ReqModifyTopicDescription();
                reqModifyTopicDescription.description = description;
                reqModifyTopicDescription.teamId = topicRoom.getTeamId();
                if (topicRoom.isPublicTopic()) {
                    new ChannelApi(InnerApiRetrofitBuilder.getInstance()).modifyPublicTopicDescription(topicRoom.getTeamId(), reqModifyTopicDescription, entityId);
                } else {
                    new GroupApi(InnerApiRetrofitBuilder.getInstance()).modifyGroupDescription(topicRoom.getTeamId(), reqModifyTopicDescription, entityId);
                }

                TopicRepository.getInstance().updateDescription(entityId, description);
                return Completable.complete();
            } catch (RetrofitException e) {
                return Completable.error(e);
            }

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, Throwable::printStackTrace);

    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
