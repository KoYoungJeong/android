package com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.presentor.TopicFolderChoosePresentor;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 8. 30..
 */
@EActivity(R.layout.activity_folder_choose)
@OptionsMenu(R.menu.choose_folder_menu)
public class TopicFolderChooseActivity extends BaseAnalyticsActivity implements TopicFolderChoosePresentor.View {

    @Extra
    int topicId;

    @Extra
    int folderId;

    @Bean
    TopicFolderChoosePresentor topicFolderChoosePresentor;
    @ViewById(R.id.rv_choose_folder)
    RecyclerView rvTopicFolderChoose;
    @ViewById(R.id.ll_no_folder)
    LinearLayout vgNoFolder;
    @ViewById(R.id.ll_folder_list)
    LinearLayout vgFolderList;

    private TopicFolderChooseAdapter adapter;

    @AfterInject
    void initObject() {
        adapter = new TopicFolderChooseAdapter();
        topicFolderChoosePresentor.setView(this);
    }

    @AfterViews
    void initView() {
        setupActionBar();
        rvTopicFolderChoose.setLayoutManager(new LinearLayoutManager(TopicFolderChooseActivity.this,
                RecyclerView.VERTICAL, false));
        rvTopicFolderChoose.addItemDecoration(new SimpleDividerItemDecoration(TopicFolderChooseActivity.this));
        rvTopicFolderChoose.setAdapter(adapter);
        topicFolderChoosePresentor.onRefreshFolders();

        adapter.setOnRecyclerItemClickListener((view, adapter, position, type) -> {
            topicFolderChoosePresentor.onItemClick(adapter, position, type, folderId, topicId);
        });

    }

    @UiThread
    @Override
    public void showFolderList(List<ResFolder> folders) {
        if (folders.size() != 0) {
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
        actionBar.setTitle("Move topic");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);
        input.setHint("Folder Name");
        input.setMaxLines(1);
        input.setCursorVisible(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input)
                .setTitle("Insert folder name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewFolder(input.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void createNewFolder(String title) {
        try {
            topicFolderChoosePresentor.onCreateFolers(title);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finishAcitivty() {
        this.finish();
    }

}