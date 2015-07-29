package com.tosslab.jandi.app.ui.message.detail.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

@EFragment(R.layout.fragment_chat_detail)
public class ChatDetailFragment extends Fragment {

    @FragmentArg
    int entityId;

    @ViewById(R.id.iv_chat_detail_starred)
    View ivStarred;

    @Bean
    EntityClientManager entityClientManager;

    @Bean
    LeaveViewModel leaveViewModel;

    @Bean
    TopicDetailModel topicDetailModel;

    @AfterViews
    void initViews() {
        setUpActionbar();
        FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);
        boolean isStarred = entity.isStarred;
        setStarred(isStarred);
    }

    private void setUpActionbar() {

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_topic_detail);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

            actionBar.setTitle(R.string.jandi_chat_detail);
        }

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(TopicLeaveEvent event) {

        EventBus.getDefault().post(new ChatCloseEvent(entityId));

        FragmentActivity activity = getActivity();
        Intent data = new Intent();
        data.putExtra(TopicDetailActivity.EXTRA_LEAVE, true);

        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    @Background
    @Click(R.id.vg_chat_detail_starred)
    void onChatStarClick() {
        FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);
        boolean isStarred = entity.isStarred;

        try {

            if (isStarred) {
                entityClientManager.disableFavorite(entityId);

                topicDetailModel.trackTopicUnStarSuccess(entityId);

            } else {
                entityClientManager.enableFavorite(entityId);

                topicDetailModel.trackTopicStarSuccess(entityId);
                showSuccessToast(getString(R.string.jandi_message_starred));
            }

            EntityManager.getInstance(getActivity()).getEntityById(entityId).isStarred = !isStarred;

            setStarred(!isStarred);

        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            if (isStarred) {
                topicDetailModel.trackTopicUnStarFail(errorCode);
            } else {
                topicDetailModel.trackTopicStarFail(errorCode);
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setStarred(boolean isStarred) {
        ivStarred.setSelected(isStarred);
    }

    @UiThread
    void showSuccessToast(String message) {
        ColoredToast.show(getActivity(), message);
    }

    @Click(R.id.vg_chat_detail_leave)
    void onChatLeaveClick() {
        leaveViewModel.initData(getActivity(), entityId);
        leaveViewModel.leave();
    }

}
