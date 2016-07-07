package com.tosslab.jandi.app.ui.profile.inputlist;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.inputlist.adapter.InputProfileListAdapter;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * Created by tee on 16. 7. 5..
 */
public class InputProfileListActivity extends BaseAppCompatActivity {

    public static final String RESULT_EXTRA = "result_extra";
    public static final String INPUT_MODE = "input_mode";
    public static final String JOB_TITLE_MODE = "job_title_mode";
    public static final String DEPARTMENT_MODE = "department_mode";
    public String mode;

    @Bind(R.id.et_jobtitle_department_name)
    EditText etName;

    @Bind(R.id.lv_jobtitle_department)
    RecyclerView listView;

    @Bind(R.id.tv_jobtitle_department_count)
    TextView tvCount;

    @Bind(R.id.tv_jobtitle_department_list)
    TextView tvList;

    private InputProfileListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobtitle_department_search_list);
        setMode();
        setupActionbar();
        ButterKnife.bind(this);
        listView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        adapter = new InputProfileListAdapter();
        adapter.setMode(mode);
        listView.setAdapter(adapter);
        setOnTextChangeListener();
        adapter.setOnItemClickListener(division -> {
            etName.setText(division);
            etName.setSelection(division.length());
        });
    }

    private void setMode() {
        Intent intent = getIntent();
        mode = intent.getStringExtra(INPUT_MODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        if (mode.equals(JOB_TITLE_MODE)) {
            getSupportActionBar().setTitle(R.string.jandi_job_title);
            etName.setHint(R.string.jandi_insert_new_job_title);
            tvList.setText(R.string.jandi_job_title_list);
        } else if (mode.equals(DEPARTMENT_MODE)) {
            getSupportActionBar().setTitle(R.string.jandi_department);
            etName.setHint(R.string.jandi_insert_new_department);
            tvList.setText(R.string.jandi_department_list);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_done, menu);
        return true;
    }

    private void setDepartmentList(String keyword) {
        final List<String> datas = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<User> users = teamInfoLoader.getUserList();
        Observable.from(users)
                .filter(user -> {
                    if (set.contains(user.getDivision())) {
                        return false;
                    }
                    if (keyword != null && keyword.length() > 0) {
                        return user.getDivision().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .map((user) -> {
                    set.add(user.getDivision());
                    return user.getDivision();
                })
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs, rhs);
                })
                .subscribe(strings -> {
                    datas.addAll(strings);
                });
        if (datas.size() > 0) {
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
            adapter.hasDataRefresh();
        } else {
            datas.add(keyword);
            adapter.setDatas(datas);
            adapter.hasNoDataRefresh();
        }
    }


    private void setJobTitleList(String keyword) {
        final List<String> datas = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<User> users = teamInfoLoader.getUserList();
        Observable.from(users)
                .filter(user -> {
                    if (set.contains(user.getPosition())) {
                        return false;
                    }
                    if (keyword != null && keyword.length() > 0) {
                        return user.getPosition().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .map((user) -> {
                    set.add(user.getPosition());
                    return user.getPosition();
                })
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs, rhs);
                })
                .subscribe(strings -> {
                    datas.addAll(strings);
                });
        if (datas.size() > 0) {
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
            adapter.hasDataRefresh();
        } else {
            datas.add(keyword);
            adapter.setDatas(datas);
            adapter.hasNoDataRefresh();
        }
    }

    public void setOnTextChangeListener() {
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                tvCount.setText(String.valueOf(s.length()));
                if (s.length() > 60) {
                    s.replace(60, s.length(), "");
                }
                if (mode.equals(DEPARTMENT_MODE)) {
                    setDepartmentList(s.toString());
                } else if (mode.equals(JOB_TITLE_MODE)) {
                    setJobTitleList(s.toString());
                }
            }
        });
    }

    public void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setTitle(R.string.jandi_profile_division);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_EXTRA, etName.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}