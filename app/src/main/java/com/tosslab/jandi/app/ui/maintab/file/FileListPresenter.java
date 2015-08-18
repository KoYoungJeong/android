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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelector;
import com.tosslab.jandi.app.ui.selector.filetype.FileTypeSelectorImpl;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.ui.selector.user.UserSelector;
import com.tosslab.jandi.app.ui.selector.user.UserSelectorImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileListPresenter {

    @RootContext
    Context context;

    // 카테코리 탭
    @ViewById(R.id.ly_file_list_where)
    LinearLayout linearLayoutFileListWhere;
    @ViewById(R.id.txt_file_list_where)
    TextView textViewFileListWhere;
    @ViewById(R.id.ly_file_list_whom)
    LinearLayout linearLayoutFileListWhom;
    @ViewById(R.id.txt_file_list_whom)
    TextView textViewFileListWhom;
    @ViewById(R.id.ly_file_list_type)
    LinearLayout linearLayoutFileListType;
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

    int entityIdForCategorizing = -1;
    String mCurrentEntityCategorizingAccodingBy = null;
    private String mCurrentUserNameCategorizingAccodingBy = null;
    private String mCurrentFileTypeCategorizingAccodingBy = null;
    private AlertDialog mFileTypeSelectDialog;
    private AlertDialog mUserSelectDialog;  // 사용자별 검색시 사용할 리스트 다이얼로그
    private AlertDialog mEntitySelectDialog;
    private EntityManager entityManager;

    public void setEntityIdForCategorizing(int entityIdForCategorizing) {
        this.entityIdForCategorizing = entityIdForCategorizing;
    }

    public void setCurrentEntityCategorizingAccodingBy(String mCurrentEntityCategorizingAccodingBy) {
        this.mCurrentEntityCategorizingAccodingBy = mCurrentEntityCategorizingAccodingBy;
    }

    @AfterInject
    void initObject() {
        entityManager = EntityManager.getInstance(context);
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
        linearLayoutFileListType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileTypeDialog(textViewFileListType);
            }
        });
    }

    private void setSpinnerAsCategorizingAccodingByWhom() {
        textViewFileListWhom.setText(
                (mCurrentUserNameCategorizingAccodingBy == null)
                        ? context.getString(R.string.jandi_file_category_everyone)
                        : mCurrentUserNameCategorizingAccodingBy
        );
        linearLayoutFileListWhom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUsersDialog(textViewFileListWhom);
            }
        });
    }

    private void setSpinnerAsCategorizingAccodingByWhere() {
        textViewFileListWhere.setText(
                (mCurrentEntityCategorizingAccodingBy == null)
                        ? context.getString(R.string.jandi_file_category_everywhere)
                        : mCurrentEntityCategorizingAccodingBy
        );
        linearLayoutFileListWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEntityDialog(textViewFileListWhere);
            }
        });
    }

    private void showFileTypeDialog(final TextView textVewFileType) {
        setUpTypeTextView(textVewFileType, true);

        FileTypeSelector fileSelector = new FileTypeSelectorImpl();
        fileSelector.setOnFileTypeSelectListener(position -> {

            mCurrentFileTypeCategorizingAccodingBy =
                    context.getString(CategorizedMenuOfFileType.stringTitleResourceList[position]);
            textVewFileType.setText(mCurrentFileTypeCategorizingAccodingBy);
            textVewFileType.invalidate();
            EventBus.getDefault().post(new CategorizedMenuOfFileType(position));

            fileSelector.dismiss();
        });

        fileSelector.setOnFileTypeDismissListener(() -> setUpTypeTextView(textVewFileType, false));
        fileSelector.show(((View) textVewFileType.getParent().getParent()));
    }

    private void showUsersDialog(final TextView textViewUser) {

        setUpTypeTextView(textViewUser, true);


        UserSelector userSelector = new UserSelectorImpl();
        userSelector.setOnUserSelectListener(new UserSelector.OnUserSelectListener() {
            @Override
            public void onUserSelect(FormattedEntity item) {


                if (item.type == FormattedEntity.TYPE_EVERYWHERE) {
                    mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everyone);
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
                } else if (item.getId() ==
                        EntityManager.getInstance(JandiApplication.getContext()).getMe().getId()) {
                    mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_my_files);
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizingAsOwner(item.getId()));
                } else {
                    mCurrentUserNameCategorizingAccodingBy = item.getName();
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizingAsOwner(item.getId()));
                }
                userSelector.dismiss();

            }
        });

        userSelector.setOnUserDismissListener(() -> setUpTypeTextView(textViewUser, false));

        userSelector.show(((View) textViewUser.getParent().getParent()));
    }

    private void showEntityDialog(final TextView textVew) {

        setUpTypeTextView(textVew, true);

        RoomSelector roomSelector = new RoomSelectorImpl();
        roomSelector.setOnRoomSelectListener(new RoomSelector.OnRoomSelectListener() {
            @Override
            public void onRoomSelect(FormattedEntity item) {

                int sharedEntityId = CategorizingAsEntity.EVERYWHERE;

                if (item.type == FormattedEntity.TYPE_EVERYWHERE) {
                    // 첫번째는 "Everywhere"인 더미 entity
                    mCurrentEntityCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everywhere);
                } else {
                    sharedEntityId = item.getId();
                    mCurrentEntityCategorizingAccodingBy = item.getName();
                }
                textVew.setText(mCurrentEntityCategorizingAccodingBy);
                textVew.invalidate();
                EventBus.getDefault().post(new CategorizingAsEntity(sharedEntityId));
                roomSelector.dismiss();

            }
        });

        roomSelector.setOnRoomDismissListener(() -> setUpTypeTextView(textVew, false));
        roomSelector.show(((View) textVew.getParent().getParent()));
    }

    private void setUpTypeTextView(TextView textVew, boolean isFocused) {

        Drawable rightDrawable;
        if (isFocused) {
            if (context instanceof SearchActivity) {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.jandi_arrow_up);
                ((View) textVew.getParent()).setBackgroundColor(context.getResources().getColor(R.color.jandi_primary_color_focus));
            } else {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.jandi_arrow_up_gray);
                ((View) textVew.getParent()).setBackgroundColor(Color.WHITE);
            }
        } else {
            if (context instanceof SearchActivity) {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.jandi_arrow_down);
            } else {
                rightDrawable = textVew.getResources().getDrawable(R.drawable.jandi_arrow_down_gray);
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
        ColoredToast.showWarning(context, message);
    }

    @UiThread
    public void showErrorToast(String failMessage) {
        ColoredToast.showError(context, failMessage);
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

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
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
        fragment.startActivityForResult(intent, FilePickerViewModel.TYPE_UPLOAD_GALLERY);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(context, FileExplorerActivity.class);
        fragment.startActivityForResult(intent, FilePickerViewModel.TYPE_UPLOAD_EXPLORER);
    }

    @UiThread
    public void dismissProgressDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void exceedMaxFileSizeError() {
        ColoredToast.showError(context, context.getString(R.string.jandi_file_size_large_error));

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
        ColoredToast.show(context, message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissMoreProgressBar() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
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
