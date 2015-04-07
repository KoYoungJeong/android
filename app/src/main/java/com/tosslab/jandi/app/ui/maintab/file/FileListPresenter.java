package com.tosslab.jandi.app.ui.maintab.file;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.UserEntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileTypeSimpleListAdapter;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileListPresenter {

    private static final Logger logger = Logger.getLogger(FileListPresenter.class);

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

    /**
     * 파일 타입 리스트 Dialog 를 보여준 뒤, 선택된 타입만 검색하라는 이벤트를
     * FileListFragment에 전달
     *
     * @param textVewFileType
     */
    private void showFileTypeDialog(final TextView textVewFileType) {

        if (mFileTypeSelectDialog != null && mFileTypeSelectDialog.isShowing()) {
            return;
        }

        if (mFileTypeSelectDialog == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_cdp, null);
            ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
            final FileTypeSimpleListAdapter adapter = new FileTypeSimpleListAdapter(context);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mFileTypeSelectDialog != null)
                        mFileTypeSelectDialog.dismiss();
                    mCurrentFileTypeCategorizingAccodingBy = adapter.getItem(i);
                    textVewFileType.setText(mCurrentFileTypeCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizedMenuOfFileType(i));
                }
            });

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.jandi_file_search_type);
            dialog.setView(view);
            mFileTypeSelectDialog = dialog.show();
            mFileTypeSelectDialog.setCanceledOnTouchOutside(true);
        } else {
            mFileTypeSelectDialog.show();
        }
    }

    /**
     * 사용자 리스트 Dialog 를 보여준 뒤, 선택된 사용자가 올린 파일을 검색하라는 이벤트를
     * FileListFragment에 전달
     *
     * @param textViewUser
     */
    private void showUsersDialog(final TextView textViewUser) {

        if (mUserSelectDialog != null && mUserSelectDialog.isShowing()) {
            return;
        }

        if (mUserSelectDialog == null) {


            View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_cdp, null);
            ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

            // TODO : List를 User가 아닌 FormattedUser로 바꾸면 addHeader가 아니라 List에서
            // TODO : Everyone 용으로 0번째 item을 추가할 수 있음. 그럼 아래 note 로 적힌 인덱스가 밀리는 현상 해결됨.
            // TODO : 뭐가 더 나은지는 모르겠네잉

            FormattedEntity me = entityManager.getMe();

            List<FormattedEntity> teamMember = entityManager.getFormattedUsers();
            for (int idx = teamMember.size() - 1; idx >= 0; idx--) {
                FormattedEntity formattedEntity = teamMember.get(idx);
                if (formattedEntity.getId() == me.getId()) {
                    teamMember.remove(idx);
                    teamMember.add(0, formattedEntity);
                } else if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                    teamMember.remove(idx);
                }
            }
            final UserEntitySimpleListAdapter adapter = new UserEntitySimpleListAdapter(context, teamMember);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mUserSelectDialog != null)
                        mUserSelectDialog.dismiss();

                    // NOTE : index 0 이 Everyone 으로 올라가면서
                    // teamMember[0]은 Adapter[1]과 같다. Adapter[0]은 모든 유저.
                    if (i == 0) {
                        mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everyone);
                        textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                        EventBus.getDefault().post(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
                    } else if (i == 1) {
                        FormattedEntity owner = teamMember.get(i - 1);
                        mCurrentUserNameCategorizingAccodingBy = context.getString(R.string.jandi_my_files);
                        textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                        EventBus.getDefault().post(new CategorizingAsOwner(owner.getId()));
                    } else {
                        FormattedEntity owner = teamMember.get(i - 1);
                        mCurrentUserNameCategorizingAccodingBy = owner.getName();
                        textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                        EventBus.getDefault().post(new CategorizingAsOwner(owner.getId()));
                    }
                }
            });
            lv.addHeaderView(getHeaderViewAsAllUser());
            lv.setAdapter(adapter);

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.jandi_file_search_user);
            dialog.setView(view);
            mUserSelectDialog = dialog.show();
            mUserSelectDialog.setCanceledOnTouchOutside(true);
        } else {
            mUserSelectDialog.show();
        }
    }

    /**
     * 모든 Entity 리스트 Dialog 를 보여준 뒤, 선택된 장소에 share 된 파일만 검색하라는 이벤트를
     * FileListFragment에 전달
     *
     * @param textVew
     */
    private void showEntityDialog(final TextView textVew) {

        if (mEntitySelectDialog != null && mEntitySelectDialog.isShowing()) {
            return;
        }

        if (mEntitySelectDialog == null) {

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_cdp, null);
            ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
            List<FormattedEntity> categorizableEntities = entityManager.getCategorizableEntities();
            FormattedEntity me = entityManager.getMe();

            for (int idx = categorizableEntities.size() - 1; idx >= 0; idx--) {
                FormattedEntity formattedEntity = categorizableEntities.get(idx);
                if (formattedEntity.isUser()) {
                    if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                        categorizableEntities.remove(idx);
                    } else if (formattedEntity.getId() == me.getId()) {
                        categorizableEntities.remove(idx);
                    }
                }
            }

            final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(context, categorizableEntities);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mEntitySelectDialog != null)
                        mEntitySelectDialog.dismiss();

                    int sharedEntityId = CategorizingAsEntity.EVERYWHERE;

                    if (i <= 0) {
                        // 첫번째는 "Everywhere"인 더미 entity
                        mCurrentEntityCategorizingAccodingBy = context.getString(R.string.jandi_file_category_everywhere);
                    } else {
                        FormattedEntity sharedEntity = adapter.getItem(i);
                        sharedEntityId = sharedEntity.getId();
                        mCurrentEntityCategorizingAccodingBy = sharedEntity.getName();
                    }
                    textVew.setText(mCurrentEntityCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizingAsEntity(sharedEntityId));
                }
            });

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.jandi_file_search_entity);
            dialog.setView(view);
            mEntitySelectDialog = dialog.show();
            mEntitySelectDialog.setCanceledOnTouchOutside(true);
        } else {
            mEntitySelectDialog.show();
        }
    }

    private View getHeaderViewAsAllUser() {
        View headerView = LayoutInflater.from(context).inflate(R.layout.item_select_cdp, null, false);
        TextView textView = (TextView) headerView.findViewById(R.id.txt_select_cdp_name);
        textView.setText(R.string.jandi_file_category_everyone);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.img_select_cdp_icon);
        imageView.setImageResource(R.drawable.jandi_profile);
        return headerView;
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
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
    }

    public void openCameraForActivityResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(context, FileExplorerActivity.class);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
    }

    @UiThread
    public void dismissProgressDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void exceedMaxFileSizeError() {
        ColoredToast.showError(context, context.getString(R.string.err_file_upload_failed));

    }

    @UiThread
    public void setSearchEmptryViewVisible(int visible) {
        searchEmptyView.setVisibility(visible);
    }

}
