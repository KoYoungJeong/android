package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.dagger.DaggerTopicCreateComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.dagger.TopicCreateModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter.TopicCreatePresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class TopicCreateActivity extends BaseAppCompatActivity implements TopicCreatePresenter.View {

    public static final int TITLE_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Nullable
    @InjectExtra
    String expectTopicName;

    @Inject
    TopicCreatePresenter topicCreatePresenter;

    MenuItem menuCreatTopic;

    @Bind(R.id.et_topic_create_title)
    EditText tvTitle;

    @Bind(R.id.et_topic_create_description)
    EditText tvTopicDescription;

    @Bind(R.id.tv_topic_create_name_count)
    TextView tvTitleCount;

    @Bind(R.id.tv_topic_create_description_count)
    TextView tvDescriptionCount;

    @Bind(R.id.tv_topic_create_is_public)
    TextView tvPublicSubTitle;

    @Bind(R.id.vg_topic_create_autojoin)
    ViewGroup vgAutojoin;

    @Bind(R.id.switch_topic_create_auto_join)
    SwitchCompat switchAutojoin;

    ProgressWheel progressWheel;
    boolean lastAutoJoin;
    boolean isPublicTopic = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_create);

        ButterKnife.bind(this);
        Dart.inject(this);
        DaggerTopicCreateComponent.builder()
                .topicCreateModule(new TopicCreateModule(this))
                .build()
                .inject(this);

        initViews();
    }


    void initViews() {
        setupActionBar();
        setTopicType(true);

        if (!TextUtils.isEmpty(expectTopicName)) {
            tvTitle.setText(expectTopicName);
            tvTitle.setSelection(tvTitle.length());
        }

        progressWheel = new ProgressWheel(TopicCreateActivity.this);

    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        getMenuInflater().inflate(R.menu.add_topic_text, menu);

        menuCreatTopic = menu.findItem(R.id.action_add_topic);
        onTitleTextChange(tvTitle.getText());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add_topic:
                onCreateTopicItemSelected();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.et_topic_create_title)
    void onTitleTextChange(CharSequence text) {
        setTitleCount(text.length());

        if (menuCreatTopic == null) {
            return;
        }

        if (TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) <= 0) {
            menuCreatTopic.setEnabled(false);
        } else {
            menuCreatTopic.setEnabled(true);
        }

    }

    @OnTextChanged(R.id.et_topic_create_description)
    void onDescriptionTextChange(CharSequence text) {
        setDescriptionCount(text.length());
    }

    void onCreateTopicItemSelected() {
        String topicTitle = getTopicTitle();
        String topicDescriptionText = getTopicDescriptionText();
        boolean publicSelected = isPublicTopic;
        boolean isAutojoin = publicSelected && switchAutojoin.isChecked();

        topicCreatePresenter.onCreateTopic(topicTitle, topicDescriptionText, publicSelected, isAutojoin);
    }

    @OnClick(R.id.vg_topic_create_autojoin)
    void onAutojoinClick() {
        if (isPublicTopic) {
            switchAutojoin.setChecked(!switchAutojoin.isChecked());
            lastAutoJoin = switchAutojoin.isChecked();
        }
    }

    @OnClick(R.id.vg_topic_create_is_public)
    void onPublicClick() {

        String[] items = {
                getString(R.string.jandi_topic_public),
                getString(R.string.jandi_topic_private)
        };

        new AlertDialog.Builder(TopicCreateActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.jandi_is_topic_required)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setSingleChoiceItems(items, isPublicTopic ? 0 : 1, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            isPublicTopic = true;
                            break;
                        case 1:
                            isPublicTopic = false;
                            break;
                    }
                    setUpPublicSubTitle(isPublicTopic);
                    setTopicType(isPublicTopic);
                    dialog.dismiss();
                })
                .create()
                .show();

    }

    private void setUpPublicSubTitle(boolean isPublicTopic) {
        if (isPublicTopic) {
            tvPublicSubTitle.setText(R.string.jandi_topic_public);
        } else {
            tvPublicSubTitle.setText(R.string.jandi_topic_private);
        }
    }

    void setTopicType(boolean isPublic) {
        if (isPublic) {
            vgAutojoin.setEnabled(true);
            switchAutojoin.setChecked(lastAutoJoin);
        } else {

            lastAutoJoin = switchAutojoin.isChecked();
            vgAutojoin.setEnabled(false);
            switchAutojoin.setChecked(false);
        }
    }

    private String getTopicTitle() {
        return tvTitle.getText().toString();
    }

    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void createTopicFailed(int err_entity_duplicated_name) {
        ColoredToast.showError(TopicCreateActivity.this.getString(err_entity_duplicated_name));

    }

    @Override
    public void createTopicSuccess(long teamId, long entityId, String topicTitle, boolean publicSelected) {

        ColoredToast.show(TopicCreateActivity.this.getString(R.string.jandi_message_create_entity, topicTitle));

        int entityType;
        if (publicSelected) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        }

        MessageListV2Activity_.intent(TopicCreateActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .teamId(teamId)
                .roomId(entityId)
                .entityType(entityType)
                .entityId(entityId)
                .start();

        TopicCreateActivity.this.finish();

    }

    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(TopicCreateActivity.this, null);
    }

    private void setTitleCount(int length) {
        tvTitleCount.setText(String.format("%d/%d", length, TITLE_MAX_LENGTH));
    }

    private void setDescriptionCount(int length) {
        tvDescriptionCount.setText(String.format("%d/%d", length, DESCRIPTION_MAX_LENGTH));
    }

    private String getTopicDescriptionText() {
        return tvTopicDescription.getText().toString().trim();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
