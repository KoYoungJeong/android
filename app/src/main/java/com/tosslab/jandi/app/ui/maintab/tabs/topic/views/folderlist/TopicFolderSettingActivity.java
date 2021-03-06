package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.DragnDropTouchHelper;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderMainAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter.TopicFolderSettingAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.dagger.DaggerTopicFolderSettingComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.dagger.TopicFolderSettingModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.presenter.TopicFolderSettingPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TopicFolderSettingActivity extends BaseAppCompatActivity
        implements TopicFolderSettingPresenter.View,
        TopicFolderSettingAdapter.OnFolderSeqChangeLisener,
        TopicFolderSettingAdapter.OnRemoveFolderListener,
        TopicFolderSettingAdapter.OnRenameFolderListener {

    public static final int ITEM_FOLDER_CHOOSE = 0x01;
    public static final int FOLDER_SETTING = 0x02;

    @Nullable
    @InjectExtra
    int mode = ITEM_FOLDER_CHOOSE;

    @Nullable
    @InjectExtra
    long topicId;

    @Nullable
    @InjectExtra
    long folderId;

    @Inject
    TopicFolderSettingPresenter topicFolderSettingPresentor;

    @Bind(R.id.tv_folder_title)
    TextView tvTitle;
    @Bind(R.id.rv_folder)
    RecyclerView lvTopicFolder;
    @Bind(R.id.ll_no_folder)
    LinearLayout vgNoFolder;
    @Bind(R.id.ll_folder_list)
    LinearLayout vgFolderList;

    private TopicFolderMainAdapter adapter;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_choose);
        ButterKnife.bind(this);
        Dart.inject(this);
        DaggerTopicFolderSettingComponent.builder()
                .topicFolderSettingModule(new TopicFolderSettingModule(this))
                .build()
                .inject(this);
        initObject();
        initView();
        EventBus.getDefault().register(this);
    }

    void initObject() {
        if (mode == ITEM_FOLDER_CHOOSE) {
            adapter = new TopicFolderChooseAdapter();
        } else {
            adapter = new TopicFolderSettingAdapter();
        }
    }

    void initView() {
        if (mode != ITEM_FOLDER_CHOOSE) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FolderManagement);
        }

        setupActionBar();

        lvTopicFolder.setLayoutManager(new LinearLayoutManager(TopicFolderSettingActivity.this,
                RecyclerView.VERTICAL, false));
        adapter.setFolderId(folderId);

        lvTopicFolder.setAdapter(adapter);

        topicFolderSettingPresentor.onRefreshFolders();
        switch (mode) {
            case ITEM_FOLDER_CHOOSE:
                tvTitle.setText(getResources().getString(R.string.jandi_folder_choose));
                break;
            case FOLDER_SETTING:
                tvTitle.setText(R.string.jandi_reorder_from_long_press);
                TopicFolderSettingAdapter settingAdapter = (TopicFolderSettingAdapter) adapter;
                ItemTouchHelper.Callback callback = new DragnDropTouchHelper(settingAdapter, () -> {
                    AnalyticsUtil.sendEvent(
                            AnalyticsValue.Screen.FolderManagement, AnalyticsValue.Action.ArrangeaFolder);
                });
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(lvTopicFolder);
                settingAdapter.setOnFolderSeqChangeLisener(this);
                settingAdapter.setOnRemoveFolderListener(this);
                settingAdapter.setOnRenameFolderListener(this);
                break;
        }

        adapter.setOnRecyclerItemClickListener((view, adapter, position, type) -> {
            topicFolderSettingPresentor.onItemClick(adapter, position, type, folderId, topicId);
        });

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MoveToaFolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_folder_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(TopicFolderRefreshEvent event) {
        topicFolderSettingPresentor.onRefreshFolders();
    }

    @Override
    public void showFolderList(List<TopicFolder> folders, boolean hasFolder) {
        if (hasFolder) {
            viewGroupSelect(true);
            adapter.clear();
            adapter.addAll(folders);
            adapter.notifyDataSetChanged();
        } else {
            viewGroupSelect(false);
        }
    }

    public void viewGroupSelect(boolean hasList) {
        if (hasList) {
            vgFolderList.setVisibility(View.VISIBLE);
            vgNoFolder.setVisibility(View.GONE);
        } else {
            vgFolderList.setVisibility(View.GONE);
            vgNoFolder.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_make_new_folder).setOnClickListener(v -> {
                showCreateNewFolderDialog(false);
            });
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        switch (mode) {
            case ITEM_FOLDER_CHOOSE:
                actionBar.setTitle(getString(R.string.jandi_folder_move_to));
                break;
            case FOLDER_SETTING:
                actionBar.setTitle(R.string.jandi_setting_folder);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_create_new_folder:
                showCreateNewFolderDialog(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showCreateNewFolderDialog(boolean fromActionBar) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TopicFolderSettingActivity.this,
                    R.style.JandiTheme_AlertDialog_FixWidth_300);

            RelativeLayout rootView = (RelativeLayout) LayoutInflater
                    .from(TopicFolderSettingActivity.this).inflate(R.layout.dialog_fragment_input_text, null);

            TextView tvTitle = (TextView) rootView.findViewById(R.id.tv_popup_title);
            EditText etInput = (EditText) rootView.findViewById(R.id.et_dialog_input_text);
            etInput.setHint(R.string.jandi_title_name);
            tvTitle.setText(R.string.jandi_folder_insert_name);

            builder.setView(rootView)
                    .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                        createNewFolder(etInput.getText().toString().trim());
                        etInput.setText("");

                        if (mode == ITEM_FOLDER_CHOOSE) {
                            if (fromActionBar) {
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.NewFolderonTop);
                            } else {
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.NewFolder);
                            }
                        } else {
                            if (fromActionBar) {
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FolderManagement, AnalyticsValue.Action.NewFolderonTop);
                            } else {
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FolderManagement, AnalyticsValue.Action.CreateNewFolder);
                            }
                        }
                    })
                    .setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
                        etInput.setText("");
                        dialog.cancel();
                    });
            alertDialog = builder.create();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            etInput.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() <= 0) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            });
        }
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public void createNewFolder(String title) {
        topicFolderSettingPresentor.onCreateFolers(title, folderId);


    }

    @Override
    public void finishAcitivty() {
        this.finish();
    }

    @Override
    public void showMoveToFolderToast(String folderName) {
        ColoredToast.show(getString(R.string.jandi_folder_has_been_move_to, folderName));
    }

    @Override
    public void showRemoveFromFolderToast(String name) {
        ColoredToast.show(getString(R.string.jandi_folder_has_been_remove_from, name));
    }

    @Override
    public void showAlreadyHasFolderToast() {
        ColoredToast.showWarning(getString(R.string.jandi_folder_alread_has_name));
    }

    @Override
    public void showFolderRenamedToast() {
        ColoredToast.show(getString(R.string.jandi_folder_renamed));
    }

    @Override
    public void showDeleteFolderToast() {
        ColoredToast.show(getString(R.string.jandi_folder_removed));
    }

    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void onSeqChanged(long folderId, int seq) {
        topicFolderSettingPresentor.modifySeqFolder(folderId, seq);
    }

    @Override
    public void onRemove(long folderId) {
        showDeleteFolderDialog(folderId);
    }

    private void showDeleteFolderDialog(long folderId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(this.getString(R.string.jandi_confirm), (dialog, which) -> {
                    topicFolderSettingPresentor.removeFolder(folderId);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FolderManagement, AnalyticsValue.Action.DeleteaFolder);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }

    private void showRenameFolderDialog(long folderId, String name, int seq) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(this).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.jandi_folder_rename);

        input.setText(name);
        input.setHint(R.string.jandi_entity_create_entity_name);
        input.setSelection(name.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(this.getString(R.string.jandi_confirm), (dialog, which) -> {
                    if (!name.equals(input.getText().toString().trim())) {
                        topicFolderSettingPresentor.modifyNameFolder(folderId, input.getText().toString().trim(), seq);
                    }
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FolderManagement, AnalyticsValue.Action.EditFolder_Rename);
                    dialog.cancel();
                })
                .setNegativeButton(this.getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() <= 0 || TextUtils.equals(name, s)) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onRename(long folderId, String folderName, int seq) {
        showRenameFolderDialog(folderId, folderName, seq);
    }

}