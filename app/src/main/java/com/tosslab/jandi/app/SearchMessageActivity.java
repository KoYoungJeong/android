package com.tosslab.jandi.app;

import android.app.ActionBar;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tosslab.jandi.app.lists.CdpItemManager;
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

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 21..
 */
@EActivity(R.layout.activity_search_message)
public class SearchMessageActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(SearchMessageActivity.class);
    @Extra
    public int mSearchMode;     // 서치 모드.   ALL || Images || PDFs

    private String mSearchUser;     // 사용자.     ALL || Mine || UserID
    private String mCurrentUserId;  // 현재 UserId

    @ViewById(R.id.ly_search_message_all_files)
    LinearLayout layoutSearchMessageAllFiles;
    @ViewById(R.id.ly_search_message_user)
    LinearLayout layoutSearchMessageUser;
    @ViewById(R.id.txt_search_message_file_type)
    TextView textViewSearchMessageFileType;
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
    private Spinner mSpinner;
    private boolean isFirst = true;

    @AfterViews
    void initView() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        View titleView = findViewById(titleId);

        // attach listener to this spinnerView for handling spinner selection change
        mSpinner = (Spinner) getLayoutInflater().inflate(R.layout.spinner_search_type, null);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isFirst) {
                    isFirst = false;
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
        ViewGroupUtils.replaceView(titleView, mSpinner);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(this);
        listSearchedMessages.setAdapter(mAdapter);

        // 선택한 서치 모드와 사용자를 셋팅
        // 내 파일 검색으로 시작한다면 두번째 텝으로 이동
        mCurrentUserId = ReqSearchFile.USER_ID_MINE;
        if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_USER) {
            setFlagSelectedAllFilesTab(false);
        } else {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                return true;

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
            layoutSearchMessageAllFiles.setBackgroundColor(getResources().getColor(R.color.bg_selected_tab));
            layoutSearchMessageUser.setBackgroundColor(getResources().getColor(R.color.bg_unselected_tab));
        } else {
            layoutSearchMessageAllFiles.setBackgroundColor(getResources().getColor(R.color.bg_unselected_tab));
            layoutSearchMessageUser.setBackgroundColor(getResources().getColor(R.color.bg_selected_tab));
        }
        String strUserName = "Just Me";
        if (mSearchUser.equals(ReqSearchFile.USER_ID_ALL) || mSearchUser.equals(ReqSearchFile.USER_ID_MINE)) {
            // DO Nothing
        } else {
            if (mCdpItemManager != null) {
                strUserName = mCdpItemManager.getCdpNameById(Integer.parseInt(mSearchUser));
            }
        }
        textViewSearchMessageUserName.setText(strUserName);

        switch (mSearchMode) {
            case JandiConstants.TYPE_SEARCH_MODE_IMAGES:
                textViewSearchMessageFileType.setText("Images");
                mSpinner.setSelection(1);
                break;
            case JandiConstants.TYPE_SEARCH_MODE_PDF:
                textViewSearchMessageFileType.setText("PDFs");
                mSpinner.setSelection(2);
                break;
            default:
                textViewSearchMessageFileType.setText("All Files");
                mSpinner.setSelection(0);
                break;
        }
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
