package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.DragnDropTouchHelper;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.TopicFolderMainAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter.TopicFolderSettingAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.presenter.TopicFolderSettingPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by tee on 15. 8. 30..
 */
@EActivity(R.layout.activity_folder_choose)
@OptionsMenu(R.menu.choose_folder_menu)
public class TopicFolderSettingActivity extends BaseAppCompatActivity
        implements TopicFolderSettingPresenter.View,
        TopicFolderSettingAdapter.OnFolderSeqChangeLisener,
        TopicFolderSettingAdapter.OnRemoveFolderListener,
        TopicFolderSettingAdapter.OnRenameFolderListener {

    public static final int ITEM_FOLDER_CHOOSE = 0x01;
    public static final int FOLDER_SETTING = 0x02;

    @Extra
    int mode = ITEM_FOLDER_CHOOSE;

    @Extra
    int topicId;

    @Extra
    int folderId;
    @Bean
    TopicFolderSettingPresenter topicFolderChoosePresentor;

    @ViewById(R.id.rv_choose_folder)
    RecyclerView lvTopicFolder;
    @ViewById(R.id.ll_no_folder)
    LinearLayout vgNoFolder;
    @ViewById(R.id.ll_folder_list)
    LinearLayout vgFolderList;

    private String currentItemFolderName = "";
    private TopicFolderMainAdapter adapter;
    private AlertDialog alertDialog;

    public void setCurrentTopicFolderName(String currentItemFolderName) {
        this.currentItemFolderName = currentItemFolderName;
    }

    @AfterInject
    void initObject() {
        if (mode == ITEM_FOLDER_CHOOSE) {
            adapter = new TopicFolderChooseAdapter();
        } else {
            adapter = new TopicFolderSettingAdapter();
        }
        topicFolderChoosePresentor.setView(this);
    }

    @AfterViews
    void initView() {
        setupActionBar();

        lvTopicFolder.setLayoutManager(new LinearLayoutManager(TopicFolderSettingActivity.this,
                RecyclerView.VERTICAL, false));
        adapter.setFolderId(folderId);

        lvTopicFolder.setAdapter(adapter);

        topicFolderChoosePresentor.onRefreshFolders(folderId);

        if (mode == FOLDER_SETTING) {
            TopicFolderSettingAdapter settingAdapter = (TopicFolderSettingAdapter) adapter;
            ItemTouchHelper.Callback callback = new DragnDropTouchHelper(settingAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(lvTopicFolder);
            settingAdapter.setOnFolderSeqChangeLisener(this);
            settingAdapter.setOnRemoveFolderListener(this);
            settingAdapter.setOnRenameFolderListener(this);
        }

        adapter.setOnRecyclerItemClickListener((view, adapter, position, type) -> {
            topicFolderChoosePresentor.onItemClick(adapter, position, type, folderId, topicId);
        });

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MoveToaFolder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    @UiThread
    @Override
    public void showFolderList(List<ResFolder> folders, boolean hasFolder) {
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
                showCreateNewFolderDialog();
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
        actionBar.setTitle(getString(R.string.jandi_folder_move_to));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_create_new_folder:
                showCreateNewFolderDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showCreateNewFolderDialog() {

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
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.NewFolder);
                    })
                    .setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
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
        topicFolderChoosePresentor.onCreateFolers(title, folderId);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishAcitivty() {
        this.finish();
    }

    public void showMoveToFolderToast(String folderName) {
        ColoredToast.show(getString(R.string.jandi_folder_has_been_move_to, folderName));
    }

    public void showRemoveFromFolderToast() {
        ColoredToast.show(getString(R.string.jandi_folder_has_been_remove_from, currentItemFolderName));
    }

    public void showAlreadyHasFolderToast() {
        ColoredToast.show(getString(R.string.jandi_folder_alread_has_name));
    }

    @Override
    public void onSeqChanged(int folderId, int seq) {
        topicFolderChoosePresentor.modifySeqFolder(folderId, seq);
    }

    @Override
    public void onRemove(int folderId) {
        showDeleteFolderDialog(folderId);
    }

    private void showDeleteFolderDialog(int folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(this.getString(R.string.jandi_confirm), (dialog, which) -> {
                    topicFolderChoosePresentor.removeFolder(folderId);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }

    private void showRenameFolderDialog(int folderId, String name, int seq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(this).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.jandi_folder_rename);

        input.setText(name);
        input.setHint(R.string.jandi_title_name);
        input.setSelection(name.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(this.getString(R.string.jandi_confirm), (dialog, which) -> {
                    topicFolderChoosePresentor.modifyNameFolder(folderId, input.getText().toString().trim(), seq);
                })
                .setNegativeButton(this.getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        input.addTextChangedListener(new SimpleTextWatcher() {
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

    @Override
    public void onRename(int folderId, String folderName, int seq) {
        showRenameFolderDialog(folderId, folderName, seq);
    }

}