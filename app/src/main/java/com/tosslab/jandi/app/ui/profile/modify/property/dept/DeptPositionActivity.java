package com.tosslab.jandi.app.ui.profile.modify.property.dept;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.modify.property.dept.adapter.DeptPositionAdapter;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by tee on 16. 7. 5..
 */
public class DeptPositionActivity extends BaseAppCompatActivity {

    public static final String RESULT_EXTRA = "result_extra";
    public static final String EXTRA_INPUT_MODE = "extra_input_mode";
    public static final String EXTRA_JOB_TITLE_MODE = "extra_job_title_mode";
    public static final String EXTRA_DEPARTMENT_MODE = "extra_department_mode";
    public static final String EXTRA_DEFAULT = "extra_default";

    @Bind(R.id.et_jobtitle_department_name)
    EditText etName;
    @Bind(R.id.lv_jobtitle_department)
    RecyclerView listView;
    @Bind(R.id.tv_jobtitle_department_count)
    TextView tvCount;
    @Bind(R.id.tv_jobtitle_department_list)
    TextView tvList;

    private String mode;
    private DeptPositionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobtitle_department_search_list);
        setMode();
        setupActionbar();
        ButterKnife.bind(this);
        listView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        adapter = new DeptPositionAdapter();
        adapter.setMode(mode);
        listView.setAdapter(adapter);
        setOnTextChangeListener();
        adapter.setOnItemClickListener(data -> {
            etName.setText(data);
            etName.setSelection(data.length());
        });
        initView();
    }

    private void setMode() {
        mode = getIntent().getStringExtra(EXTRA_INPUT_MODE);
    }

    private void initView() {
        if (TextUtils.equals(mode, EXTRA_JOB_TITLE_MODE)) {
            getSupportActionBar().setTitle(R.string.jandi_job_title);
            etName.setHint(R.string.jandi_enter_your_job_title);
            tvList.setText(R.string.jandi_job_title_list);
        } else if (TextUtils.equals(mode, EXTRA_DEPARTMENT_MODE)) {
            getSupportActionBar().setTitle(R.string.jandi_profile_division);
            etName.setHint(R.string.jandi_enter_your_dept);
            tvList.setText(R.string.jandi_department_list);
        }
        String defaultName = getIntent().getStringExtra(EXTRA_DEFAULT);
        if (!TextUtils.isEmpty(defaultName)) {
            etName.setText(defaultName);
            etName.setSelection(defaultName.length());
        } else {
            etName.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_done, menu);
        return true;
    }

    private void setDepartmentList(String keyword) {
        final List<Pair<String, Boolean>> datas = new ArrayList<>();
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<User> users = teamInfoLoader.getUserList();

        if (TextUtils.isEmpty(keyword)) {
            Observable.from(users)
                    .map((user) -> user.getDivision())
                    .filter(department -> !TextUtils.isEmpty(department))
                    .distinct()
                    .map((Func1<String, Pair<String, Boolean>>) string -> new Pair(string, false))
                    .toSortedList((leftPair, rightPair) -> {
                        return StringCompareUtil.compare(leftPair.first, rightPair.first);
                    })
                    .subscribe(pairs -> {
                        datas.addAll(pairs);
                    }, Throwable::printStackTrace);
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
            adapter.dataRefresh();
            return;
        }

        Observable.from(users)
                .filter(user -> {
                    if (keyword != null && keyword.length() > 0) {
                        return user.getDivision().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .map((user) -> user.getDivision())
                .distinct()
                .map((Func1<String, Pair<String, Boolean>>) string -> new Pair(string, false))
                .toSortedList((leftPair, rightPair) -> {
                    return StringCompareUtil.compare(leftPair.first, rightPair.first);
                })
                .subscribe(pairs -> {
                    datas.addAll(pairs);
                }, Throwable::printStackTrace);
        if (datas.size() > 0) {
            if (!datas.contains(new Pair(keyword, false))) {
                datas.add(new Pair(keyword, true));
            }
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
        } else {
            datas.add(new Pair(keyword, true));
            adapter.setDatas(datas);
        }
        adapter.dataRefresh();
    }


    private void setJobTitleList(String keyword) {
        final List<Pair<String, Boolean>> datas = new ArrayList<>();
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        List<User> users = teamInfoLoader.getUserList();

        if (TextUtils.isEmpty(keyword)) {
            Observable.from(users)
                    .map((user) -> user.getPosition())
                    .filter(jobTitle -> !TextUtils.isEmpty(jobTitle))
                    .distinct()
                    .map((Func1<String, Pair<String, Boolean>>) string -> new Pair(string, false))
                    .toSortedList((leftPair, rightPair) -> {
                        return StringCompareUtil.compare(leftPair.first, rightPair.first);
                    })
                    .subscribe(pairs -> {
                        datas.addAll(pairs);
                    }, Throwable::printStackTrace);
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
            adapter.dataRefresh();
            return;
        }

        Observable.from(users)
                .filter(user -> {
                    if (keyword != null && keyword.length() > 0) {
                        return user.getPosition().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .map((user) -> user.getPosition())
                .distinct()
                .map((Func1<String, Pair<String, Boolean>>) string -> new Pair(string, false))
                .toSortedList((leftPair, rightPair) -> {
                    return StringCompareUtil.compare(leftPair.first, rightPair.first);
                })
                .subscribe(pairs -> {
                    datas.addAll(pairs);
                }, Throwable::printStackTrace);
        if (datas.size() > 0) {
            if (!datas.contains(new Pair(keyword, false))) {
                datas.add(new Pair(keyword, true));
            }
            adapter.setDatas(datas);
            adapter.setKeyword(keyword);
        } else {
            datas.add(new Pair(keyword, true));
            adapter.setDatas(datas);
        }
        adapter.dataRefresh();
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
                if (mode.equals(EXTRA_DEPARTMENT_MODE)) {
                    setDepartmentList(s.toString());
                } else if (mode.equals(EXTRA_JOB_TITLE_MODE)) {
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