package com.tosslab.jandi.app.ui.share;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.share.model.MainShareModel;
import com.tosslab.jandi.app.ui.share.multi.MultiShareFragment;
import com.tosslab.jandi.app.ui.share.text.TextShareFragment;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainShareActivity extends BaseAppCompatActivity {

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final String FRAGMENT_TAG = "share";
    public static final int REQ_SELECT_ROOM = 1002;
    MainShareModel mainShareModel;

    private Share share;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_share);
        mainShareModel = new MainShareModel();
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    void initViews() {
        setupActionbar();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        boolean used = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        IntentType intentType = mainShareModel.getIntentType(action, type);

        if (intentType == null || used) {
            ColoredToast.show(R.string.jandi_unsupported_share_contents);
            finish();
            return;
        }

        if (!mainShareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(getString(R.string.err_profile_get_info));
            startIntro();
            finish();
            return;
        }

        if (intentType == IntentType.Text && intent.getParcelableExtra(Intent.EXTRA_STREAM) == null) {
            setUpFragment(intent, intentType);
        } else {
            Permissions.getChecker()
                    .activity(MainShareActivity.this)
                    .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .hasPermission(() -> setUpFragment(intent, intentType))
                    .noPermission(() ->
                            ActivityCompat.requestPermissions(MainShareActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQ_STORAGE_PERMISSION))
                    .check();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(MainShareActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> {
                    new Handler().postDelayed(() -> {
                        Intent intent = getIntent();
                        setUpFragment(intent, mainShareModel.getIntentType(intent.getAction(), intent.getType()));
                    }, 300);
                }, this::finish)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(MainShareActivity.this);
                })
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    private void setUpFragment(Intent intent, IntentType intentType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (fragment != null) {
            return;
        }

        boolean attached = false;
        if (intentType == IntentType.Text && intent.getParcelableExtra(Intent.EXTRA_STREAM) == null) {

            attached = attachTextShareFragment(intent);

        } else if (intentType == IntentType.Multiple) {

            attached = attachMultiShareFragment(intent);

        } else if (intentType == IntentType.File ||
                (intentType == IntentType.Text && intent.getParcelableExtra(Intent.EXTRA_STREAM) != null)) {

            attached = attachSingleShareFragment(intent);

        }

        if (attached) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SharetoJandi);
        } else {
            ColoredToast.show(R.string.jandi_unsupported_share_contents);
            finish();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof Share) {
            this.share = (Share) fragment;
        }
    }

    private boolean attachTextShareFragment(Intent intent) {
        String subject = mainShareModel.getShareSubject(intent);
        CharSequence text = mainShareModel.getShareText(intent);
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        TextShareFragment fragment = TextShareFragment.create(this, subject, text.toString());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();

        return true;
    }

    private boolean attachSingleShareFragment(Intent intent) {
        Uri uri = mainShareModel.getShareFile(intent);
        if (uri == null) {
            return false;
        }

        MultiShareFragment fragment = MultiShareFragment.create(Arrays.asList(uri));
        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();
        return true;
    }

    private boolean attachMultiShareFragment(Intent intent) {
        List<Uri> uris = mainShareModel.getShareFiles(intent);
        if (uris == null || uris.isEmpty()) {
            return false;
        }

        MultiShareFragment fragment = MultiShareFragment.create(uris);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();
        return true;
    }

    private void startIntro() {
        IntroActivity.startActivity(MainShareActivity.this, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                if (share != null) {
                    share.startShare();
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.Send);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_share);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(R.string.jandi_share_to_jandi);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQ_SELECT_ROOM:
                boolean isTopic = data.getBooleanExtra(RoomFilterActivity.KEY_IS_TOPIC, false);

                long roomId = -1;
                if (isTopic) {
                    roomId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1);
                } else {
                    roomId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_MEMBER_ID, -1);
                }
                ShareSelectRoomEvent event = new ShareSelectRoomEvent();
                event.setRoomId(roomId);
                EventBus.getDefault().post(event);
                break;
        }
    }

    public enum IntentType {
        Text, Multiple, File
    }

    public interface Share {
        void startShare();
    }
}
