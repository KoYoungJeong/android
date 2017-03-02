package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.RefreshMentionBadgeCountEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialMentionInfoRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.model.MentionListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.model.MentionListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view.MentionListView;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MentionListPresenterImpl implements MentionListPresenter {

    public static final String TAG = MentionListPresenter.class.getSimpleName();

    private final MentionListModel mentionListModel;
    private final MentionListView mentionListView;
    private final MentionListDataModel mentionListDataModel;

    private boolean isInInitializing = false;

    private long actuallLastMarkerId = -1;

    @Inject
    public MentionListPresenterImpl(MentionListModel mentionListModel,
                                    MentionListDataModel mentionListDataModel,
                                    MentionListView mentionListView) {
        this.mentionListModel = mentionListModel;
        this.mentionListDataModel = mentionListDataModel;
        this.mentionListView = mentionListView;
    }

    @Override
    public void onInitializeMyPage(final boolean isRefreshAction, final boolean doUpdateLastMessage) {
        isInInitializing = true;
        mentionListView.clearLoadMoreOffset();

        long lastReadMentionId = mentionListModel.getLastReadMentionId();
        actuallLastMarkerId = lastReadMentionId;
        mentionListDataModel.setLastReadMessageId(lastReadMentionId);

        mentionListModel.getMentionsObservable(-1, MentionListModel.MENTION_LIST_LIMIT)
                .doOnNext(resStarMentioned -> {
                    if (doUpdateLastMessage) {
                        List<StarredMessage> records = resStarMentioned.getRecords();
                        if (records != null && !(records.isEmpty())) {
                            try {
                                mentionListModel.updateLastReadMessageId(records.get(0).getMessage().id);
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .map(resStarMentioned -> {
                    List<MentionMessage> mentionMessages =
                            mentionListModel.getConvertedMentionList(resStarMentioned.getRecords());
                    return Pair.create(resStarMentioned, mentionMessages);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    if (isRefreshAction) {
                        mentionListView.hideRefreshProgress();
                    }

                    ResStarMentioned resStarMentioned = pair.first;
                    mentionListView.setHasMore(resStarMentioned.hasMore());

                    mentionListDataModel.clear();

                    List<MentionMessage> mentionMessages = pair.second;
                    if (mentionMessages == null || mentionMessages.isEmpty()) {
                        mentionListView.showEmptyMentionView();
                    } else {
                        mentionListView.hideEmptyMentionView();
                        mentionListDataModel.addAll(mentionMessages);
                    }
                    mentionListView.notifyDataSetChanged();
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    if (isRefreshAction) {
                        mentionListView.hideRefreshProgress();
                    } else {
                        mentionListView.hideEmptyMentionView();
                        mentionListDataModel.clear();
                        mentionListView.notifyDataSetChanged();
                    }
                    isInInitializing = false;
                });
    }

    @Override
    public void loadMoreMentions(long offset) {
        mentionListView.showMoreProgress();

        mentionListModel.getMentionsObservable(offset, MentionListModel.MENTION_LIST_LIMIT)
                .map(resStarMentioned -> {
                    List<MentionMessage> mentionMessages =
                            mentionListModel.getConvertedMentionList(resStarMentioned.getRecords());
                    return Pair.create(resStarMentioned, mentionMessages);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    mentionListView.hideMoreProgress();

                    ResStarMentioned resStarMentioned = pair.first;
                    mentionListView.setHasMore(resStarMentioned.hasMore());

                    List<MentionMessage> mentionMessages = pair.second;
                    if (mentionMessages == null || mentionMessages.isEmpty()) {
                        return;
                    }

                    mentionListDataModel.addAll(mentionMessages);
                    mentionListView.notifyDataSetChanged();
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    mentionListView.hideMoreProgress();
                });
    }

    @Override
    public void onClickMention(MentionMessage mention) {
        String contentType = mention.getContentType();

        if (TextUtils.equals("text", contentType)) {
            onClickTextTypeMessage(mention);
        } else if (TextUtils.equals("file", contentType)) {
            mentionListView.moveToFileDetailActivity(mention.getMessageId(), mention.getMessageId());
        } else if (TextUtils.equals("comment", contentType)) {
            if ("poll".equals(mention.getFeedbackType())) {
                mentionListView.moveToPollDetailActivity(mention.getPollId());
            } else {
                mentionListView.moveToFileDetailActivity(mention.getFeedbackId(), mention.getMessageId());
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionTab_ChooseFileComment);
            }
        }
    }

    private void onClickTextTypeMessage(MentionMessage mention) {

        if (!TeamInfoLoader.getInstance().isTopic(mention.getRoomId())
                && !TeamInfoLoader.getInstance().isChat(mention.getRoomId())) {
            mentionListView.showUnknownEntityToast();
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
            mentionListView.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
            return;
        }

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(mention.getRoomId());
        Long searchedMemberId = Observable.from(topic.getMembers())
                .filter(memberId -> memberId == TeamInfoLoader.getInstance().getMyId())
                .toBlocking()
                .firstOrDefault(-1L);

        if (searchedMemberId != -1L) {
            mentionListView.moveToMessageListActivity(teamId, entityId, entityType, roomId, linkId);
        } else {
            mentionListView.showUnknownEntityToast();
        }


        if (TextUtils.isEmpty(mention.getFeedbackTitle())) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionTab_ChooseTopicMsg);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionTab_ChooseFileComment);
        }

    }

    @Override
    public void addMentionedMessage(ResMessages.Link link) {
        Observable.just(link)
                .observeOn(Schedulers.io())
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
                    mentionListView.hideEmptyMentionView();
                    // 새로운 멘션 메세지 추가
                    if (mentionListDataModel.indexOfLink(mentionMessage.getLinkId()) < 0) {
                        mentionListDataModel.add(0, mentionMessage);
                        mentionListView.notifyDataSetChanged();
                    }
                }, t -> {
                    LogUtil.d(TAG, t.getMessage());
                });

    }

    @Override
    public void reInitializeIfEmpty() {
        boolean isEmpty = mentionListDataModel.getItemCount() <= 0;
        if (isEmpty && !isInInitializing) {
            onInitializeMyPage(false, true);
        }
    }

    @Override
    public void removeMentionedMessage(long linkId) {
        Observable.just(linkId)
                .map(mentionListDataModel::indexOfLink)
                .filter(index -> index >= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(index -> {
                    mentionListDataModel.remove(index);
                    mentionListView.notifyDataSetChanged();
                });
    }

    @Override
    public void onStarred(long messageId) {
        Completable.fromCallable(() -> {
            mentionListModel.registerStarred(messageId);
            return Completable.complete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mentionListView::successStarredMessage, t -> {
                    boolean success = false;
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getResponseCode() == 40015) {
                            success = true;
                        }
                    }
                    if (success) {
                        mentionListView.successStarredMessage();
                    } else {
                        mentionListView.failStarredMessage();
                    }
                });
    }

    @Override
    public void onUpdateMentionMarker() {
        MentionMessage item = mentionListDataModel.getItem(0);
        if (item != null && actuallLastMarkerId > 0
                && item.getMessageId() > actuallLastMarkerId) {
            Completable.fromCallable(() -> {
                mentionListModel.updateLastReadMessageId(item.getMessageId());
                return true;
            }).subscribeOn(Schedulers.newThread())
                    .subscribe(() -> {
                        InitialMentionInfoRepository.getInstance(TeamInfoLoader.getInstance().getTeamId()).clearUnreadCount();
                        TeamInfoLoader.getInstance().refreshMention();
                        EventBus.getDefault().post(new RefreshMentionBadgeCountEvent());
                    }, Throwable::printStackTrace);
            actuallLastMarkerId = item.getMessageId();
        }
    }

}
