package com.tosslab.jandi.app.ui.search.filter.room;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.RoomFilterAdapter;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.view.RoomFilterDataView;
import com.tosslab.jandi.app.ui.search.filter.room.component.DaggerRoomFilterComponent;
import com.tosslab.jandi.app.ui.search.filter.room.module.RoomFilterModule;
import com.tosslab.jandi.app.ui.search.filter.room.presenter.RoomFilterPresenter;
import com.tosslab.jandi.app.utils.ProgressWheel;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * Created by tonyjs on 16. 7. 22..
 */
public class RoomFilterActivity extends BaseAppCompatActivity implements RoomFilterPresenter.View {
    public static final int REQUEST_CODE_VOICE = 8812;

    public static final String KEY_IS_TOPIC = "isTopic";
    public static final String KEY_SELECTED_ROOM_ID = "selectedRoomId";

    public static final String KEY_FILTERED_ROOM_ID = "roomId";

    @Inject
    InputMethodManager inputMethodManager;

    @Inject
    RoomFilterPresenter roomFilterPresenter;

    @Inject
    RoomFilterDataView roomFilterDataView;

    @Bind(R.id.et_room_filter)
    EditText etRoomFilter;

    @Bind(R.id.lv_room_filter)
    RecyclerView lvRoomFilter;

    @Bind(R.id.toolbar_room_filter)
    Toolbar toolbar;

    @Bind(R.id.btn_room_filter_topic)
    View btnRoomTypeTopic;

    @Bind(R.id.btn_room_filter_dm)
    View btnRoomTypeDirectMessage;

    private ProgressWheel progressWheel;

    private MenuItem menuDeleteQuery;
    private MenuItem menuVoiceInput;

    private RoomFilterPresenter.RoomType roomType;

    public static void startForResultWithDirectMessageId(Activity activity, long selectedRoomId, int requestCode) {
        Intent intent = new Intent(activity, RoomFilterActivity.class);
        intent.putExtra(KEY_IS_TOPIC, false);
        intent.putExtra(KEY_SELECTED_ROOM_ID, selectedRoomId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startForResultWithTopicId(Activity activity, long selectedRoomId, int requestCode) {
        Intent intent = new Intent(activity, RoomFilterActivity.class);
        intent.putExtra(KEY_IS_TOPIC, true);
        intent.putExtra(KEY_SELECTED_ROOM_ID, selectedRoomId);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_bottom_with_alpha, 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_filter);

        RoomFilterAdapter roomFilterAdapter = new RoomFilterAdapter();
        injectComponent(roomFilterAdapter);

        ButterKnife.bind(this);

        setupActionBar();

        initProgressWheel();

        initRoomFilterViews(roomFilterAdapter);

        initRooms();

        initSelectedRoomId();
    }

    private void initSelectedRoomId() {
        boolean isTopic = getIntent().getBooleanExtra(KEY_IS_TOPIC, false);
        long selectedRoomId = getIntent().getLongExtra(KEY_SELECTED_ROOM_ID, -1L);
        roomFilterPresenter.onInitializeSelectedRoomId(isTopic, selectedRoomId);
    }

    private void initRooms() {
        btnRoomTypeTopic.setSelected(true);
        roomType = RoomFilterPresenter.RoomType.Topic;

        roomFilterPresenter.onInitializeRooms(roomType);
    }

    private void initRoomFilterViews(RoomFilterAdapter roomFilterAdapter) {
        lvRoomFilter.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        lvRoomFilter.setAdapter(roomFilterAdapter);

        roomFilterDataView.setOnMemberClickListener(memberId -> {
            roomFilterPresenter.onMemberClickActionForGetRoomId(memberId);
        });

        roomFilterDataView.setOnTopicRoomClickListener(roomId -> {
            setResultRoomId(true, roomId);
            finish();
        });
    }

    private void injectComponent(RoomFilterAdapter roomFilterAdapter) {
        DaggerRoomFilterComponent.builder()
                .roomFilterModule(new RoomFilterModule(roomFilterAdapter, this))
                .build()
                .inject(this);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.account_icon_back);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
        progressWheel.setCancelable(false);
        progressWheel.setCanceledOnTouchOutside(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_filter_activity, menu);
        menuDeleteQuery = menu.findItem(R.id.action_close);
        menuVoiceInput = menu.findItem(R.id.action_voice);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_close:
                etRoomFilter.setText("");
                return true;
            case R.id.action_voice:
                startVoiceInput();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.et_room_filter)
    void onSearchRoom(CharSequence text) {
        roomFilterPresenter.onSearchRooms(text.toString(), roomType);

        boolean isEmpty = TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) <= 0;
        menuDeleteQuery.setVisible(!isEmpty);
        menuVoiceInput.setVisible(isEmpty);
    }

    @OnEditorAction(R.id.et_room_filter)
    boolean onSearchAction(TextView view, int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideKeyboard();
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(etRoomFilter.getWindowToken(), 0);
    }

    @OnClick(R.id.btn_room_filter_topic)
    void setRoomTypeToTopic(View view) {
        if (view.isSelected()) {
            return;
        }

        btnRoomTypeDirectMessage.setSelected(false);

        view.setSelected(true);
        roomType = RoomFilterPresenter.RoomType.Topic;

        roomFilterPresenter.onRoomTypeChanged(roomType, etRoomFilter.getText().toString());
    }

    @OnClick(R.id.btn_room_filter_dm)
    void setRoomTypeToDirectMessage(View view) {
        if (view.isSelected()) {
            return;
        }

        btnRoomTypeTopic.setSelected(false);

        view.setSelected(true);
        roomType = RoomFilterPresenter.RoomType.DirectMessage;

        roomFilterPresenter.onRoomTypeChanged(roomType, etRoomFilter.getText().toString());
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void hideProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        roomFilterDataView.notifyDataSetChanged();
    }

    @Override
    public void setResultRoomId(boolean isTopic, long roomId) {
        Intent intent = new Intent();
        intent.putExtra(KEY_IS_TOPIC, isTopic);
        intent.putExtra(KEY_FILTERED_ROOM_ID, roomId);
        setResult(RESULT_OK, intent);
    }

    public void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start the activity, the intent will be populated with the speech text
        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE);
        } catch (ActivityNotFoundException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VOICE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            List<String> voiceSearchResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (voiceSearchResults != null && voiceSearchResults.isEmpty()) {
                return;
            }

            roomFilterPresenter.onSearchRooms(voiceSearchResults.get(0), roomType);
        }
    }

    @Override
    protected void onDestroy() {
        roomFilterPresenter.stopTopicSearchQueue();
        roomFilterPresenter.stopDirectMessageSearchQueue();
        hideKeyboard();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_to_bottom);
    }
}
