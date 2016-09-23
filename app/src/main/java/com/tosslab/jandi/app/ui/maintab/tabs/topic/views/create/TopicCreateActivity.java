package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter.TopicCreatePresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter.TopicCreatePresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_topic_create)
@OptionsMenu(R.menu.add_topic_text)
public class TopicCreateActivity extends BaseAppCompatActivity implements TopicCreatePresenter.View {

    public static final int TITLE_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Extra
    String expectTopicName;

    @Bean(TopicCreatePresenterImpl.class)
    TopicCreatePresenter topicCreatePresenter;

    @OptionsMenuItem(R.id.action_add_topic)
    MenuItem menuCreatTopic;

    @ViewById(R.id.et_topic_create_title)
    EditText tvTitle;

    @ViewById(R.id.et_topic_create_description)
    EditText tvTopicDescription;

    @ViewById(R.id.tv_topic_create_name_count)
    TextView tvTitleCount;

    @ViewById(R.id.tv_topic_create_description_count)
    TextView tvDescriptionCount;

    @ViewById(R.id.tv_topic_create_is_public)
    TextView tvPublicSubTitle;

    @ViewById(R.id.vg_topic_create_autojoin)
    ViewGroup vgAutojoin;

    @ViewById(R.id.switch_topic_create_auto_join)
    SwitchCompat switchAutojoin;

    ProgressWheel progressWheel;
    boolean lastAutoJoin;
    boolean isPublicTopic = true;

    @AfterInject
    void initObject() {
        topicCreatePresenter.setView(this);
    }

    @AfterViews
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

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        onTitleTextChange(tvTitle);
        return true;
    }

    @TextChange(R.id.et_topic_create_title)
    void onTitleTextChange(TextView textView) {
        // NullPointer 가 될 수 있음...?
        setTitleCount(textView.length());

        if (menuCreatTopic == null) {
            return;
        }

        CharSequence text = textView.getText();
        if (TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) <= 0) {
            menuCreatTopic.setEnabled(false);
        } else {
            menuCreatTopic.setEnabled(true);
        }

    }

    @TextChange(R.id.et_topic_create_description)
    void onDescriptionTextChange(TextView textView) {
        setDescriptionCount(textView.length());
    }

    @OptionsItem(R.id.action_add_topic)
    void onCreateTopicItemSelected() {
        String topicTitle = getTopicTitle();
        String topicDescriptionText = getTopicDescriptionText();
        boolean publicSelected = isPublicTopic;
        boolean isAutojoin = publicSelected && switchAutojoin.isChecked();

        topicCreatePresenter.onCreateTopic(topicTitle, topicDescriptionText, publicSelected, isAutojoin);
    }

    @Click(R.id.vg_topic_create_autojoin)
    void onAutojoinClick() {
        if (isPublicTopic) {
            switchAutojoin.setChecked(!switchAutojoin.isChecked());
            lastAutoJoin = switchAutojoin.isChecked();
        }
    }

    @Click(R.id.vg_topic_create_is_public)
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

    @UiThread
    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    @Override
    public void createTopicFailed(int err_entity_duplicated_name) {
        ColoredToast.showError(TopicCreateActivity.this.getString(err_entity_duplicated_name));

    }

    @UiThread
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
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
