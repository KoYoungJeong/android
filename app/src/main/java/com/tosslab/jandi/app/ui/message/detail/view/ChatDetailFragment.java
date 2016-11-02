package com.tosslab.jandi.app.ui.message.detail.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.dagger.DaggerChatDetailComponent;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicStar;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicUnStar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ChatDetailFragment extends Fragment {

    private static final String EXTRA_ENTITY_ID = "entityId";
    long entityId;

    @Bind(R.id.iv_chat_detail_starred)
    View ivStarred;

    EntityClientManager entityClientManager;

    @Inject
    LeaveViewModel leaveViewModel;

    @Inject
    TopicDetailModel topicDetailModel;

    @Bind(R.id.vg_chat_detail_starred)
    View vgStarred;

    public static Fragment createFragment(Context context, long entityId) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ENTITY_ID, entityId);
        return Fragment.instantiate(context, ChatDetailFragment.class.getName(), bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DaggerChatDetailComponent.builder()
                .build()
                .inject(this);

        entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
        initArgument();

        initViews();
    }

    private void initArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            entityId = bundle.getLong(EXTRA_ENTITY_ID);
        }

    }

    void initViews() {
        setUpActionbar();
        boolean isStarred = TeamInfoLoader.getInstance().isStarredUser(entityId);
        setStarred(isStarred);
        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MessageDescription);

    }

    private void setUpActionbar() {

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_topic_detail);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
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

    public void onEventMainThread(MemberStarredEvent event) {
        if (event.getId() == entityId) {
            boolean isStarred = TeamInfoLoader.getInstance().isStarredUser(entityId);
            setStarred(isStarred);
        }
    }

    @OnClick(R.id.vg_chat_detail_starred)
    void onChatStarClick() {

        Observable<Boolean> starredUser = Observable.just(TeamInfoLoader.getInstance().isStarredUser(entityId))
                .share();

        Action1<Boolean> successAction = isStarred -> {
            HumanRepository.getInstance().updateStarred(entityId, !isStarred);
            TeamInfoLoader.getInstance().refresh();

            setStarred(!isStarred);
            if (!isStarred) {
                showSuccessToast(getString(R.string.jandi_message_starred));
            } else {
                showSuccessToast(getString(R.string.jandi_message_no_starred));
            }
        };
        starredUser.filter(it -> it)
                .doOnNext(isStarred -> {
                    try {
                        entityClientManager.disableFavorite(entityId);
                        SprinklrTopicUnStar.sendLog(entityId);
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.Off);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(successAction, Throwable::printStackTrace);

        starredUser.filter(it -> !it)
                .doOnNext(isStarred -> {
                    try {
                        entityClientManager.enableFavorite(entityId);
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.On);
                        SprinklrTopicStar.sendLog(entityId);
                    } catch (RetrofitException e) {
                        int errorCode = e.getStatusCode();
                        if (TeamInfoLoader.getInstance().isStarredUser(entityId)) {
                            SprinklrTopicUnStar.sendFailLog(errorCode);
                        } else {
                            SprinklrTopicStar.sendFailLog(errorCode);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(successAction);

    }

    void setStarred(boolean isStarred) {
        ivStarred.setSelected(isStarred);
    }

    void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @OnClick(R.id.vg_chat_detail_leave)
    void onChatLeaveClick() {
        leaveViewModel.leave(entityId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageDescription, AnalyticsValue.Action.Leave);
    }

}
