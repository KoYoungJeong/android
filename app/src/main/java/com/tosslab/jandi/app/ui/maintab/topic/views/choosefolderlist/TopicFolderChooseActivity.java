package com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.presenter.TopicFolderChoosePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

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
public class TopicFolderChooseActivity extends BaseAppCompatActivity implements TopicFolderChoosePresenter.View {

    @Extra
    int topicId;

    @Extra
    int folderId;
    @Bean
    TopicFolderChoosePresenter topicFolderChoosePresentor;
    @ViewById(R.id.rv_choose_folder)
    RecyclerView lvTopicFolderChoose;
    @ViewById(R.id.ll_no_folder)
    LinearLayout vgNoFolder;
    @ViewById(R.id.ll_folder_list)
    LinearLayout vgFolderList;
    private String currentItemFolderName = "";
    private TopicFolderChooseAdapter adapter;

    public void setCurrentTopicFolderName(String currentItemFolderName) {
        this.currentItemFolderName = currentItemFolderName;
    }

    @AfterInject
    void initObject() {
        adapter = new TopicFolderChooseAdapter();
        topicFolderChoosePresentor.setView(this);
    }

    @AfterViews
    void initView() {
        setupActionBar();
        lvTopicFolderChoose.setLayoutManager(new LinearLayoutManager(TopicFolderChooseActivity.this,
                RecyclerView.VERTICAL, false));
        adapter.setFolderId(folderId);
        lvTopicFolderChoose.setAdapter(adapter);
        topicFolderChoosePresentor.onRefreshFolders(folderId);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(TopicFolderChooseActivity.this);

        LinearLayout vgInputEditText = (LinearLayout) LayoutInflater
                .from(TopicFolderChooseActivity.this).inflate(R.layout.input_edit_text_view, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_input);

        builder.setView(vgInputEditText)
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    createNewFolder(input.getText().toString());
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.NewFolder);
                })
                .setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }

    public void createNewFolder(String title) {
        topicFolderChoosePresentor.onCreateFolers(title, folderId);
    }

    @Override
    public void finishAcitivty() {
        this.finish();
    }

    public void showMoveToFolderToast(String folderName) {
        ColoredToast.show(getApplicationContext(),
                getString(R.string.jandi_folder_has_been_move_to, folderName));
    }

    public void showRemoveFromFolderToast() {
        ColoredToast.show(getApplicationContext(),
                getString(R.string.jandi_folder_has_been_remove_from, currentItemFolderName));
    }

    public void showAlreadyHasFolderToast() {
        ColoredToast.show(getApplicationContext(),
                getString(R.string.jandi_folder_alread_has_name));
    }
}