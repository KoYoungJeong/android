package com.tosslab.jandi.app.ui.message.detail.edit;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;



@EActivity(R.layout.activity_topic_description_edit)
@OptionsMenu(R.menu.topic_description_edit)
public class TopicDescriptionEditActivity extends BaseAppCompatActivity {

    public static final int DESCRIPTION_MAX_LENGTH = 300;
    public static final int REQUEST_EDIT = 321;

    @Extra
    long entityId;

    @ViewById(R.id.et_topic_description_edit_content)
    EditText etDescpription;

    @ViewById(R.id.tv_topic_description_edit_count)
    TextView tvDescpriptionLength;

    @Bean
    EntityClientManager entityClientManager;

    @AfterViews
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

    @TextChange(R.id.et_topic_description_edit_content)
    void onChangeDescriptionText(CharSequence text) {
        tvDescpriptionLength.setText(String.format("%d/%d", text.length(), DESCRIPTION_MAX_LENGTH));
    }

    @OptionsItem(R.id.action_topic_description_save)
    @Background
    void onSaveOptionSelected() {
        TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(entityId);

        String description = etDescpription.getText().toString().trim();

        try {
            if (topicRoom.isPublicTopic()) {
                entityClientManager.modifyChannelDescription(entityId, description);
            } else {
                entityClientManager.modifyPrivateGroupDescription(entityId, description);
            }

            TopicRepository.getInstance().updateDescription(entityId, description);
            TeamInfoLoader.getInstance().refresh();

            setResult(RESULT_OK);
            finish();

        } catch (RetrofitException e) {
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
