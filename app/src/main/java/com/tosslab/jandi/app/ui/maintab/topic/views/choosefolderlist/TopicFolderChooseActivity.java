package com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.adapter.TopicFolderChooseAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.presenter.TopicFolderChoosePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;

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
public class TopicFolderChooseActivity extends BaseAnalyticsActivity implements TopicFolderChoosePresenter.View {

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout vgInputEditText = (LinearLayout) LayoutInflater
                .from(this.getApplicationContext()).inflate(R.layout.input_edit_text_view, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_input);

        input.setMaxLines(1);
        input.setCursorVisible(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Folder Name");

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, displayMetrics);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, displayMetrics);
        vgInputEditText.setMinimumWidth(minWidth);
        vgInputEditText.setPadding(padding, input.getPaddingTop(), padding, input.getPaddingBottom());

        TextView tvTitle = new TextView(this.getApplicationContext());
        tvTitle.setText(R.string.jandi_folder_insert_name);
        tvTitle.setTextColor(getResources().getColor(R.color.black));
        tvTitle.setTextSize(20);
        int paddingTopLeftRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, displayMetrics);
        tvTitle.setPadding(paddingTopLeftRight, paddingTopLeftRight, paddingTopLeftRight, 0);

        builder.setView(vgInputEditText)
                .setTitle(getString(R.string.jandi_folder_insert_name))
                .setPositiveButton(getString(R.string.jandi_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewFolder(input.getText().toString());
                    }
                })
                .setNegativeButton(R.string.jandi_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
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