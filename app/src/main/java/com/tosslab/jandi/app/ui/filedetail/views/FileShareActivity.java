package com.tosslab.jandi.app.ui.filedetail.views;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 9. 30..
 */

@EActivity(R.layout.activity_file_share_entity_choose)
public class FileShareActivity extends BaseAppCompatActivity {

    public static final int REQUEST_CODE_SHARE = 0;
    public static final int REQUEST_CODE_UNSHARE = 1;
    public static final String KEY_ENTITY_ID = "entity_id";

    @Extra
    int fileId;
    @Bean
    FileDetailModel fileDetailModel;
    @ViewById(R.id.layout_search_bar)
    Toolbar toolbar;
    @ViewById(R.id.view_container)
    View container;

    @AfterViews
    void initViews() {
        setupActionbar();
        showList();
    }

    void setupActionbar() {
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_title_cdp_to_be_shared);
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

    public void showList() {
        final List<FormattedEntity> unSharedEntities = fileDetailModel.getUnsharedEntities();

        List<FormattedEntity> topics = new ArrayList<>();
        List<FormattedEntity> users = new ArrayList<>();

        for (FormattedEntity entity : unSharedEntities) {
            if (entity.isUser() || entity instanceof BotEntity) {
                users.add(entity);
            } else {
                topics.add(entity);
            }
        }

        if (!topics.isEmpty() || !users.isEmpty()) {
            RoomSelector roomSelector = new RoomSelectorImpl(topics, users);
            roomSelector.setOnRoomSelectListener(item -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_ENTITY_ID, item.getEntityId());
                setResult(RESULT_OK, returnIntent);
                finish();
            });
            roomSelector.setType(RoomSelectorImpl.TYPE_VIEW);
            roomSelector.show(container);
        } else {
            showErrorToast(getString(R.string.err_file_already_shared_all_topics));
            finish();
        }
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError( message);
    }

}