package com.tosslab.jandi.app.ui.poll.create;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.CalendarDialogFragment;
import com.tosslab.jandi.app.dialogs.TimePickerDialogFragment;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.poll.create.component.DaggerPollCreateComponent;
import com.tosslab.jandi.app.ui.poll.create.module.PollCreateModule;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

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

    @Inject
    PollCreatePresenter pollCreatePresenter;

    @Bind(R.id.vg_create_poll_item_wrapper)
    ViewGroup vgPollItems;

    @Bind(R.id.switch_create_poll_anonymous)
    SwitchCompat switchAnonymous;
    @Bind(R.id.switch_create_poll_multiplechoice)
    SwitchCompat switchMultipleChoice;

    public static void start(Activity activity, long topicId) {
        Intent intent = new Intent(activity, PollCreateActivity.class);
        intent.putExtra("topicId", topicId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        DaggerPollCreateComponent.builder()
                .pollCreateModule(new PollCreateModule(this))
                .build()
                .inject(this);

        ButterKnife.bind(this);

        addPollItem();

        long topicId = getIntent().getLongExtra("topicId", 0);
        if (topicId <= 0) {
            showUnExpectedErrorToast();
            finish();
            return;
        }

        pollCreatePresenter.initializePollCreateBuilder(topicId);
    }

    @OnClick(R.id.btn_create_poll_item_add)
    void addPollItem() {
        final int position = vgPollItems.getChildCount();

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
            pollCreatePresenter.onPollItemRemove(position);
            vgPollItems.removeView(itemView);

            if (vgPollItems.getChildCount() < 2) {
                for (int i = 0; i < vgPollItems.getChildCount(); i++) {
                    View child = vgPollItems.getChildAt(i);
                    child.findViewById(R.id.btn_create_poll_item_delete).setVisibility(View.GONE);
                }
            }

            View child = vgPollItems.getChildAt(vgPollItems.getChildCount() - 1);
            if (child != null) {
                child.requestFocus();
            }
        });

        vgPollItems.addView(itemView);
        if (position != 0) {
            etTitle.requestFocus();
        }

        if (vgPollItems.getChildCount() >= 2) {
            for (int i = 0; i < vgPollItems.getChildCount(); i++) {
                View child = vgPollItems.getChildAt(i);
                child.findViewById(R.id.btn_create_poll_item_delete).setVisibility(View.VISIBLE);
            }
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
        pollCreatePresenter.onPollDueDateSelected(calendarDay.getCalendar());
    }

    @Override
    public void onHourSelected(int hour) {
        pollCreatePresenter.onPollDueDateHourSelected(hour);
    }

    @OnClick(R.id.btn_create_poll_anonymous)
    void onClickAnonymous() {
        boolean checked = switchAnonymous.isChecked();
        pollCreatePresenter.onPollAnonymousOptionChanged(!checked);
        switchAnonymous.setChecked(!checked);
    }

    @OnClick(R.id.btn_create_poll_multiplechoice)
    void onClickMultipleChoice() {
        boolean checked = switchMultipleChoice.isChecked();
        pollCreatePresenter.onPollMultipleChoiceOptionChanged(!checked);
        switchMultipleChoice.setChecked(!checked);
    }

    @OnClick(R.id.btn_create_poll)
    void createPoll() {
        pollCreatePresenter.onCreatePoll();
    }

    @Override
    public void showEmptyDueDateToast() {
        ColoredToast.showError("DueDate 설정해라.");
    }

    @Override
    public void showNotEnoughPollItemsToast() {
        ColoredToast.showError("아이템들 넣어라.");
    }

    @Override
    public void showEmptySubjectToast() {
        ColoredToast.showError("Subject 적어라.");
    }

    @Override
    public void showUnExpectedErrorToast() {
        ColoredToast.showError("API 에러다.");
    }

    @Override
    public void showSuccessToast() {
        ColoredToast.show("성공했다.");
    }

    @Override
    public void showChooseRightDueDateToast() {
        ColoredToast.showError("시간 다시 골라라.");
    }
}
