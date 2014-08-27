package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.CategorizingAsOwner;
import com.tosslab.jandi.app.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.events.ReadyToRetrievePrivateGroupList;
import com.tosslab.jandi.app.events.ReadyToRetrieveUserList;
import com.tosslab.jandi.app.events.RetrieveChannelList;
import com.tosslab.jandi.app.events.RetrievePrivateGroupList;
import com.tosslab.jandi.app.events.RetrieveUserList;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.UserEntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileTypeSimpleListAdapter;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(MainTabActivity.class);

    @RestService
    TossRestClient mTossRestClient;

    @ViewById(R.id.drawer_user_profile)
    ImageView imageViewUserProfile;
    @ViewById(R.id.drawer_user_id)
    TextView textViewUserEmail;
    @ViewById(R.id.drawer_user_name)
    TextView textViewUserName;

    private int mCurrentTabIndex = 0;
    private String mMyToken;
    private ProgressWheel mProgressWheel;
    private Context mContext;

    private EntityManager mEntityManager;
    private JandiEntityClient mJandiEntityClient;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private MainTabPagerAdapter mMainTabPagerAdapter;
    private ViewPager mViewPager;

    private boolean isReadyToRetrieveEntityList = false;

    @AfterViews
    void initView() {
        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        // Network Client 설정
        mJandiEntityClient = new JandiEntityClient(mTossRestClient, mMyToken);

        // Drawer
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawer = (LinearLayout)findViewById(R.id.drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                setActionBarForDrawerClose();
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                setActionBarForDrawerOpen();
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // ViewPager
        mMainTabPagerAdapter = new MainTabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        mViewPager.setAdapter(mMainTabPagerAdapter);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(mViewPager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentTabIndex = position;
                switch (position) {
                    case 3:
                        setActionBarForFileList();
                        break;
                    default:
                        setActionBar();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    public void setActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    public void setActionBarForDrawerOpen() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_drawer);
    }

    public void setActionBarForDrawerClose() {
        switch (mCurrentTabIndex) {
            case 3:
                setActionBarForFileList();
                break;
            default:
                setActionBar();
                break;
        }
    }

    private void setActionBarForFileList() {
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_file_list_tab);
        actionBar.setDisplayShowCustomEnabled(true);

        setSpinnerAsCategorizingAccodingByFileType();
        setSpinnerAsCategorizingAccodingByUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        // Push가 MainTabActivity를 보고 있을 때
        // 발생한다면 알람 카운트 갱신을 위한 BR 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(JandiConstants.PUSH_REFRESH_ACTION);
        registerReceiver(mRefreshEntities, intentFilter);
        // Entity의 리스트를 획득하여 저장한다.
        getEntities();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mRefreshEntities);
        super.onPause();
    }

    public void onEvent(ReadyToRetrieveChannelList event) {
        log.debug("onEvent : ReadyToRetrieveChannelList");
        if (isReadyToRetrieveEntityList) {
            postShowChannelListEvent();
        }
    }

    public void onEvent(ReadyToRetrieveUserList event) {
        log.debug("onEvent : ReadyToRetrieveUserList");
        if (isReadyToRetrieveEntityList) {
            postShowUserListEvent();
        }
    }

    public void onEvent(ReadyToRetrievePrivateGroupList event) {
        log.debug("onEvent : ReadyToRetrievePrivateGroupList");
        if (isReadyToRetrieveEntityList) {
            postShowPrivateGroupListEvent();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawer)) {
                mDrawerLayout.closeDrawer(mDrawer);
            } else {
                mDrawerLayout.openDrawer(mDrawer);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /************************************************************
     * 왼쪽 메뉴 설정
     ************************************************************/

    private void showDrawerUserProfile() {
        FormattedEntity me = mEntityManager.getMe();
        textViewUserEmail.setText(me.getUserEmail());
        textViewUserName.setText(me.getUserName());
        Picasso.with(mContext)
                .load(me.getUserProfileUrl())
                .placeholder(R.drawable.jandi_profile)
                .transform(new CircleTransform())
                .into(imageViewUserProfile);
    }

    @Click(R.id.drawer_action_setting)
    public void closeDrawerAndMoveToSettingActivity() {
        mDrawerLayout.closeDrawer(mDrawer);
        SettingsActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
    }

    /************************************************************
     * Entities List Update / Refresh
     ************************************************************/

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @UiThread
    public void getEntities() {
        getEntitiesInBackground();
    }

    @Background
    public void getEntitiesInBackground() {
        try {
            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo();
            getEntitiesDone(true, resLeftSideMenu, null);
        } catch (JandiException e) {
            log.error("get entity failed", e);
            getEntitiesDone(false, null, getString(R.string.err_expired_session));
        }
    }

    @UiThread
    public void getEntitiesDone(boolean isOk, ResLeftSideMenu resLeftSideMenu, String errMessage) {
        log.debug("getEntitiesDone");
        if (isOk) {
            mEntityManager = new EntityManager(resLeftSideMenu);
            trackSigningIn(mEntityManager);
            isReadyToRetrieveEntityList = true;
            showDrawerUserProfile();
            getActionBar().setTitle(mEntityManager.getTeamName());
            postAllEvents();
        } else {
            ColoredToast.showError(mContext, errMessage);
            returnToLoginActivity();
        }
    }

    private void postAllEvents() {
        postShowChannelListEvent();
        postShowUserListEvent();
        postShowPrivateGroupListEvent();
    }

    private void postShowChannelListEvent() {
        EventBus.getDefault().post(
                new RetrieveChannelList(mEntityManager)
        );
    }

    private void postShowUserListEvent() {
        EventBus.getDefault().post(new RetrieveUserList(mEntityManager.getUsersWithoutMe()));
    }

    private void postShowPrivateGroupListEvent() {
        EventBus.getDefault().post(
                new RetrievePrivateGroupList(mEntityManager)
        );
    }

    public EntityManager getEntityManager() {
        return mEntityManager;
    }


    /************************************************************
     * File tab 을 위한 액션바와 카테고리 선택 다이얼로그, 이벤트 전달
     ************************************************************/
    private String mCurrentUserNameCategorizingAccodingBy = null;
    private String mCurrentFileTypeCategorizingAccodingBy = null;


    private AlertDialog mFileTypeSelectDialog;
    private AlertDialog mUserSelectDialog;  // 사용자별 검색시 사용할 리스트 다이얼로그

    private void setSpinnerAsCategorizingAccodingByFileType() {
        LinearLayout categoryFileType = (LinearLayout) findViewById(R.id.actionbar_file_list_type);
        final TextView textViewFileType = (TextView) findViewById(R.id.actionbar_file_list_type_text);
        textViewFileType.setText(
                (mCurrentFileTypeCategorizingAccodingBy == null)
                        ? getString(R.string.jandi_file_category_all)
                        : mCurrentFileTypeCategorizingAccodingBy
        );
        categoryFileType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileTypeDialog(textViewFileType);
            }
        });
    }

    private void setSpinnerAsCategorizingAccodingByUser() {
        LinearLayout categoryUser = (LinearLayout) findViewById(R.id.actionbar_file_list_user);
        final TextView textViewUser = (TextView) findViewById(R.id.actionbar_file_list_user_text);
        textViewUser.setText(
                (mCurrentUserNameCategorizingAccodingBy == null)
                        ? getString(R.string.jandi_file_category_everyone)
                        : mCurrentUserNameCategorizingAccodingBy
        );
        categoryUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUsersDialog(textViewUser);
            }
        });
    }

    /**
     * 파일 타입 리스트 Dialog 를 보여준 뒤, 선택된 타입만 검색하라는 이벤트를
     * FileListFragment에 전달
     * @param textVewFileType
     */
    private void showFileTypeDialog(final TextView textVewFileType) {
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final FileTypeSimpleListAdapter adapter = new FileTypeSimpleListAdapter(this);
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // TODO : List를 User가 아닌 FormattedUser로 바꾸면 addHeader가 아니라 List에서
        // TODO : Everyone 용으로 0번째 item을 추가할 수 있음. 그럼 아래 note 로 적힌 인덱스가 밀리는 현상 해결됨.
        // TODO : 뭐가 더 나은지는 모르겠네잉

        final List<FormattedEntity> teamMember = mEntityManager.getUsers();
        final UserEntitySimpleListAdapter adapter = new UserEntitySimpleListAdapter(this, mEntityManager.getUsers());

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
                    EventBus.getDefault().post(new CategorizingAsOwner(CategorizingAsOwner.EVERYONE));
                } else {
                    ResLeftSideMenu.User owner = teamMember.get(i - 1).getUser();
                    log.debug(owner.id + " is selected");
                    mCurrentUserNameCategorizingAccodingBy = owner.name;
                    textViewUser.setText(mCurrentUserNameCategorizingAccodingBy);
                    EventBus.getDefault().post(new CategorizingAsOwner(owner.id));
                }
            }
        });
        lv.addHeaderView(getHeaderViewAsAllUser());
        lv.setAdapter(adapter);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_file_search_user);
        dialog.setView(view);
        mUserSelectDialog = dialog.show();
        mUserSelectDialog.setCanceledOnTouchOutside(true);
    }

    private View getHeaderViewAsAllUser() {
        View headerView = getLayoutInflater().inflate(R.layout.item_select_cdp, null, false);
        TextView textView = (TextView) headerView.findViewById(R.id.txt_select_cdp_name);
        textView.setText(R.string.jandi_file_category_everyone);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.img_select_cdp_icon);
        imageView.setImageResource(R.drawable.jandi_profile);
        return headerView;
    }

    /**
     * JandiGCMBroadcastReceiver로부터 Push가 들어왔다는 event가 MainTabActivity를 보고 있을 때
     * 발생한다면 알람 카운트 갱신을 위해 다시 받아온다.
     */
    private BroadcastReceiver mRefreshEntities = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getEntities();
        }
    };
}
