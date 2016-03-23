package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.maintab.mypage.model.MyPageModel;
import com.tosslab.jandi.app.ui.maintab.mypage.view.MyPageView;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPagePresenterImpl implements MyPagePresenter {

    public static final String TAG = MyPagePresenter.class.getSimpleName();

    private final MyPageModel model;
    private final MyPageView view;

    @Inject
    public MyPagePresenterImpl(MyPageModel model, MyPageView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void onInitialize() {
        view.clearMentions();

        view.setMe(model.getMe());

        view.showProgress();

        model.getMentionsObservable(-1, MyPageModel.MENTION_LIST_LIMIT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resStarMentioned -> {
                    view.hideProgress();

                    view.setHasMore(resStarMentioned.hasMore());

                    List<StarMentionedMessageObject> records = resStarMentioned.getRecords();
                    if (records == null || records.isEmpty()) {
                        view.showEmptyMentionView();
                        return;
                    }

                    List<MentionMessage> convertedMentionList = model.getConvertedMentionList(records);
                    view.addMentions(convertedMentionList);
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    view.hideProgress();
                });
    }

    @Override
    public void loadMoreMentions(long offset) {
        view.showMoreProgress();

        model.getMentionsObservable(offset, MyPageModel.MENTION_LIST_LIMIT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resStarMentioned -> {
                    view.setHasMore(resStarMentioned.hasMore());

                    view.hideMoreProgress();

                    List<StarMentionedMessageObject> records = resStarMentioned.getRecords();
                    if (records == null || records.isEmpty()) {
                        return;
                    }

                    List<MentionMessage> convertedMentionList = model.getConvertedMentionList(records);
                    view.addMentions(convertedMentionList);
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
}
