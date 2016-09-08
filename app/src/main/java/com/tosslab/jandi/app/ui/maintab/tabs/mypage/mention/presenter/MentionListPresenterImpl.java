package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.model.MentionListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view.MentionListView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collection;
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
public class MentionListPresenterImpl implements MentionListPresenter {

    public static final String TAG = MentionListPresenter.class.getSimpleName();

    private final MentionListModel model;
    private final MentionListView view;

    private boolean isInInitializing = false;

    @Inject
    public MentionListPresenterImpl(MentionListModel model, MentionListView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void onInitializeMyPage(final boolean isRefreshAction) {
        isInInitializing = true;
        view.clearLoadMoreOffset();

        if (!isRefreshAction) {
            view.showProgress();
        }
        model.getMentionsObservable(-1, MentionListModel.MENTION_LIST_LIMIT)
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
                        view.hideEmptyMentionView();
                        view.addMentions(records);
                        view.notifyDataSetChanged();
                    }
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    view.hideProgress();
                    if (isRefreshAction) {
                        view.hideRefreshProgress();
                    } else {
                        view.hideProgress();
                        view.hideEmptyMentionView();
                        view.clearMentions();
                        view.notifyDataSetChanged();
                    }
                    isInInitializing = false;
                });
    }

    @Override
    public void loadMoreMentions(long offset) {
        view.showMoreProgress();

        model.getMentionsObservable(offset, MentionListModel.MENTION_LIST_LIMIT)
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

        } else if (TextUtils.equals("file", contentType)) {
            view.moveToFileDetailActivity(mention.getMessageId(), mention.getMessageId());
        } else if (TextUtils.equals("comment", contentType)) {
            if ("poll".equals(mention.getFeedbackType())) {
                view.moveToPollDetailActivity(mention.getPollId());
            } else {
                view.moveToFileDetailActivity(mention.getFeedbackId(), mention.getMessageId());
            }
        }
    }

    private void onClickTextTypeMessage(MentionMessage mention) {

        if (!TeamInfoLoader.getInstance().isTopic(mention.getRoomId())
                && !TeamInfoLoader.getInstance().isChat(mention.getRoomId())) {
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
        if (TeamInfoLoader.getInstance().isUser(mention.getRoomId())) {
            view.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
            return;
        }

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(mention.getRoomId());
        Long searchedMemberId = Observable.from(topic.getMembers())
                .filter(memberId -> memberId == TeamInfoLoader.getInstance().getMyId())
                .toBlocking()
                .firstOrDefault(-1L);

        if (searchedMemberId != -1L) {
            view.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
        } else {
            view.showUnknownEntityToast();
        }
    }

    @Override
    public void addMentionedMessage(ResMessages.Link link) {
        Observable.just(link)
                .observeOn(Schedulers.io())
                .filter(link1 -> {
                    if (link1.message instanceof ResMessages.TextMessage) {
                        Collection<MentionObject> mentions = ((ResMessages.TextMessage) link1.message).mentions;
                        return mentions != null && !mentions.isEmpty();
                    } else if (link1.message instanceof ResMessages.CommentMessage) {
                        Collection<MentionObject> mentions = ((ResMessages.CommentMessage) link1.message).mentions;
                        return mentions != null && !mentions.isEmpty();
                    } else {
                        return false;
                    }
                })
                .filter(link1 -> {
                    Collection<MentionObject> mentions;
                    if (link1.message instanceof ResMessages.TextMessage) {
                        mentions = ((ResMessages.TextMessage) link1.message).mentions;
                    } else {
                        mentions = ((ResMessages.CommentMessage) link1.message).mentions;
                    }
                    return Observable.from(mentions)
                            .takeFirst(mentionObject -> mentionObject.getId() == TeamInfoLoader.getInstance().getMyId())
                            .map(it -> true)
                            .toBlocking().firstOrDefault(false);
                })
                //내 메션인 있으면 멘션 메세지 받아오기
                .map(link1 -> {
                    long roomId = link1.toEntity.get(0);
                    String roomType;
                    String roomName;
                    String userName;
                    String photoUrl;
                    if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                        if (TeamInfoLoader.getInstance().isPublicTopic(roomId)) {
                            roomType = "channel";
                        } else {
                            roomType = "privateGroup";
                        }
                        roomName = TeamInfoLoader.getInstance().getName(roomId);
                        userName = TeamInfoLoader.getInstance().getName(link1.message.writerId);
                        photoUrl = TeamInfoLoader.getInstance().getUser(link1.message.writerId).getPhotoUrl();
                    } else {
                        roomType = "user";
                        roomName = TeamInfoLoader.getInstance().getName(link1.message.writerId);
                        userName = TeamInfoLoader.getInstance().getName(link1.message.writerId);
                        photoUrl = TeamInfoLoader.getInstance().getUser(link1.message.writerId).getPhotoUrl();
                    }
                    return MentionMessage.createForMentions(link1, roomType, roomName, userName, photoUrl);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mentionMessage -> {
                    // 새로운 멘션 메세지 추가
                    view.addNewMention(mentionMessage);
                    view.notifyDataSetChanged();
                }, t -> {LogUtil.d(TAG, t.getMessage());});

    }

    @Override
    public void reInitializeIfEmpty(boolean isEmpty) {
        if (isEmpty && !isInInitializing) {
            onInitializeMyPage(false);
        }
    }
}
