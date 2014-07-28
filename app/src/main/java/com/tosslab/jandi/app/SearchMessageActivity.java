package com.tosslab.jandi.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.CdpSelectListAdapter;
import com.tosslab.jandi.app.lists.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.ViewGroupUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 21..
 */
@EActivity(R.layout.activity_search_message)
public class SearchMessageActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(SearchMessageActivity.class);
    @Extra
    public int mSearchMode;         // 서치 모드.   ALL || Images || PDFs
    private String mSearchUser;     // 사용자.     ALL || Mine || UserID
    private String mCurrentUserName;    // 현재 UserName
    private String mCurrentUserId;      // 현재 UserId

    @ViewById(R.id.ly_search_message_all_files)
    LinearLayout layoutSearchMessageAllFiles;
    @ViewById(R.id.ly_search_message_user)
    LinearLayout layoutSearchMessageUser;
    @ViewById(R.id.txt_search_message_user_name)
    TextView textViewSearchMessageUserName;

    @ViewById(R.id.list_searched_messages)
    ListView listSearchedMessages;
    @Bean
    SearchedFileItemListAdapter mAdapter;

    @RestService
    TossRestClient tossRestClient;

    private boolean isSelectedAllFilesTab;  // 파일 종류 탭을 클릭한 경우.
    private boolean isSelectedUsersTab;     // 사용자 탭을 클릭한 경우.

    private ProgressWheel mProgressWheel;
    private String mMyToken;

    private CdpItemManager mCdpItemManager = null;
    private boolean isFirst = true;

    private AlertDialog mUserSelectDialog;  // 사용자별 검색시 사용할 리스트 다이얼로그

    @AfterViews
    void initView() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        View titleView = findViewById(titleId);

        // attach listener to this spinnerView for handling spinner selection change
        Spinner spinner = (Spinner) getLayoutInflater().inflate(R.layout.spinner_search_type, null);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isFirst) {
                    return;
                }
                // All tab으로 이동하여 다시 서치를 수행
                log.debug(i + " selected");
                mSearchMode = i;
                setFlagSelectedAllFilesTab(true);
                drawLayout();
                // 서치 시작
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ViewGroupUtils.replaceView(titleView, spinner);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(this);
        listSearchedMessages.setAdapter(mAdapter);

        // 선택한 서치 모드와 사용자를 셋팅
        // 내 파일 검색으로 시작한다면 두번째 텝으로 이동
        mCurrentUserId = ReqSearchFile.USER_ID_MINE;
        mCurrentUserName = "Just Me";
        if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_USER) {
            setFlagSelectedAllFilesTab(false);
        } else {
            spinner.setSelection(mSearchMode);
            setFlagSelectedAllFilesTab(true);
        }
        drawLayout();

        // 서치 시작
        doSearch();
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
//            case R.id.action_search:
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sticky Event from MainRightFragment
     * SearchListFragment에서 cdpItemManager를 FileDetailActivity로 넘겨주기 위해 사용
     * @param event
     */
    public void onEvent(CdpItemManager event) {
        mCdpItemManager = event;
    }

    @Click(R.id.img_search_message_select_user)
    public void showUsersDialog() {
        /**
         * 사용자 리스트 Dialog 를 보여준 뒤, 선택된 사용자가 올린 파일을 검색
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final List<CdpItem> teamMember = mCdpItemManager.mUsers;
        final CdpSelectListAdapter adapter = new CdpSelectListAdapter(this, teamMember);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mUserSelectDialog != null)
                    mUserSelectDialog.dismiss();
                mCurrentUserId = teamMember.get(i).id + "";
//                mSearchMode = JandiConstants.TYPE_SEARCH_USER_SPECIFIC;
                setFlagSelectedAllFilesTab(false);
                drawLayout();
                // 서치 시작
                doSearch();
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.title_search_user_select);
        dialog.setIcon(android.R.drawable.ic_menu_agenda);
        dialog.setView(view);
        mUserSelectDialog = dialog.show();
    }

    /************************************************************
     * 레이아웃 디스플레이
     ************************************************************/

    private void setFlagSelectedAllFilesTab(boolean isSelectedAllFilesTab) {
        if (isSelectedAllFilesTab) {
            mSearchUser = ReqSearchFile.USER_ID_ALL;
        } else {
            mSearchUser = mCurrentUserId;
        }
        this.isSelectedAllFilesTab = isSelectedAllFilesTab;
        this.isSelectedUsersTab = !isSelectedAllFilesTab;

    }
    @UiThread
    public void drawLayout() {
        if (isSelectedAllFilesTab) {
            layoutSearchMessageAllFiles.setBackgroundColor(getResources().getColor(R.color.jandi_file_search_tab_bg_active));
            layoutSearchMessageUser.setBackgroundColor(getResources().getColor(R.color.jandi_file_search_tab_bg_inactive));
        } else {
            layoutSearchMessageAllFiles.setBackgroundColor(getResources().getColor(R.color.jandi_file_search_tab_bg_inactive));
            layoutSearchMessageUser.setBackgroundColor(getResources().getColor(R.color.jandi_file_search_tab_bg_active));
        }

        if (mSearchUser.equals(ReqSearchFile.USER_ID_MINE)) {
            mCurrentUserName = "Just Me";
        } else if (mSearchUser.equals(ReqSearchFile.USER_ID_ALL)) {
            // DO NOTHING
        } else {
            if (mCdpItemManager != null) {
                mCurrentUserName = mCdpItemManager.getCdpNameById(Integer.parseInt(mSearchUser));
            }
        }
        textViewSearchMessageUserName.setText(mCurrentUserName);
    }

    /**
     * 모든 파일 탭을 터치한 경우
     */
    @Click(R.id.ly_search_message_all_files)
    void selectAllFileTab() {
        if (isSelectedUsersTab) {
            setFlagSelectedAllFilesTab(true);
            drawLayout();
            doSearch();
        }
    }

    /**
     * 사용자 탭을 터치한 경우
     */
    @Click(R.id.txt_search_message_user_name)
    void selectUserTab() {
        if (isSelectedAllFilesTab) {
            setFlagSelectedAllFilesTab(false);
            drawLayout();
            doSearch();
        }
    }

    /************************************************************
     * 검색
     ************************************************************/
    @UiThread
    void doSearch() {
        mProgressWheel.show();
        mAdapter.clearAdapter();
        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {
        tossRestClient.setHeader("Authorization", mMyToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;

            if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_IMAGES) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
            } else if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_PDF) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_PDF;
            } else {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_ALL;
            }

            reqSearchFile.userId = mSearchUser;

            ResSearchFile resSearchFile = tossRestClient.searchFile(reqSearchFile);
            mAdapter.insert(resSearchFile);

            log.debug("success to find " + resSearchFile.fileCount + " files.");
            doSearchDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            doSearchDone(false, "파일 검색에 실패했습니다");
        }
    }

    @UiThread
    void doSearchDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        if (isOk) {
            mAdapter.notifyDataSetChanged();
            isFirst = false;
        } else {
            ColoredToast.showError(this, message);
        }
    }

    @ItemClick
    void list_searched_messagesItemClicked(ResMessages.FileMessage searchedFile) {
        FileDetailActivity_
                .intent(this)
                .fileId(searchedFile.id)
                .start();
        EventBus.getDefault().postSticky(mCdpItemManager);
    }
}
