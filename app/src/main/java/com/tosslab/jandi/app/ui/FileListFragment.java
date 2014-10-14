package com.tosslab.jandi.app.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.CategorizingAsEntity;
import com.tosslab.jandi.app.events.CategorizingAsOwner;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.UserEntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileTypeSimpleListAdapter;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EFragment(R.layout.fragment_file_list)
public class FileListFragment extends Fragment {
    private final Logger log = Logger.getLogger(FileListFragment.class);

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

    @ViewById(R.id.list_searched_files)
    PullToRefreshListView pullToRefreshListViewSearchedFiles;
    ListView actualListView;
    @Bean
    SearchedFileItemListAdapter mAdapter;
    @RestService
    JandiRestClient jandiRestClient;
    @FragmentArg
    int entityIdForCategorizing = -1;

    private MenuItem mSearch;   // ActionBar의 검색뷰
    private SearchQuery mSearchQuery;
    private ProgressWheel mProgressWheel;
    private String mMyToken;
    private Context mContext;
    private EntityManager mEntityManager;

    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    @AfterInject
    void init() {
        mContext = getActivity();
        mSearchQuery = new SearchQuery();
        if (entityIdForCategorizing >= 0) {
            mSearchQuery.setSharedEntity(entityIdForCategorizing);
        }
    }

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        retrieveEntityManager();
        setSpinnerAsCategorizingAccodingByFileType();
        setSpinnerAsCategorizingAccodingByWhere();
        setSpinnerAsCategorizingAccodingByWhom();

        pullToRefreshListViewSearchedFiles.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
                new GetPreviousFilesTask().execute();
            }
        });
        actualListView = pullToRefreshListViewSearchedFiles.getRefreshableView();

        // Empty View를 가진 ListView 설정
        View emptyView
                = getActivity().getLayoutInflater().inflate(R.layout.view_search_list_empty, null);
        actualListView.setEmptyView(emptyView);
        actualListView.setAdapter(mAdapter);

        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                list_searched_messagesItemClicked(mAdapter.getItem(i - 1));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchQuery.setToFirst();
        // 서치 시작
        doSearch();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_list_actionbar_menu, menu);

        mSearch = menu.findItem(R.id.action_file_list_search);
        final SearchView sv = (SearchView) mSearch.getActionView();

        mSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                imm.hideSoftInputFromWindow(sv.getWindowToken(),0);
                doKeywordSearch("");
                return true;
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                imm.hideSoftInputFromWindow(sv.getWindowToken(),0);
                doKeywordSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        setSearchViewStyle(sv);
    }

    private void setSearchViewStyle(SearchView searchView) {
        // Style 에서 설정이 안되서 코드에서 수정토록...
        // 글씨 색
        int searchSrcTextId
                = getResources().getIdentifier("android:id/search_src_text", null, null);
        AutoCompleteTextView searchEditText
                = (AutoCompleteTextView) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);

        // 닫기 버튼
        int closeButtonId
                = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButtonImage = (ImageView) searchView.findViewById(closeButtonId);
        closeButtonImage.setImageResource(R.drawable.jandi_actionb_remove);

        // 검색 Editbox 라인
        int searchEditId
                = getResources().getIdentifier("android:id/search_plate", null, null);
        View searchEditView = searchView.findViewById(searchEditId);
        searchEditView.setBackgroundResource(R.drawable.jandi_textfield_activated_holo_dark);
    }

    private void retrieveEntityManager() {
        EntityManager entityManager = ((JandiApplication)getActivity().getApplication()).getEntityManager();
        if (entityManager != null) {
            mEntityManager = entityManager;
        }
    }



    /************************************************************
     * 검색
     ************************************************************/
    void doKeywordSearch(String s) {
        mSearchQuery.setKeyword(s);
        doSearch();
    }

    @UiThread
    public void onInnerEvent(CategorizedMenuOfFileType event) {
        mSearchQuery.setFileType(event.getServerQuery());
        doSearch();
    }

    @UiThread
    public void onInnerEvent(CategorizingAsOwner event) {
        mSearchQuery.setWriter(event.userId);
        doSearch();
    }

    @UiThread
    public void onInnerEvent(CategorizingAsEntity event) {
        mSearchQuery.setSharedEntity(event.sharedEntityId);
        doSearch();
    }

    @UiThread
    void doSearch() {
        mAdapter.clearAdapter();
        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {
        jandiRestClient.setHeader(JandiConstants.AUTH_HEADER, mMyToken);

        try {
            ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
            ResSearchFile resSearchFile = jandiRestClient.searchFile(reqSearchFile);
            searchSucceed(resSearchFile);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            searchFailed(getString(R.string.err_file_search));
        } catch (HttpMessageNotReadableException e) {
            log.error("fail to get searched files.", e);
            searchFailed(getString(R.string.err_file_search));
        }
    }

    @UiThread
    void searchSucceed(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount > 0) {
            mAdapter.insert(resSearchFile);
            mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
        }

        if (resSearchFile.fileCount < ReqSearchFile.MAX) {
            pullToRefreshListViewSearchedFiles.setMode(PullToRefreshBase.Mode.DISABLED);
        } else {
            pullToRefreshListViewSearchedFiles.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        }

        log.debug("success to find " + resSearchFile.fileCount + " files.");
        mAdapter.notifyDataSetChanged();
    }

    @UiThread
    void searchFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }

    /**
     * Full To Refresh 전용
     * TODO 위에 거랑 합치기.
     */
    private class GetPreviousFilesTask extends AsyncTask<Void, Void, String> {
        private int justGetFilesSize;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
                ResSearchFile resSearchFile = jandiRestClient.searchFile(reqSearchFile);

                justGetFilesSize = resSearchFile.fileCount;
                if (justGetFilesSize > 0) {
                    mAdapter.insert(resSearchFile);
                    mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                }
                return null;
            } catch (RestClientException e) {
                log.error("fail to get searched files.", e);
                return getString(R.string.err_file_search);
            } catch (HttpMessageNotReadableException e) {
                log.error("fail to get searched files.", e);
                return getString(R.string.err_file_search);
            }
        }

        @Override
        protected void onPostExecute(String errMessage) {
            pullToRefreshListViewSearchedFiles.onRefreshComplete();
            if (justGetFilesSize < ReqSearchFile.MAX) {
                ColoredToast.showWarning(mContext, getString(R.string.warn_no_more_files));
                pullToRefreshListViewSearchedFiles.setMode(PullToRefreshBase.Mode.DISABLED);
            }

            if (errMessage == null) {
                // Success
                mAdapter.notifyDataSetChanged();
            } else {
                ColoredToast.showError(mContext, errMessage);
            }
            super.onPostExecute(errMessage);
        }
    }

    /************************************************************
     * File tab 을 위한 액션바와 카테고리 선택 다이얼로그, 이벤트 전달
     ************************************************************/
    private String mCurrentUserNameCategorizingAccodingBy = null;
    private String mCurrentFileTypeCategorizingAccodingBy = null;
    @FragmentArg
    String mCurrentEntityCategorizingAccodingBy = null;

    private AlertDialog mFileTypeSelectDialog;
    private AlertDialog mUserSelectDialog;  // 사용자별 검색시 사용할 리스트 다이얼로그
    private AlertDialog mEntitySelectDialog;

    private void setSpinnerAsCategorizingAccodingByFileType() {
        textViewFileListType.setText(
                (mCurrentFileTypeCategorizingAccodingBy == null)
                        ? getString(R.string.jandi_file_category_all)
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
                        ? getString(R.string.jandi_file_category_everyone)
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
                        ? getString(R.string.jandi_file_category_everywhere)
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
     * @param textVewFileType
     */
    private void showFileTypeDialog(final TextView textVewFileType) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final FileTypeSimpleListAdapter adapter = new FileTypeSimpleListAdapter(mContext);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mFileTypeSelectDialog != null)
                    mFileTypeSelectDialog.dismiss();
                mCurrentFileTypeCategorizingAccodingBy = adapter.getItem(i);
                textVewFileType.setText(mCurrentFileTypeCategorizingAccodingBy);
                onInnerEvent(new CategorizedMenuOfFileType(i));
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.jandi_file_search_type);
        dialog.setView(view);
        mFileTypeSelectDialog = dialog.show();
        mFileTypeSelectDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 사용자 리스트 Dialog 를 보여준 뒤, 선택된 사용자가 올린 파일을 검색하라는 이벤트를
     * FileListFragment에 전달
     * @param textViewUser
     */
    private void showUsersDialog(final TextView textViewUser) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // TODO : List를 User가 아닌 FormattedUser로 바꾸면 addHeader가 아니라 List에서
        // TODO : Everyone 용으로 0번째 item을 추가할 수 있음. 그럼 아래 note 로 적힌 인덱스가 밀리는 현상 해결됨.
        // TODO : 뭐가 더 나은지는 모르겠네잉

        final List<FormattedEntity> teamMember = mEntityManager.getFormattedUsers();
        final UserEntitySimpleListAdapter adapter = new UserEntitySimpleListAdapter(mContext, mEntityManager.getFormattedUsers());

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mUserSelectDialog != null)
                    mUserSelectDialog.dismiss();
                // NOTE : index 0 이 Everyone 으로 올라가면서
                // teamMember[0]은 Adapter[1]과 같다. Adapter[0]은 모든 유저.
                if (i == 0) {
                    mCurrentUserNameCategorizingAccodingBy = getString(R.string.jandi_file_category_everyone);
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    onInnerEvent(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
                } else {
                    FormattedEntity owner = teamMember.get(i - 1);
                    log.debug(owner.getId() + " is selected");
                    mCurrentUserNameCategorizingAccodingBy = owner.getName();
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    onInnerEvent(new CategorizingAsOwner(owner.getId()));
                }
            }
        });
        lv.addHeaderView(getHeaderViewAsAllUser());
        lv.setAdapter(adapter);

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.jandi_file_search_user);
        dialog.setView(view);
        mUserSelectDialog = dialog.show();
        mUserSelectDialog.setCanceledOnTouchOutside(true);
    }

    private View getHeaderViewAsAllUser() {
        View headerView = getActivity().getLayoutInflater().inflate(R.layout.item_select_cdp, null, false);
        TextView textView = (TextView) headerView.findViewById(R.id.txt_select_cdp_name);
        textView.setText(R.string.jandi_file_category_everyone);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.img_select_cdp_icon);
        imageView.setImageResource(R.drawable.jandi_profile);
        return headerView;
    }

    /**
     * 모든 Entity 리스트 Dialog 를 보여준 뒤, 선택된 장소에 share 된 파일만 검색하라는 이벤트를
     * FileListFragment에 전달
     * @param textVew
     */
    private void showEntityDialog(final TextView textVew) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(mContext, mEntityManager.getCategorizableEntities());
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mEntitySelectDialog != null)
                    mEntitySelectDialog.dismiss();

                int sharedEntityId = CategorizingAsEntity.EVERYWHERE;

                if (i <= 0) {
                    // 첫번째는 "Everywhere"인 더미 entity
                    mCurrentEntityCategorizingAccodingBy = getString(R.string.jandi_file_category_everywhere);
                } else {
                    FormattedEntity sharedEntity = adapter.getItem(i);
                    sharedEntityId = sharedEntity.getId();
                    mCurrentEntityCategorizingAccodingBy = sharedEntity.getName();
                }
                textVew.setText(mCurrentEntityCategorizingAccodingBy);
                onInnerEvent(new CategorizingAsEntity(sharedEntityId));
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.jandi_file_search_entity);
        dialog.setView(view);
        mEntitySelectDialog = dialog.show();
        mEntitySelectDialog.setCanceledOnTouchOutside(true);
    }

    /************************************************************
     * Etc
     ************************************************************/
    void list_searched_messagesItemClicked(ResMessages.FileMessage searchedFile) {
        moveToFileDetailActivity(searchedFile.id);
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    /************************************************************
     * 파일 검색을 담당하는 쿼리 클래스
     ************************************************************/
    private class SearchQuery {
        private final String CATEGORY_ALL   = "all";
        private final int LATEST_MESSAGE    = -1;

        private String mSearchFileType;
        private String mSearchUser;
        private String mKeyword;
        private int mSearchEntity;
        private int mStartMessageId;

        public SearchQuery() {
            mSearchEntity = ReqSearchFile.ALL_ENTITIES;
            mStartMessageId = LATEST_MESSAGE;
            mKeyword = "";
            mSearchFileType = CATEGORY_ALL;    // 서치 모드.   ALL || Images || PDFs
            mSearchUser = CATEGORY_ALL;        // 사용자.     ALL || Mine || UserID
        }

        public void setToFirst() {
            mStartMessageId = LATEST_MESSAGE;
        }

        public void setKeyword(String keyword) {
            setToFirst();
            mKeyword = keyword;
        }

        public void setFileType(String fileType) {
            setToFirst();
            mSearchFileType = fileType;
        }

        public void setWriter(String userEntityId) {
            setToFirst();
            mSearchUser = userEntityId;
        }

        public void setSharedEntity(int entityId) {
            setToFirst();
            mSearchEntity = entityId;
        }

        public void setNext(int startMessageId) {
            mStartMessageId = startMessageId;
        }

        public ReqSearchFile getRequestQuery() {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
            reqSearchFile.listCount = ReqSearchFile.MAX;

            reqSearchFile.fileType = mSearchFileType;
            reqSearchFile.writerId = mSearchUser;
            reqSearchFile.sharedEntityId = mSearchEntity;

            reqSearchFile.startMessageId = mStartMessageId;
            reqSearchFile.keyword = mKeyword;
            return reqSearchFile;
        }
    }
}
