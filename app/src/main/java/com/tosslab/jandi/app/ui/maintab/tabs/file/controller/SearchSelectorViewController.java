package com.tosslab.jandi.app.ui.maintab.tabs.file.controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.search.file.view.FileSearchActivity;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelector;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelectorImpl;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.ui.selector.user.UserSelector;
import com.tosslab.jandi.app.ui.selector.user.UserSelectorImpl;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by tee on 16. 6. 29..
 */
public class SearchSelectorViewController {

    private TextView tvFileListWhere;
    private TextView tvFileListWhom;
    private TextView tvFileListType;

    private Context context;

    private String currentFileTypeText = null;
    private String currentUserNameText = null;
    private String currentEntityNameText = null;

    public SearchSelectorViewController(Context context,
                                        TextView tvFileListWhere,
                                        TextView tvFileListWhom,
                                        TextView tvFileListType) {
        this.context = context;
        this.tvFileListWhere = tvFileListWhere;
        this.tvFileListWhom = tvFileListWhom;
        this.tvFileListType = tvFileListType;
        setSpinnerByFileType();
        setSpinnerByWhom();
        setSpinnerByWhere();
    }

    public void setCurrentEntityNameText(String currentEntityNameText) {
        this.currentEntityNameText = currentEntityNameText;
        setSpinnerByWhere();
    }

    private void setSpinnerByFileType() {
        tvFileListType.setText(
                (currentFileTypeText == null || currentEntityNameText.equals(""))
                        ? JandiApplication.getContext().getString(R.string.jandi_file_category_all)
                        : currentFileTypeText
        );
    }

    private void setSpinnerByWhom() {
        tvFileListWhom.setText(
                (currentUserNameText == null || currentUserNameText.equals(""))
                        ? JandiApplication.getContext().getString(R.string.jandi_search_category_everyone)
                        : currentUserNameText
        );
    }

    private void setSpinnerByWhere() {
        tvFileListWhere.setText(
                (currentEntityNameText == null || currentEntityNameText.equals(""))
                        ? JandiApplication.getContext().getString(R.string.jandi_joined_room)
                        : currentEntityNameText
        );
    }

    public void showFileTypeDialog() {
        setUpTypeTextView(tvFileListType, true);

        FileTypeSelector fileSelector = new FileTypeSelectorImpl();
        fileSelector.setOnFileTypeSelectListener(position -> {

            currentFileTypeText =
                    context.getString(CategorizedMenuOfFileType.stringTitleResourceList[position]);
            tvFileListType.setText(currentFileTypeText);
            tvFileListType.invalidate();
            EventBus.getDefault().post(new CategorizedMenuOfFileType(position));

            fileSelector.dismiss();

            AnalyticsValue.Label label;
            switch (position) {
                default:
                case 0:
                    label = AnalyticsValue.Label.AllType;
                    break;
                case 1:
                    label = AnalyticsValue.Label.GoogleDocs;
                    break;
                case 2:
                    label = AnalyticsValue.Label.Words;
                    break;
                case 3:
                    label = AnalyticsValue.Label.Presentations;
                    break;
                case 4:
                    label = AnalyticsValue.Label.Spreadsheets;
                    break;
                case 5:
                    label = AnalyticsValue.Label.PDFs;
                    break;
                case 6:
                    label = AnalyticsValue.Label.Images;
                    break;
                case 7:
                    label = AnalyticsValue.Label.Videos;
                    break;
                case 8:
                    label = AnalyticsValue.Label.Audios;
                    break;
                case 9:
                    label = AnalyticsValue.Label.Zipfiles;
            }

            AnalyticsValue.Screen screen;
            if (context instanceof FileSearchActivity) {
                screen = AnalyticsValue.Screen.FilesSearch;
            } else {
                screen = AnalyticsValue.Screen.FilesTab;
            }
            AnalyticsValue.Action action = AnalyticsValue.Action.ChooseTypeFilter;
            AnalyticsUtil.sendEvent(screen, action, label);

        });

        fileSelector.setOnFileTypeDismissListener(() -> {
            setUpTypeTextView(tvFileListType, false);
        });

        fileSelector.show(((View) tvFileListType.getParent().getParent()));

        if (context instanceof FileSearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenTypeFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenTypeFilter);
        }
    }

    public void showUsersDialog() {
        setUpTypeTextView(tvFileListWhom, true);
        UserSelector userSelector = new UserSelectorImpl();
        userSelector.setOnUserSelectListener(item -> {

            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                currentUserNameText = context.getString(R.string.jandi_search_category_everyone);
                tvFileListWhom.setText(currentUserNameText);
                EventBus.getDefault().post(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
            } else if (item.getEntityId() == TeamInfoLoader.getInstance().getMyId()) {
                currentUserNameText = context.getString(R.string.jandi_my_files);
                tvFileListWhom.setText(currentUserNameText);
                EventBus.getDefault().post(new CategorizingAsOwner(item.getEntityId()));
            } else {
                currentUserNameText = item.getName();
                tvFileListWhom.setText(currentUserNameText);
                EventBus.getDefault().post(new CategorizingAsOwner(item.getEntityId()));
            }
            userSelector.dismiss();

            AnalyticsValue.Screen screen;
            if (context instanceof FileSearchActivity) {
                screen = AnalyticsValue.Screen.FilesSearch;
            } else {
                screen = AnalyticsValue.Screen.FilesTab;
            }

            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseMemberFilter, AnalyticsValue.Label.AllMember);
            } else {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseMemberFilter, AnalyticsValue.Label.Member);
            }
        });

        userSelector.setOnUserDismissListener(() -> {
            setUpTypeTextView(tvFileListWhom, false);
        });

        userSelector.show(((View) tvFileListWhom.getParent().getParent()));

        if (context instanceof FileSearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenMemberFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenMemberFilter);
        }
    }

    public void showEntityDialog() {
        setUpTypeTextView(tvFileListWhere, true);

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<TopicRoom> allTopics = new ArrayList<>();
        Observable.from(teamInfoLoader.getTopicList())
                .filter(TopicRoom::isJoined)
                .subscribe(allTopics::add);
        List<Member> users = new ArrayList<>();
        List<User> userList;
        if (TeamInfoLoader.getInstance().getMyLevel() != Level.Guest) {
            userList = teamInfoLoader.getUserList();
        } else {
            userList = Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .filter(TopicRoom::isJoined)
                    .concatMap(topicRoom -> Observable.from(topicRoom.getMembers()))
                    .distinct()
                    .filter(it -> TeamInfoLoader.getInstance().isUser(it))
                    .filter(it -> !TeamInfoLoader.getInstance().isJandiBot(it))
                    .filter(it -> TeamInfoLoader.getInstance().getMyId() != it)
                    .map(it -> TeamInfoLoader.getInstance().getUser(it))
                    .collect((Func0<ArrayList<User>>) ArrayList::new, List::add)
                    .toBlocking().firstOrDefault(new ArrayList<>());
        }
        Observable.from(userList)
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(user -> TeamInfoLoader.getInstance().getMyId() != user.getId())
                .collect(() -> users, List::add)
                .subscribe();
        if (teamInfoLoader.hasJandiBot()) {
            users.add(0, TeamInfoLoader.getInstance().getJandiBot());
        }

        RoomSelector roomSelector = new RoomSelectorImpl(allTopics, users);
        roomSelector.setOnRoomSelectListener(item -> {
            long sharedEntityId;
            AnalyticsValue.Label label;
            if (item.getType() == JandiConstants.Entity.TYPE_JOINED_ROOM) {
                sharedEntityId = CategorizingAsEntity.JOINED;
                currentEntityNameText = context.getString(R.string.jandi_joined_room);
                label = AnalyticsValue.Label.JoinedRoom;
            } else if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                sharedEntityId = CategorizingAsEntity.ACCESSIBLE;
                // 첫번째는 "Everywhere"인 더미 entity
                currentEntityNameText = context.getString(R.string.jandi_search_category_everywhere);
                label = AnalyticsValue.Label.AllRoom;
            } else {
                sharedEntityId = item.getEntityId();
                currentEntityNameText = item.getName();
                if (item.isUser()) {
                    label = AnalyticsValue.Label.Member;
                } else {
                    label = AnalyticsValue.Label.Topic;
                }
            }
            tvFileListWhere.setText(currentEntityNameText);
            tvFileListWhere.invalidate();
            EventBus.getDefault().post(new CategorizingAsEntity(sharedEntityId));
            roomSelector.dismiss();

            AnalyticsValue.Screen screen;
            if (context instanceof FileSearchActivity) {
                screen = AnalyticsValue.Screen.FilesSearch;
            } else {
                screen = AnalyticsValue.Screen.FilesTab;
            }

            AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseTopicFilter, label);

        });

        roomSelector.setOnRoomDismissListener(() -> {
            setUpTypeTextView(tvFileListWhere, false);
        });

        roomSelector.show(((View) tvFileListWhere.getParent().getParent()));
        if (context instanceof FileSearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenTopicFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenTopicFilter);
        }

    }

    private void setUpTypeTextView(TextView textVew, boolean isFocused) {
        Drawable rightDrawable;
        if (isFocused) {
            rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_up);
            ((View) textVew.getParent()).setSelected(true);
        } else {
            rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_down);
            ((View) textVew.getParent()).setSelected(false);
        }
        textVew.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    public void setCurrentEntity(long entityId) {
        String name = TeamInfoLoader.getInstance().getName(entityId);

        if (entityId < 0) {
            tvFileListWhere.setText(R.string.jandi_search_category_everywhere);
        } else if (!TextUtils.isEmpty(name)) {
            tvFileListWhere.setText(name);
        }
    }

    public void setCurrentMember(long writerId) {
        String name;

        if (writerId < 0) {
            tvFileListWhom.setText(R.string.jandi_search_category_everyone);
        } else if (TeamInfoLoader.getInstance().getMyId() == writerId) {
            tvFileListWhom.setText(R.string.jandi_my_files);
        } else {
            name = TeamInfoLoader.getInstance().getName(writerId);
            tvFileListWhom.setText(name);
        }
    }

    public void setCurrentFileType(String fileType) {
        int titleResId = CategorizedMenuOfFileType.findTitleResIdFromQuery(fileType);
        if (titleResId > 0) {
            tvFileListType.setText(titleResId);
        } else {
            tvFileListType.setText(R.string.jandi_file_category_all);
        }
    }
}
