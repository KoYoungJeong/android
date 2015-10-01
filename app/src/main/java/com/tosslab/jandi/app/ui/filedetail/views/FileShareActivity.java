package com.tosslab.jandi.app.ui.filedetail.views;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResMessages;
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
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 15. 9. 30..
 */

@EActivity(R.layout.activity_file_share_entity_choose)
public class FileShareActivity extends BaseAppCompatActivity {

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

        // 프레임워크 내부 프로세스 상 딜레이가 없으면 실행이 안됨
        Observable.just(1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Integer -> {
                    showList();
                });
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
        ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage(fileId);

        final List<FormattedEntity> unSharedEntities
                = fileDetailModel.getUnsharedEntities(fileMessage);
        int myId = fileDetailModel.getMyId();

        List<FormattedEntity> topics = new ArrayList<>();
        List<FormattedEntity> users = new ArrayList<>();

        for (FormattedEntity entity : unSharedEntities) {
            if (entity.isUser() && entity.getId() != myId) {
                users.add(entity);
            } else {
                topics.add(entity);
            }
        }

        if (!users.isEmpty() || !users.isEmpty()) {
            RoomSelector roomSelector = new RoomSelectorImpl(topics, users);
            roomSelector.setOnRoomSelectListener(item -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("EntityId", item.getEntityId());
                setResult(RESULT_OK, returnIntent);
                roomSelector.dismiss();
            });
            roomSelector.setOnRoomDismissListener(() -> finish());
            roomSelector.show(container, false);
        } else {
            showErrorToast(getString(R.string.err_file_already_shared_all_topics));
            finish();
        }
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError(this, message);
    }

}