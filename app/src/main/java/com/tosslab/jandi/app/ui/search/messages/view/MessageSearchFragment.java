package com.tosslab.jandi.app.ui.search.messages.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SelectMemberEvent;
import com.tosslab.jandi.app.events.search.MoreSearchRequestEvent;
import com.tosslab.jandi.app.events.search.NewSearchRequestEvent;
import com.tosslab.jandi.app.events.search.SelectEntityEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.EntitySelectDialogAdatper;
import com.tosslab.jandi.app.ui.search.messages.adapter.MemberSelectDialogAdapter;
import com.tosslab.jandi.app.ui.search.messages.adapter.MessageSearchResultAdapter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenterImpl;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EFragment(R.layout.fragment_message_search)
public class MessageSearchFragment extends Fragment implements MessageSearchPresenter.View {

    @Bean(MessageSearchPresenterImpl.class)
    MessageSearchPresenter messageSearchPresenter;

    @ViewById(R.id.list_search_messages)
    RecyclerView searchListView;

    private Dialog memberSelectDialog;
    private Dialog entitySelectDialog;

    private MessageSearchResultAdapter messageSearchResultAdapter;

    @AfterViews
    void initObject() {
        messageSearchPresenter.setView(this);

        FragmentActivity parentActivity = getActivity();
        searchListView.setLayoutManager(new LinearLayoutManager(parentActivity));
        messageSearchResultAdapter = new MessageSearchResultAdapter(parentActivity);
        searchListView.setAdapter(messageSearchResultAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Click(R.id.layout_search_scope_where)
    void onEntityClick() {
        messageSearchPresenter.onEntityClick();
    }

    @Click(R.id.layout_search_scope_who)
    void onMemberClick() {
        messageSearchPresenter.onMemberClick();
    }

    public void onEvent(NewSearchRequestEvent event) {
        messageSearchPresenter.onSearchRequest(event.getQuery());
    }

    public void onEvent(MoreSearchRequestEvent event) {
        messageSearchPresenter.onMoreSearchRequest();
    }

    public void onEvent(SelectEntityEvent event) {

    }

    public void onEvent(SelectMemberEvent event) {

    }

    @UiThread
    @Override
    public void clearSearchResult() {
        messageSearchResultAdapter.clear();
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @UiThread
    @Override
    public void addSearchResult(List<ResMessageSearch.SearchRecord> searchRecords) {
        messageSearchResultAdapter.addAll(searchRecords);
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEntityDialog() {
        if (entitySelectDialog != null && entitySelectDialog.isShowing()) {
            return;
        }

        if (entitySelectDialog == null) {

            Context context = getActivity();
            EntityManager entityManager = EntityManager.getInstance(context);

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.jandi_file_search_entity);

            EntitySelectDialogAdatper adapter = new EntitySelectDialogAdatper(context);
            dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EntitySelectDialogAdatper.SimpleEntityInfo item = ((EntitySelectDialogAdatper) ((AlertDialog) dialog).getListView().getAdapter()).getItem(which);
                }
            });

            List<FormattedEntity> categorizableEntities = entityManager.getCategorizableEntities();

            Iterable<EntitySelectDialogAdatper.SimpleEntityInfo> entityInfoIterable = Observable.from(categorizableEntities)
                    .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                    .map(entity -> {

                        int id = entity.getId();
                        String name = entity.getName();
                        int type;
                        String photo;

                        if (entity.isPublicTopic()) {
                            type = JandiConstants.TYPE_PUBLIC_TOPIC;
                            photo = "";
                        } else if (entity.isPrivateGroup()) {
                            type = JandiConstants.TYPE_PRIVATE_TOPIC;
                            photo = "";
                        } else {
                            type = JandiConstants.TYPE_DIRECT_MESSAGE;
                            photo = entity.getUserSmallProfileUrl();
                        }

                        return new EntitySelectDialogAdatper.SimpleEntityInfo(type, name, id, photo);
                    }).toBlocking()
                    .toIterable();

            for (EntitySelectDialogAdatper.SimpleEntityInfo simpleEntityInfo : entityInfoIterable) {
                adapter.add(simpleEntityInfo);
            }

            FormattedEntity me = entityManager.getMe();

            for (int idx = categorizableEntities.size() - 1; idx >= 0; idx--) {
                FormattedEntity formattedEntity = categorizableEntities.get(idx);
                if (formattedEntity.isUser()) {
                    if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                        categorizableEntities.remove(idx);
                    } else if (formattedEntity.getId() == me.getId()) {
                        categorizableEntities.remove(idx);
                    }
                }
            }

//            final EntitySimpleListAdapter adapterasdasd = new EntitySimpleListAdapter(context, categorizableEntities);
//            lv.setAdapter(adapterasdasd);
//            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    if (entitySelectDialog != null)
//                        entitySelectDialog.dismiss();
//
//                    int sharedEntityId = CategorizingAsEntity.EVERYWHERE;
//
//                    if (i <= 0) {
//                        // 첫번째는 "Everywhere"인 더미 entity
//                        mCurrentEntityCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everywhere);
//                    } else {
//                        FormattedEntity sharedEntity = adapterasdasd.getItem(i);
//                        sharedEntityId = sharedEntity.getId();
//                        mCurrentEntityCategorizingAccodingBy = sharedEntity.getName();
//                    }
//                    textVew.setText(mCurrentEntityCategorizingAccodingBy);
//                    EventBus.getDefault().post(new CategorizingAsEntity(sharedEntityId));
//                }
//            });


            entitySelectDialog = dialog.create();
        }
        entitySelectDialog.show();
    }

    @Override
    public void showMemberDialog() {
        if (memberSelectDialog != null && memberSelectDialog.isShowing()) {
            return;
        }

        if (memberSelectDialog == null) {

            Context context = getActivity();
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.jandi_file_search_user);
            MemberSelectDialogAdapter adapter = new MemberSelectDialogAdapter(context);

            EntityManager entityManager = EntityManager.getInstance(context);
            List<FormattedEntity> formattedUsersWithoutMe = entityManager.getFormattedUsersWithoutMe();

            Iterable<MemberSelectDialogAdapter.SimpleMemberInfo> simpleMemberInfoIterable = Observable.from(formattedUsersWithoutMe)
                    .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                    .map(entity -> new MemberSelectDialogAdapter.SimpleMemberInfo(entity.getId(), entity.getName(), entity.getUserSmallProfileUrl()))
                    .toBlocking()
                    .toIterable();

            List<MemberSelectDialogAdapter.SimpleMemberInfo> simpleMemberInfos = new ArrayList<MemberSelectDialogAdapter.SimpleMemberInfo>();

            for (MemberSelectDialogAdapter.SimpleMemberInfo simpleMemberInfo : simpleMemberInfoIterable) {
                simpleMemberInfos.add(simpleMemberInfo);
            }

            FormattedEntity me = entityManager.getMe();

            simpleMemberInfos.add(0, new MemberSelectDialogAdapter.SimpleMemberInfo(me.getId(), me.getName(), me.getUserSmallProfileUrl()));
            simpleMemberInfos.add(0, new MemberSelectDialogAdapter.SimpleMemberInfo(0, context.getString(R.string.jandi_file_category_everyone), ""));

            dialogBuilder.setAdapter(adapter, (dialog, which) -> {

                MemberSelectDialogAdapter dialogAdapter = (MemberSelectDialogAdapter) ((AlertDialog) dialog).getListView().getAdapter();
                MemberSelectDialogAdapter.SimpleMemberInfo item = dialogAdapter.getItem(which);

                EventBus.getDefault().post(new SelectMemberEvent(item.getMemberId(), item.getName()));
            });

            memberSelectDialog = dialogBuilder.create();
        }
        memberSelectDialog.show();
    }
}
