package com.tosslab.jandi.app.ui.maintab.topic.views.create;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.topic.views.create.presenter.TopicCreatePresenter;
import com.tosslab.jandi.app.ui.maintab.topic.views.create.presenter.TopicCreatePresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EActivity(R.layout.activity_topic_create)
@OptionsMenu(R.menu.add_topic_text)
public class TopicCreateActivity extends BaseAppCompatActivity implements TopicCreatePresenter.View {

    public static final int TITLE_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Bean(TopicCreatePresenterImpl.class)
    TopicCreatePresenter topicCreatePresenter;

    @OptionsMenuItem(R.id.action_add_topic)
    MenuItem menuCreatTopic;

    @ViewById(R.id.et_topic_create_title)
    EditText tvTitle;

    @ViewById(R.id.et_topic_create_description)
    EditText tvTopicDescription;

    @ViewById(R.id.img_topic_create_private_check)
    ImageView privateCheckView;

    @ViewById(R.id.img_topic_create_public_check)
    ImageView publicCheckView;

    @ViewById(R.id.tv_topic_create_name_count)
    TextView tvTitleCount;

    @ViewById(R.id.tv_topic_create_description_count)
    TextView tvDescriptionCount;

    @ViewById(R.id.vg_topic_create_autojoin)
    ViewGroup vgAutojoin;

    @ViewById(R.id.switch_topic_create_auto_join)
    SwitchCompat switchAutojoin;

    ProgressWheel progressWheel;
    boolean lastAutoJoin;

    @AfterInject
    void initObject() {
        topicCreatePresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        setupActionBar();
        setTopicType(true);

        progressWheel = new ProgressWheel(TopicCreateActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
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

    @TextChange(R.id.et_topic_create_title)
    void onTitleTextChange(TextView textView) {
        // NullPointer 가 될 수 있음...?
        if (menuCreatTopic == null) {
            return;
        }

        CharSequence text = textView.getText();
        if (TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) <= 0) {
            menuCreatTopic.setEnabled(false);
        } else {
            menuCreatTopic.setEnabled(true);
        }

        setTitleCount(textView.length());
    }

    @TextChange(R.id.et_topic_create_description)
    void onDescriptionTextChange(TextView textView) {
        setDescriptionCount(textView.length());
    }

    @OptionsItem(R.id.action_add_topic)
    void onCreateTopicItemSelected() {
        String topicTitle = getTopicTitle();
        String topicDescriptionText = getTopicDescriptionText();
        boolean publicSelected = isPublicSelected();
        boolean isAutojoin = publicSelected && switchAutojoin.isChecked();

        topicCreatePresenter.onCreateTopic(topicTitle, topicDescriptionText, publicSelected, isAutojoin);
    }

    @Click(R.id.layout_topic_create_public_check)
    void onPublicTypeClick() {
        setTopicType(true);
    }


    @Click(R.id.layout_topic_create_private_check)
    void onPrivateTypeClick() {
        setTopicType(false);

    }

    @Click(R.id.vg_topic_create_autojoin)
    void onAutojoinClick() {
        if (isPublicSelected()) {
            switchAutojoin.setChecked(!switchAutojoin.isChecked());
            lastAutoJoin = switchAutojoin.isChecked();
        }
    }

    private void setTopicType(boolean isPublic) {
        if (isPublic) {
            publicCheckView.setSelected(true);
            publicCheckView.setVisibility(View.VISIBLE);

            privateCheckView.setSelected(false);
            privateCheckView.setVisibility(View.GONE);

            vgAutojoin.setEnabled(true);
            switchAutojoin.setChecked(lastAutoJoin);

        } else {
            publicCheckView.setSelected(false);
            publicCheckView.setVisibility(View.GONE);

            privateCheckView.setSelected(true);
            privateCheckView.setVisibility(View.VISIBLE);

            lastAutoJoin = switchAutojoin.isChecked();
            vgAutojoin.setEnabled(false);
            switchAutojoin.setChecked(false);
        }
    }

    private String getTopicTitle() {
        return tvTitle.getText().toString();
    }

    private boolean isPublicSelected() {
        return publicCheckView.isSelected();
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
        ColoredToast.showError(TopicCreateActivity.this, TopicCreateActivity.this.getString(err_entity_duplicated_name));

    }

    @UiThread
    @Override
    public void createTopicSuccess(int teamId, int entityId, String topicTitle, boolean publicSelected) {

        ColoredToast.show(TopicCreateActivity.this, TopicCreateActivity.this.getString(R.string.jandi_message_create_entity, topicTitle));

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
                .isFavorite(false)
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
