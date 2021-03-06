package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ShowMoreSharedEntitiesEvent;
import com.tosslab.jandi.app.events.files.FileStarredStateChangeEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.widget.LinkedEllipsizeTextView;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.EntitySpannable;
import com.tosslab.jandi.app.views.spannable.MessageSpannable;

import java.util.Collection;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tonyjs on 16. 1. 19..
 * <p>
 * 파일의 기본 정보 (올린 사람, 시간, 타입, 확장자, 사이즈 등등) 등을 "고정적으로" 그린다.
 * 파일의 형태에 따라 컨텐츠 영역을 다르게 보여주어야 하는데,
 * 이 클래스를 상속 후 #addContentView 안에서 컨텐츠 레이아웃을 addToggledUser 한 후 구현한다.
 */
public abstract class FileViewHolder extends BaseViewHolder<ResMessages.FileMessage> {

    @Bind(R.id.tv_file_detail_user_name)
    TextView tvUserName;
    @Bind(R.id.iv_file_detail_user_profile)
    ImageView ivUserProfile;
    @Bind(R.id.v_file_detail_user_name_disable_indicator)
    View vUserNameDisableIndicator;
    @Bind(R.id.v_file_detail_user_profile_disable_indicator)
    View vUserProfileDisableIndicator;
    @Bind(R.id.tv_file_detail_create_date)
    TextView tvCreatedDate;
    @Bind(R.id.tv_file_detail_file_info)
    TextView tvFileInfo;
    @Bind(R.id.vg_file_detail_info)
    ViewGroup vgFileInfo;
    @Bind(R.id.btn_file_detail_star)
    View btnStar;
    @Bind(R.id.vg_file_detail_deleted)
    View vDeleted;
    @Bind(R.id.tv_file_detail_deleted_date)
    TextView tvDeletedDate;
    @Bind(R.id.tv_file_detail_shared_topics)
    LinkedEllipsizeTextView tvSharedTopics;
    @Bind(R.id.vg_profile_absence)
    ViewGroup vgProfileAbsence;
    private Context context;
    private ViewGroup vgFileContent;


    protected FileViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        vgFileContent = (ViewGroup) itemView.findViewById(R.id.vg_file_detail_content);
        addContentView(vgFileContent);
        ButterKnife.bind(this, itemView);
        initView();
        tvSharedTopics.setOnRequestMoreClickListener(() ->
                EventBus.getDefault().post(new ShowMoreSharedEntitiesEvent()));
    }

    public static View getItemView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(R.layout.layout_file_detail, parent, false);
    }

    protected abstract void initView();

    public abstract void addContentView(ViewGroup parent);

    @Override
    public void onBindView(ResMessages.FileMessage fileMessage) {

        long writerId = fileMessage.writerId;

        User writer = TeamInfoLoader.getInstance().getUser(writerId);
        ProfileBinder.newInstance(tvUserName, vUserNameDisableIndicator,
                ivUserProfile, vgProfileAbsence, vUserProfileDisableIndicator)
                .bind(writer);

        String createTime = DateTransformator.getTimeString(fileMessage.createTime);
        tvCreatedDate.setText(createTime);

        // 삭제된 파일 인 경우
        if (isDeleted(fileMessage.status)) {
            vgFileContent.setVisibility(View.GONE);
            vgFileInfo.setVisibility(View.GONE);

            vDeleted.setVisibility(View.VISIBLE);
            btnStar.setVisibility(View.GONE);

            tvDeletedDate.setText(getDeletedDate(fileMessage.updateTime));
            return;
        }

        vDeleted.setVisibility(View.GONE);
        vgFileInfo.setVisibility(View.VISIBLE);

        btnStar.setSelected(fileMessage.isStarred);
        btnStar.setOnClickListener(v -> {
            boolean nextStarredState = !v.isSelected();
            v.setSelected(nextStarredState);
            EventBus.getDefault().post(new FileStarredStateChangeEvent(nextStarredState));
        });

        ResMessages.FileContent content = fileMessage.content;

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            tvFileInfo.setText(fileMessage.content.ext);
        } else {
            String fileSize = FileUtil.formatFileSize(content.size);
            tvFileInfo.setText(fileSize);
        }

        bindFileContent(fileMessage);

        bindSharedTopics(fileMessage.shareEntities);
    }

    protected boolean isDeleted(String status) {
        return TextUtils.equals(status, "archived");
    }

    private String getDeletedDate(Date updateTime) {
        String time = DateTransformator.getTimeString(updateTime);
        return context.getResources().getString(R.string.jandi_file_deleted_with_date, time);
    }

    public abstract void bindFileContent(final ResMessages.FileMessage fileMessage);

    protected void bindSharedTopics(
            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities) {

        Resources resources = context.getResources();

        if (shareEntities == null || shareEntities.isEmpty()) {
            tvSharedTopics.setText(resources.getString(R.string.jandi_nowhere_shared_file));
            return;
        }


        final long teamId = TeamInfoLoader.getInstance().getTeamId();

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int sharedIndicatorSize = (int) resources.getDimension(R.dimen.jandi_text_size_small);
        int sharedIndicatorColor = resources.getColor(R.color.jandi_text_medium);
        MessageSpannable sharedIndicatorSpannable =
                new MessageSpannable(sharedIndicatorSize, sharedIndicatorColor);

        ssb.append(resources.getString(R.string.jandi_shared_in_room))
                .setSpan(sharedIndicatorSpannable,
                        0, ssb.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("  ");
        int firstLength = ssb.length();

        boolean guest = TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;

        Observable.from(shareEntities)
                .distinct(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .filter(id -> {
                    if (ssb.length() != firstLength) {
                        ssb.append(", ");
                    }
                    if (guest) {
                        if (TeamInfoLoader.getInstance().isTopic(id)) {
                            // 해당 토픽이 내가 참여 중인지 확인함
                            return TeamInfoLoader.getInstance().getTopic(id).isJoined();
                        } else if (TeamInfoLoader.getInstance().isUser(id)) {
                            // 내가 참여한 토픽에 참여한 멤버인지 확인함
                            return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                                    .filter(TopicRoom::isJoined)
                                    .map(TopicRoom::getMembers)
                                    .takeFirst(longs -> longs.contains(id))
                                    .map(it -> true)
                                    .toBlocking().firstOrDefault(false);
                        } else {
                            return false;
                        }
                    } else {
                        return TeamInfoLoader.getInstance().isTopic(id)
                                || TeamInfoLoader.getInstance().isUser(id);
                    }
                })
                .doOnNext(id -> {

                })
                .subscribe(id -> {
                    int entityType;

                    TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
                    if (teamInfoLoader.isTopic(id)) {
                        if (teamInfoLoader.isPublicTopic(id)) {
                            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
                        } else {
                            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
                        }
                    } else {
                        entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
                    }

                    EntitySpannable entitySpannable = new EntitySpannable(context,
                            teamId, id, entityType, teamInfoLoader.isStarred(id) || teamInfoLoader.isStarredUser(id));
                    entitySpannable.setColor(context.getResources().getColor(R.color.jandi_accent_color));

                    int length = ssb.length();
                    String name = teamInfoLoader.getName(id);
                    ssb.append(name);

                    ssb.setSpan(entitySpannable,
                            length, length + name.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }, Throwable::printStackTrace);

        // Check if empty
        if (firstLength == ssb.length()) {
            tvSharedTopics.setText(resources.getString(R.string.jandi_nowhere_shared_file));
        } else {
            tvSharedTopics.setText(ssb);
            LinkifyUtil.setOnLinkClick(tvSharedTopics);
        }

    }

    public Context getContext() {
        return context;
    }

    public ViewGroup getFileContentLayout() {
        return vgFileContent;
    }
}
