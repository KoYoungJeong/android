package com.tosslab.jandi.app.ui.filedetail.views;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by tee on 15. 9. 30..
 */
@EActivity(R.layout.activity_file_unshare_entity_choose)
public class FileSharedEntityChooseActivity extends BaseAppCompatActivity {
    public static final String KEY_ENTITY_ID = "entity_id";

    public static final int MODE_UNSHARE = 0;
    public static final int MODE_PICK = 1;

    @Extra
    long fileId;

    @Extra
    long[] sharedEntities;

    @Extra
    int mode = MODE_UNSHARE;

    @ViewById(R.id.lv_shared_entity)
    ListView lvSharedEntities;

    @ViewById(R.id.layout_search_bar)
    Toolbar toolbar;

    @Bean
    FileDetailModel fileDetailModel;

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
        if (mode == MODE_PICK) {
            actionBar.setTitle(getString(R.string.jandi_shared_in_room));
        } else {
            actionBar.setTitle(R.string.jandi_title_cdp_to_be_unshared);
        }
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
        long myId = fileDetailModel.getMyId();

        List<Long> sharedEntityWithoutMe = new ArrayList<>();
        List<EntitySimpleListAdapter.SimpleEntity> sharedEntities = new ArrayList<>();

        Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                for (long sharedEntity : FileSharedEntityChooseActivity.this.sharedEntities) {
                    subscriber.onNext(sharedEntity);
                }
                subscriber.onCompleted();
            }
        })
                .distinct()
                .filter(integerWrapper -> integerWrapper != myId)
                .filter(entityId -> TeamInfoLoader.getInstance().isTopic(entityId)
                        || TeamInfoLoader.getInstance().isUser(entityId))
                .map(entityId -> {

                    EntitySimpleListAdapter.SimpleEntity simpleEntity = new EntitySimpleListAdapter.SimpleEntity();
                    simpleEntity.setName(TeamInfoLoader.getInstance().getName(entityId));
                    simpleEntity.setId(entityId);
                    if (TeamInfoLoader.getInstance().isMember(entityId)) {
                        Member member = TeamInfoLoader.getInstance().getMember(entityId);
                        simpleEntity.setPhotoUrl(member.getPhotoUrl());
                        simpleEntity.setStarred(TeamInfoLoader.getInstance().isStarredUser(entityId));
                        simpleEntity.setUser(true);
                    } else {
                        simpleEntity.setStarred(TeamInfoLoader.getInstance().isStarred(entityId));
                    }

                    simpleEntity.setPublic(TeamInfoLoader.getInstance().isPublicTopic(entityId));

                    return simpleEntity;
                })
                .toSortedList((lhs, rhs) -> StringCompareUtil.compare(lhs.getName(), rhs.getName()))
                .collect(() -> sharedEntities, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        if (!sharedEntities.isEmpty()) {

            final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, sharedEntities);
            lvSharedEntities.setAdapter(adapter);
            lvSharedEntities.setOnItemClickListener((adapterView, view, i, l) -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_ENTITY_ID, sharedEntities.get(i).getId());
                setResult(RESULT_OK, returnIntent);
                finish();
            });
        } else {
            showErrorToast(getString(R.string.err_file_has_not_been_shared));
            finish();
        }
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

}
