package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.ui.maintab.mypage.model.MyPageModel;
import com.tosslab.jandi.app.ui.maintab.mypage.view.MyPageView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPagePresenterImpl implements MyPagePresenter {

    public static final String TAG = MyPagePresenter.class.getSimpleName();

    private final MyPageModel model;
    private final MyPageView view;

    private Subscription mentionInitializeQueueSubscription;
    private PublishSubject<Object> mentionInitializeQueue;

    @Inject
    public MyPagePresenterImpl(MyPageModel model, MyPageView view) {
        this.model = model;
        this.view = view;

        initializeMentionInitializeQueue();
    }

    @Override
    public void initializeMentionInitializeQueue() {
        mentionInitializeQueue = PublishSubject.create();
        mentionInitializeQueueSubscription =
                mentionInitializeQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> onInitializeMyPage(false));
    }

    @Override
    public void onInitializeMyPage(final boolean isRefreshAction) {
        view.clearLoadMoreOffset();

        if (!isRefreshAction) {
            view.showProgress();
        }
        model.getMentionsObservable(-1, MyPageModel.MENTION_LIST_LIMIT)
                .concatMap(model::getConvertedMentionObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    if (isRefreshAction) {
                        view.hideRefreshProgress();
                    } else {
                        view.hideProgress();
                    }

                    view.setHasMore(pair.first);

                    view.clearMentions();

                    List<MentionMessage> records = pair.second;
                    if (records == null || records.isEmpty()) {
                        view.showEmptyMentionView();
                    } else {
                        view.addMentions(records);
                    }
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    view.hideProgress();
                    if (isRefreshAction) {
                        view.hideRefreshProgress();
                    } else {
                        view.hideProgress();
                    }
                }, view::notifyDataSetChanged);
    }

    @Override
    public void onRetrieveMyInfo() {
        view.setMe(model.getMe());
    }

    @Override
    public void loadMoreMentions(long offset) {
        view.showMoreProgress();

        model.getMentionsObservable(offset, MyPageModel.MENTION_LIST_LIMIT)
                .concatMap(model::getConvertedMentionObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    view.hideMoreProgress();

                    view.setHasMore(pair.first);

                    List<MentionMessage> records = pair.second;
                    if (records == null || records.isEmpty()) {
                        return;
                    }

                    view.addMentions(records);
                    view.notifyDataSetChanged();
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    view.hideMoreProgress();
                });
    }

    @Override
    public void onClickMention(MentionMessage mention) {
        String contentType = mention.getContentType();
        if (TextUtils.equals("text", contentType)) {

            onClickTextTypeMessage(mention);

        } else if (TextUtils.equals("file", contentType)
                || TextUtils.equals("comment", contentType)) {

            long fileId = TextUtils.equals("file", contentType)
                    ? mention.getMessageId() : mention.getFeedbackId();

            view.moveToFileDetailActivity(fileId, mention.getMessageId());
        }
    }

    private void onClickTextTypeMessage(MentionMessage mention) {
        final EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(mention.getRoomId());
        if (entity == EntityManager.UNKNOWN_USER_ENTITY) {
            view.showUnknownEntityToast();
            return;
        }

        long teamId = mention.getTeamId();
        long entityId = mention.getRoomId();

        String roomType = mention.getRoomType();
        int entityType = TextUtils.equals("channel", roomType)
                ? JandiConstants.TYPE_PUBLIC_TOPIC
                : TextUtils.equals("privateGroup", roomType)
                ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;
        long roomId = entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityId : -1;
        long linkId = mention.getLinkId();
        if (entity.isUser() || entity instanceof BotEntity) {
            view.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
            return;
        }

        Long searchedMemberId = Observable.from(entity.getMembers())
                .filter(memberId -> memberId == entityManager.getMe().getId())
                .toBlocking()
                .firstOrDefault(-1L);

        if (searchedMemberId != -1L) {
            view.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
        } else {
            view.showUnknownEntityToast();
        }
    }

    @Override
    public void onNewMentionComing(long teamId, @Nullable Date latestCreatedAt) {
        if (teamId != EntityManager.getInstance().getTeamId()) {
            return;
        }

        mentionInitializeQueue.onNext(new Object());
    }

    @Override
    public void clearMentionInitializeQueue() {
        if (mentionInitializeQueueSubscription != null
                && !mentionInitializeQueueSubscription.isUnsubscribed()) {

            mentionInitializeQueueSubscription.unsubscribe();
        }
    }
}
