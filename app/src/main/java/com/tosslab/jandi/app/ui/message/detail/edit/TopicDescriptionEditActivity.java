package com.tosslab.jandi.app.ui.message.detail.edit;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import retrofit.RetrofitError;

@EActivity(R.layout.activity_topic_description_edit)
@OptionsMenu(R.menu.topic_description_edit)
public class TopicDescriptionEditActivity extends BaseAppCompatActivity {

    public static final int DESCRIPTION_MAX_LENGTH = 300;
    public static final int REQUEST_EDIT = 321;

    @Extra
    int entityId;

    @ViewById(R.id.et_topic_description_edit_content)
    EditText etDescpription;

    @ViewById(R.id.tv_topic_description_edit_count)
    TextView tvDescpriptionLength;

    @Bean
    EntityClientManager entityClientManager;

    @AfterViews
    void initViews() {

        setUpActionbar();

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        if (entity.isUser()) {
            finish();
            return;
        }

        String description = getTopicDescription(entity);

        etDescpription.setText(description);
        etDescpription.setSelection(etDescpription.length());

    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    private String getTopicDescription(FormattedEntity entity) {
        String description;
        if (entity.isPublicTopic()) {
            description = ((ResLeftSideMenu.Channel) entity.getEntity()).description;
        } else {
            description = ((ResLeftSideMenu.PrivateGroup) entity.getEntity()).description;
        }
        return description;
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

    @TextChange(R.id.et_topic_description_edit_content)
    void onChangeDescriptionText(CharSequence text) {
        tvDescpriptionLength.setText(String.format("%d/%d", text.length(), DESCRIPTION_MAX_LENGTH));
    }

    @OptionsItem(R.id.action_topic_description_save)
    @Background
    void onSaveOptionSelected() {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        String description = etDescpription.getText().toString().trim();

        try {
            if (entity.isPublicTopic()) {
                entityClientManager.modifyChannelDescription(entityId, description);
            } else {
                entityClientManager.modifyPrivateGroupDescription(entityId, description);
            }

            if (entity.isPublicTopic()) {
                ((ResLeftSideMenu.Channel) entity.getEntity()).description = description;
            } else {
                ((ResLeftSideMenu.PrivateGroup) entity.getEntity()).description = description;
            }

            setResult(RESULT_OK);
            finish();

        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeMenuClick() {
        finish();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
