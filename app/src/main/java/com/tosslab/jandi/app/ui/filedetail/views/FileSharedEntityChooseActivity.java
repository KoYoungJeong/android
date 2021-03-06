package com.tosslab.jandi.app.ui.filedetail.views;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

public class FileSharedEntityChooseActivity extends BaseAppCompatActivity {
    public static final String KEY_ENTITY_ID = "entity_id";

    public static final int MODE_UNSHARE = 0;
    public static final int MODE_PICK = 1;

    @Nullable
    @InjectExtra
    long fileId;

    @Nullable
    @InjectExtra
    long[] sharedEntities;

    @Nullable
    @InjectExtra
    int mode = MODE_UNSHARE;

    @Bind(R.id.lv_shared_entity)
    ListView lvSharedEntities;

    @Bind(R.id.layout_search_bar)
    Toolbar toolbar;

    @Inject
    FileDetailModel fileDetailModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_unshare_entity_choose);
        ButterKnife.bind(this);
        Dart.inject(this);
        DaggerFileShareComponent.builder()
                .build()
                .inject(this);
        initViews();
    }

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

        List<EntitySimpleListAdapter.SimpleEntity> simpleEntities = new ArrayList<>();
        boolean guest = TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;

        Observable.create((Subscriber<? super Long> subscriber) -> {
            if (sharedEntities != null) {
                for (long sharedEntity : sharedEntities) {
                    subscriber.onNext(sharedEntity);
                }
            }
            subscriber.onCompleted();
        }).distinct()
                .filter(integerWrapper -> integerWrapper != myId)
                .filter(entityId -> {
                    if (!guest) {
                        return TeamInfoLoader.getInstance().isTopic(entityId)
                                || TeamInfoLoader.getInstance().isUser(entityId);
                    }

                    if (TeamInfoLoader.getInstance().isTopic(entityId)) {
                        return TeamInfoLoader.getInstance().getTopic(entityId).isJoined();
                    } else if (TeamInfoLoader.getInstance().isUser(entityId)) {
                        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                                .filter(TopicRoom::isJoined)
                                .map(TopicRoom::getMembers)
                                .takeFirst(its -> its.contains(entityId))
                                .map(its -> true)
                                .defaultIfEmpty(false)
                                .toBlocking().firstOrDefault(false);
                    }
                    return false;
                })
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
                .collect(() -> simpleEntities, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        if (!simpleEntities.isEmpty()) {

            final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, simpleEntities);
            lvSharedEntities.setAdapter(adapter);
            lvSharedEntities.setOnItemClickListener((adapterView, view, i, l) -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_ENTITY_ID, simpleEntities.get(i).getId());
                setResult(RESULT_OK, returnIntent);
                finish();
            });
        } else {
            showErrorToast(getString(R.string.err_file_has_not_been_shared));
            finish();
        }
    }

    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

}
