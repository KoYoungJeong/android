package com.tosslab.jandi.app.ui.search.messages.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

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

    @ViewById(R.id.txt_search_scope_where)
    TextView entityTextView;

    @ViewById(R.id.txt_search_scope_who)
    TextView memberTextView;

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
        messageSearchPresenter.onSelectEntity(event.getEntityId(), event.getName());
    }

    public void onEvent(SelectMemberEvent event) {
        messageSearchPresenter.onSelectMember(event.getMemberId(), event.getName());
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
            dialog.setAdapter(adapter, (dialog1, which) -> {
                EntitySelectDialogAdatper.SimpleEntityInfo item = ((EntitySelectDialogAdatper) ((AlertDialog) dialog1).getListView().getAdapter()).getItem(which);
                EventBus.getDefault().post(new SelectEntityEvent(item.getId(), item.getName()));
            });

            List<FormattedEntity> categorizableEntities = entityManager.getCategorizableEntities();

            FormattedEntity me = entityManager.getMe();

            Iterable<EntitySelectDialogAdatper.SimpleEntityInfo> entityInfoIterable = Observable.from(categorizableEntities)
                    .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                    .filter(entity -> entity.getId() != me.getId())
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

            adapter.add(new EntitySelectDialogAdatper.SimpleEntityInfo(-1, context.getString(R.string.jandi_file_category_everywhere), -1, ""));

            for (EntitySelectDialogAdatper.SimpleEntityInfo simpleEntityInfo : entityInfoIterable) {
                adapter.add(simpleEntityInfo);
            }

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

    @Override
    public void setEntityName(String name) {
        entityTextView.setText(name);
    }

    @Override
    public void setMemberName(String name) {
        memberTextView.setText(name);
    }
}
