package com.tosslab.jandi.app.ui.maintab.file;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelector;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelectorImpl;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.ui.selector.user.UserSelector;
import com.tosslab.jandi.app.ui.selector.user.UserSelectorImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileListPresenter {

    @RootContext
    Context context;

    // 카테코리 탭
    @ViewById(R.id.txt_file_list_where)
    TextView textViewFileListWhere;

    @ViewById(R.id.txt_file_list_whom)
    TextView textViewFileListWhom;

    @ViewById(R.id.txt_file_list_type)
    TextView textViewFileListType;

    @ViewById(R.id.layout_file_list_empty)
    View uploadEmptyView;

    @ViewById(R.id.layout_file_list_search_empty)
    View searchEmptyView;

    @ViewById(R.id.layout_file_list_loading)
    View initLoadingView;

    @ViewById(R.id.progress_file_list)
    ProgressBar moreLoadingProgressBar;

    long entityIdForCategorizing = -1;
    String mCurrentEntityCategorizingAccodingBy = null;
    private String mCurrentUserNameCategorizingAccodingBy = null;
    private String mCurrentFileTypeCategorizingAccodingBy = null;

    public void setEntityIdForCategorizing(long entityIdForCategorizing) {
        this.entityIdForCategorizing = entityIdForCategorizing;
    }

    public void setCurrentEntityCategorizingAccodingBy(String mCurrentEntityCategorizingAccodingBy) {
        this.mCurrentEntityCategorizingAccodingBy = mCurrentEntityCategorizingAccodingBy;
    }

    @AfterViews
    void initViews() {
        setSpinnerAsCategorizingAccodingByFileType();
        setSpinnerAsCategorizingAccodingByWhere();
        setSpinnerAsCategorizingAccodingByWhom();

    }

    private void setSpinnerAsCategorizingAccodingByFileType() {
        textViewFileListType.setText(
                (mCurrentFileTypeCategorizingAccodingBy == null)
                        ? context.getString(R.string.jandi_file_category_all)
                        : mCurrentFileTypeCategorizingAccodingBy
        );
    }

    private void setSpinnerAsCategorizingAccodingByWhom() {
        textViewFileListWhom.setText(
                (mCurrentUserNameCategorizingAccodingBy == null)
                        ? context.getString(R.string.jandi_file_category_everyone)
                        : mCurrentUserNameCategorizingAccodingBy
        );
    }

    private void setSpinnerAsCategorizingAccodingByWhere() {
        textViewFileListWhere.setText(
                (mCurrentEntityCategorizingAccodingBy == null)
                        ? context.getString(R.string.jandi_file_category_everywhere)
                        : mCurrentEntityCategorizingAccodingBy
        );
    }

    public void showFileTypeDialog() {
        setUpTypeTextView(textViewFileListType, true);

        FileTypeSelector fileSelector = new FileTypeSelectorImpl();
        fileSelector.setOnFileTypeSelectListener(position -> {

            mCurrentFileTypeCategorizingAccodingBy =
                    context.getString(CategorizedMenuOfFileType.stringTitleResourceList[position]);
            textViewFileListType.setText(mCurrentFileTypeCategorizingAccodingBy);
            textViewFileListType.invalidate();
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
            }

            AnalyticsValue.Screen screen;
            if (context instanceof SearchActivity) {
                screen = AnalyticsValue.Screen.FilesSearch;
            } else {
                screen = AnalyticsValue.Screen.FilesTab;
            }
            AnalyticsValue.Action action = AnalyticsValue.Action.ChooseTypeFilter;
            AnalyticsUtil.sendEvent(screen, action, label);

        });

        fileSelector.setOnFileTypeDismissListener(() -> {
            setUpTypeTextView(textViewFileListType, false);
            if (context instanceof SearchActivity) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.CloseTypeFilter);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.CloseTypeFilter);
            }
        });
        fileSelector.show(((View) textViewFileListType.getParent().getParent()));

        if (context instanceof SearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenTypeFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenTypeFilter);
        }
    }

    public void showUsersDialog() {

        setUpTypeTextView(textViewFileListWhom, true);


        UserSelector userSelector = new UserSelectorImpl();
        userSelector.setOnUserSelectListener(item -> {

            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everyone);
                textViewFileListWhom.setText(mCurrentUserNameCategorizingAccodingBy);
                EventBus.getDefault().post(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
            } else if (item.getEntityId() == TeamInfoLoader.getInstance().getMyId()) {
                mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_my_files);
                textViewFileListWhom.setText(mCurrentUserNameCategorizingAccodingBy);
                EventBus.getDefault().post(new CategorizingAsOwner(item.getEntityId()));
            } else {
                mCurrentUserNameCategorizingAccodingBy = item.getName();
                textViewFileListWhom.setText(mCurrentUserNameCategorizingAccodingBy);
                EventBus.getDefault().post(new CategorizingAsOwner(item.getEntityId()));
            }
            userSelector.dismiss();

            AnalyticsValue.Screen screen;
            if (context instanceof SearchActivity) {
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
            setUpTypeTextView(textViewFileListWhom, false);
            if (context instanceof SearchActivity) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.CloseMemberFilter);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.CloseMemberFilter);
            }
        });

        userSelector.show(((View) textViewFileListWhom.getParent().getParent()));

        if (context instanceof SearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenMemberFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenMemberFilter);
        }
    }

    public void showEntityDialog() {
        setUpTypeTextView(textViewFileListWhere, true);

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<TopicRoom> allTopics = new ArrayList<>();
        Observable.from(teamInfoLoader.getTopicList())
                .subscribe(entity -> {
                            allTopics.add(entity);
                        }
                );
        List<Member> users = new ArrayList<>();
        Observable.from(teamInfoLoader.getUserList())
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .collect(() -> users, List::add)
                .subscribe();
        if (teamInfoLoader.hasJandiBot()) {
            users.add(0, TeamInfoLoader.getInstance().getJandiBot());
        }

        RoomSelector roomSelector = new RoomSelectorImpl(allTopics, users);
        roomSelector.setOnRoomSelectListener(item -> {
            long sharedEntityId = CategorizingAsEntity.EVERYWHERE;
            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                // 첫번째는 "Everywhere"인 더미 entity
                mCurrentEntityCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everywhere);
            } else {
                sharedEntityId = item.getEntityId();
                mCurrentEntityCategorizingAccodingBy = item.getName();
            }
            textViewFileListWhere.setText(mCurrentEntityCategorizingAccodingBy);
            textViewFileListWhere.invalidate();
            EventBus.getDefault().post(new CategorizingAsEntity(sharedEntityId));
            roomSelector.dismiss();

            AnalyticsValue.Screen screen;
            if (context instanceof SearchActivity) {
                screen = AnalyticsValue.Screen.FilesSearch;
            } else {
                screen = AnalyticsValue.Screen.FilesTab;
            }

            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.AllTopic);
            } else if (item.isUser()) {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.Member);
            } else {
                AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.Topic);
            }
        });

        roomSelector.setOnRoomDismissListener(() -> {
            setUpTypeTextView(textViewFileListWhere, false);
            if (context instanceof SearchActivity) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.CloseTopicFilter);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.CloseTopicFilter);
            }
        });

        roomSelector.show(((View) textViewFileListWhere.getParent().getParent()));
        if (context instanceof SearchActivity) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, AnalyticsValue.Action.OpenTopicFilter);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, AnalyticsValue.Action.OpenTopicFilter);
        }

    }

    private void setUpTypeTextView(TextView textVew, boolean isFocused) {
        Drawable rightDrawable;
        if (isFocused) {
            if (context instanceof SearchActivity) {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_up);
                ((View) textVew.getParent()).setBackgroundColor(context.getResources().getColor(R.color.jandi_primary_color_focus));
            } else {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_up_gray);
                ((View) textVew.getParent()).setBackgroundColor(Color.WHITE);
            }
        } else {
            if (context instanceof SearchActivity) {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_down);
            } else {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.file_arrow_down_gray);
            }
            ((View) textVew.getParent()).setBackgroundColor(Color.TRANSPARENT);
        }

        textVew.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    @UiThread
    public void setInitLoadingViewVisible(int visible) {
        initLoadingView.setVisibility(visible);
    }

    @UiThread
    public void setEmptyViewVisible(int visible) {
        uploadEmptyView.setVisibility(visible);
    }

    @UiThread
    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    @UiThread
    public void showErrorToast(String failMessage) {
        ColoredToast.show(failMessage);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showMoreProgressBar() {
        moreLoadingProgressBar.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissProgressBar() {

        moreLoadingProgressBar.getAnimation().reset();

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                moreLoadingProgressBar.setVisibility(View.GONE);
            }
        });

        animation.startNow();

    }

    @UiThread(delay = 10000)
    public void dismissProgressBarDelay() {
        dismissProgressBar();
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_GALLERY);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(context, FileExplorerActivity.class);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    @UiThread
    public void dismissProgressDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void exceedMaxFileSizeError() {
        ColoredToast.show(context.getString(R.string.jandi_file_size_large_error));

    }

    @UiThread
    public void setSearchEmptryViewVisible(int visible) {
        searchEmptyView.setVisibility(visible);
    }

    public ProgressDialog getUploadProgress(ConfirmFileUploadEvent event) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_file_uploading) + " " + event.realFilePath);
        progressDialog.show();

        return progressDialog;
    }

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissMoreProgressBar() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                moreLoadingProgressBar.setVisibility(View.GONE);
            }
        });

        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }
}
