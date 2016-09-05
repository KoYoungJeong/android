package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.model.StarredListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model.StarredListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 15. 7. 30..
 */
public class StarredListPresenterImpl implements StarredListPresenter {

    private final StarredListModel starredListModel;
    private final StarredListDataModel starredListDataModel;
    private final View starredListView;
    private boolean isInInitializing = false;

    @Inject
    public StarredListPresenterImpl(StarredListModel starredListModel,
                                    StarredListDataModel starredListDataModel,
                                    View starredListView) {
        this.starredListModel = starredListModel;
        this.starredListDataModel = starredListDataModel;
        this.starredListView = starredListView;
    }

    @Override
    public void onInitializeStarredList(StarredType starredType) {
        isInInitializing = true;
        starredListView.hideEmptyLayout();
        starredListModel.getStarredListObservable(starredType.getName(), -1, StarredListModel.DEFAULT_COUNT)
                .map(resStarMentioned -> {
                    List<MultiItemRecyclerAdapter.Row<?>> rows =
                            starredListDataModel.getStarredListRows(resStarMentioned.getRecords());
                    return Pair.create(resStarMentioned.hasMore(), rows);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Boolean hasMore = pair.first;
                    List<MultiItemRecyclerAdapter.Row<?>> rows = pair.second;

                    starredListDataModel.clear();
                    starredListDataModel.addRows(rows);
                    starredListView.notifyDataSetChanged();

                    starredListView.setHasMore(hasMore);

                    if (rows == null || rows.size() <= 0) {
                        starredListView.showEmptyLayout();
                    }
                }, e -> {
                    LogUtil.e(e.getMessage());
                    starredListDataModel.clear();
                    starredListView.notifyDataSetChanged();
                    starredListView.showEmptyLayout();
                    isInInitializing = false;
                });
    }

    @Override
    public void onLoadMoreAction(StarredType starredType, long offset) {
        starredListView.showMoreProgress();

        starredListModel.getStarredListObservable(
                starredType.getName(), offset, StarredListModel.DEFAULT_COUNT)
                .map(resStarMentioned -> {
                    List<MultiItemRecyclerAdapter.Row<?>> rows =
                            starredListDataModel.getStarredListRows(resStarMentioned.getRecords());
                    return Pair.create(resStarMentioned.hasMore(), rows);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Boolean hasMore = pair.first;
                    List<MultiItemRecyclerAdapter.Row<?>> rows = pair.second;

                    starredListDataModel.addRows(rows);
                    starredListView.notifyDataSetChanged();

                    starredListView.setHasMore(hasMore);
                }, e -> {
                    starredListView.hideMoreProgress();
                });
    }

    @Override
    public void unStarMessage(long messageId) {
        starredListModel.getUnStarMessageObservable(messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resCommon -> {
                    starredListView.showUnStarSuccessToast();
                    starredListDataModel.removeByMessageId(messageId);
                    starredListView.notifyDataSetChanged();
                }, Throwable::printStackTrace);
    }

    @Override
    public void onStarredMessageClick(StarredMessage message) {
        String contentType = message.getMessage().contentType;

        if ("text".equals(contentType)) {

            moveToMessageList(message);

        } else if ("file".equals(contentType)) {

            starredListView.moveToFileDetail(message.getMessage().id, message.getMessage().id);

        } else if ("poll".equals(contentType)) {

            starredListView.moveToPollDetail(message.getMessage().pollId);

        } else if ("comment".equals(contentType)) {

            if ("poll".equals(message.getMessage().feedbackType)) {
                starredListView.moveToPollDetail(message.getMessage().pollId);
            } else if ("file".equals(message.getMessage().feedbackType)) {
                starredListView.moveToFileDetail(
                        message.getMessage().feedbackId, message.getMessage().id);
            }
        }
    }

    @Override
    public void onFileMessageDeleted(long fileMessageId) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    starredListDataModel.removeByMessageId(fileMessageId);
                    starredListView.notifyDataSetChanged();
                });
    }

    @Override
    public void onFileCommentMessageDeleted(long commentId) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    starredListDataModel.removeByMessageId(commentId);
                    starredListView.notifyDataSetChanged();
                });
    }

    @Override
    public void onMessageDeleted(long messageId) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    starredListDataModel.removeByMessageId(messageId);
                    starredListView.notifyDataSetChanged();
                });
    }

    @Override
    public void onMessageUnStarred(long messageId) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    starredListDataModel.removeByMessageId(messageId);
                    starredListView.notifyDataSetChanged();
                });
    }

    @Override
    public void onMessageStarred(long messageId, StarredType starredType) {
        final StarredMessage starredMessage = starredListDataModel.findMessageById(messageId);
        if (starredMessage != null && starredMessage.getStarredId() > 0) {
            return;
        }

        starredListModel.getStarredListObservable(starredType.getName(), -1, 1)
                .concatMap(resStarMentioned -> {
                    if (resStarMentioned == null
                            || resStarMentioned.getRecords() == null
                            || resStarMentioned.getRecords().isEmpty()) {
                        return Observable.defer(() -> Observable.error(new NullPointerException("empty")));
                    }

                    StarredMessage message = resStarMentioned.getRecords().get(0);
                    StarredMessage messageById = starredListDataModel.findMessageById(message.getMessage().id);
                    if (messageById != null && messageById.getStarredId() > 0) {
                        return Observable.defer(() -> Observable.error(new Throwable("already exists")));
                    }

                    return Observable.defer(() ->
                            Observable.just(starredListDataModel.getStarredMessageRow(message)));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    starredListDataModel.addRows(0, rows);
                    starredListView.notifyDataSetChanged();
                }, Throwable::printStackTrace);
    }

    @Override
    public void reInitializeIfEmpty(StarredType starredType) {
        if (isInInitializing) {
            return;
        }

        if (starredListDataModel.isEmpty()) {
            onInitializeStarredList(starredType);
        }
    }

    private void moveToMessageList(StarredMessage message) {
        long roomId = message.getRoom().id;
        Collection<Long> members = TeamInfoLoader.getInstance()
                .getTopic(roomId)
                .getMembers();

        Observable.from(members)
                .takeFirst(memberId -> memberId == TeamInfoLoader.getInstance().getMyId())
                .firstOrDefault(-1L)
                .subscribe(memberId -> {
                    if (memberId == -1L) {
                        return;
                    }

                    String entityTypeStr = message.getRoom().type;
                    int entityType = "channel".equals(entityTypeStr)
                            ? JandiConstants.TYPE_PUBLIC_TOPIC
                            : "privateGroup".equals(entityTypeStr)
                            ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;

                    starredListView.moveToMessageList(
                            message.getTeamId(), roomId, entityType, message.getLinkId());
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));
                    starredListView.showUnJoinedTopicErrorToast();
                });
    }

}