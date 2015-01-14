package com.tosslab.jandi.app.ui.maintab.file;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.UserEntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileTypeSimpleListAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
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

            final List<FormattedEntity> teamMember = entityManager.getFormattedUsers();
            final UserEntitySimpleListAdapter adapter = new UserEntitySimpleListAdapter(context, entityManager.getFormattedUsers());

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
                    } else {
                        FormattedEntity owner = teamMember.get(i - 1);
                        logger.debug(owner.getId() + " is selected");
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
            final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(context, entityManager.getCategorizableEntities());
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

}
