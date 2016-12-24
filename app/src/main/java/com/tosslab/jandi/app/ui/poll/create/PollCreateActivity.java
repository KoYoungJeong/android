package com.tosslab.jandi.app.ui.poll.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarUtils;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.CalendarDialogFragment;
import com.tosslab.jandi.app.dialogs.TimePickerDialogFragment;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.poll.create.dagger.DaggerPollCreateComponent;
import com.tosslab.jandi.app.ui.poll.create.dagger.PollCreateModule;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by tonyjs on 16. 6. 16..
 */
public class PollCreateActivity extends BaseAppCompatActivity
        implements CalendarDialogFragment.OnDateSelectedListener,
        TimePickerDialogFragment.OnHourSelectedListener,
        PollCreatePresenter.View {

    private static final String KEY_TOPIC_ID = "topicId";

    @Inject
    PollCreatePresenter pollCreatePresenter;

    @Bind(R.id.toolbar_create_poll)
    Toolbar toolbar;
    @Bind(R.id.vg_create_poll_item_wrapper)
    ViewGroup vgPollItems;
    @Bind(R.id.btn_create_poll_item_add)
    View btnCreatePoll;
    @Bind(R.id.switch_create_poll_anonymous)
    SwitchCompat switchAnonymous;
    @Bind(R.id.switch_create_poll_multiplechoice)
    SwitchCompat switchMultipleChoice;
    @Bind(R.id.tv_create_poll_date)
    TextView tvCreatePollDate;
    @Bind(R.id.tv_create_poll_time)
    TextView tvCreatePollTime;

    private ProgressWheel progressWheel;

    public static void start(Activity activity, long topicId) {
        Intent intent = new Intent(activity, PollCreateActivity.class);
        intent.putExtra(KEY_TOPIC_ID, topicId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        injectComponent();

        ButterKnife.bind(this);

        setupActionBar();

        initProgressWheel();

        initPollCreate();

        addDefaultPollItem();

        Calendar tomorrow = CalendarUtils.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        onDateSelected(CalendarDay.from(tomorrow));

        onHourSelected(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1);
    }

    private void addDefaultPollItem() {
        addPollItem();
        addPollItem();
    }

    private void initPollCreate() {
        long topicId = getIntent().getLongExtra(KEY_TOPIC_ID, 0);
        if (topicId <= 0) {
            showUnExpectedErrorToast();
            finish();
            return;
        }
        pollCreatePresenter.initializePollCreateBuilder(topicId);
    }

    private void injectComponent() {
        DaggerPollCreateComponent.builder()
                .pollCreateModule(new PollCreateModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_create_poll_item_add)
    void addPollItem() {
        final int position = vgPollItems.getChildCount();
        if (position > 2) {
            sendAnalyticsEvent(AnalyticsValue.Action.AddChoice);
        }

        LayoutInflater inflater = getLayoutInflater();
        final View itemView = inflater.inflate(R.layout.layout_create_poll_item, vgPollItems, false);

        final EditText etTitle = (EditText) itemView.findViewById(R.id.et_create_poll_item_title);
        etTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pollCreatePresenter.onPollItemInput(position, s.toString());
            }
        });

        View btnItemDelete = itemView.findViewById(R.id.btn_create_poll_item_delete);
        btnItemDelete.setVisibility(position >= 2 ? View.VISIBLE : View.GONE);
        btnItemDelete.setOnClickListener(v -> {
            sendAnalyticsEvent(AnalyticsValue.Action.DeleteChoice);
            pollCreatePresenter.onPollItemRemove(position);
            vgPollItems.removeView(itemView);

            int childCount = vgPollItems.getChildCount();
            if (childCount < 2) {
                for (int i = 0; i < childCount; i++) {
                    View child = vgPollItems.getChildAt(i);
                    child.findViewById(R.id.btn_create_poll_item_delete).setVisibility(View.GONE);
                }
            }

            View child = vgPollItems.getChildAt(childCount - 1);
            if (child != null) {
                child.requestFocus();
            }
        });

        vgPollItems.addView(itemView);
        if (position > 1) {
            etTitle.requestFocus();
        }

        int childCount = vgPollItems.getChildCount();
        if (childCount >= 2) {
            for (int i = 0; i < childCount; i++) {
                View child = vgPollItems.getChildAt(i);
                child.findViewById(R.id.btn_create_poll_item_delete).setVisibility(View.VISIBLE);
            }
        }

        if (childCount >= 31) {
            btnCreatePoll.setVisibility(View.GONE);
        }

    }

    @OnTextChanged(R.id.et_create_poll_subject)
    void onSubjectChanged(CharSequence subject) {
        pollCreatePresenter.onPollSubjectChanged(subject.toString());
    }

    @OnClick(R.id.btn_create_poll_duedate)
    void showDueDateChoiceView() {
        CalendarDialogFragment fragment = new CalendarDialogFragment();
        fragment.show(getSupportFragmentManager(), "calendar_dialog");
    }

    @OnClick(R.id.btn_create_poll_duedate_time)
    void showHourChoiceView() {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.show(getSupportFragmentManager(), "time_picker_dialog");
    }

    @Override
    public void onDateSelected(CalendarDay calendarDay) {
        Calendar calendar = calendarDay.getCalendar();
        setDate(calendar);
        pollCreatePresenter.onPollDueDateSelected(calendar);
    }

    private void setDate(Calendar calendar) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        DateFormat dateFormat = null;
        switch (locale.getLanguage()) {
            case "ko":
                dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일(E)");
                break;
            case "zh":
                dateFormat = new SimpleDateFormat("yyyy年 MM月 dd日 E");
                break;
            case "ja":
                dateFormat = new SimpleDateFormat("yyyy年 MM月 dd日(E)");
                break;
            default:
                dateFormat = new SimpleDateFormat("E MMM dd, yyyy");
                break;
        }
        tvCreatePollDate.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onHourSelected(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        setTime(calendar);
        pollCreatePresenter.onPollDueDateHourSelected(hour);
    }

    private void setTime(Calendar calendar) {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        tvCreatePollTime.setText(DateTransformator.getTimeStringForSimple(calendar.getTime()));
    }

    @OnClick(R.id.btn_create_poll_anonymous)
    void onClickAnonymous() {
        boolean checked = switchAnonymous.isChecked();
        boolean future = !checked;
        pollCreatePresenter.onPollAnonymousOptionChanged(future);
        switchAnonymous.setChecked(future);

        sendAnalyticsEvent(AnalyticsValue.Action.Anonymous,
                future ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    @OnClick(R.id.btn_create_poll_multiplechoice)
    void onClickMultipleChoice() {
        boolean checked = switchMultipleChoice.isChecked();
        boolean future = !checked;
        pollCreatePresenter.onPollMultipleChoiceOptionChanged(future);
        switchMultipleChoice.setChecked(future);

        sendAnalyticsEvent(AnalyticsValue.Action.AllowMultipleChoices,
                future ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    @OnClick(R.id.btn_create_poll)
    void createPoll() {
        pollCreatePresenter.onCreatePoll();
        sendAnalyticsEvent(AnalyticsValue.Action.CreatePoll);
    }

    @Override
    public void showNotEnoughPollItemsToast() {
        ColoredToast.showError(R.string.jandi_input_enough_items);
    }

    @Override
    public void showEmptySubjectToast() {
        ColoredToast.showError(R.string.jandi_empty_subject);
    }

    @Override
    public void showUnExpectedErrorToast() {
        ColoredToast.showError(R.string.jandi_err_unexpected);
    }

    @Override
    public void showDueDateCannotBePastTimeToast() {
        ColoredToast.showError(R.string.jandi_duedate_cannotbe_past_time);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_poll_create);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CreatePoll, action);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, AnalyticsValue.Label label) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CreatePoll, action, label);
    }
}
