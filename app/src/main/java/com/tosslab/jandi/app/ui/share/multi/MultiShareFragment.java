package com.tosslab.jandi.app.ui.share.multi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbDivideItemDecorator;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.share.MainShareActivity;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareAdapterDataView;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareFragmentPageAdapter;
import com.tosslab.jandi.app.ui.share.multi.dagger.DaggerMultiShareComponent;
import com.tosslab.jandi.app.ui.share.multi.dagger.MultiShareModule;
import com.tosslab.jandi.app.ui.share.multi.interaction.FileShareInteractor;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenter;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;

public class MultiShareFragment extends Fragment implements MultiSharePresenter.View, MainShareActivity.Share, FileShareInteractor.Wrapper {

    private static final int REQ_SELECT_TEAM = 1001;
    private static final String EXTRA_URIS = "uris";

    @Inject
    MultiSharePresenter multiSharePresenter;

    MentionControlViewModel mentionControlViewModel;

    @Bind(R.id.vp_multi_share)
    ViewPager vpShare;

    @Bind(R.id.vg_multi_share_file_icon)
    View vgFileStatus;

    @Bind(R.id.iv_multi_share_previous)
    ImageView ivPreviousScroll;

    @Bind(R.id.iv_multi_share_next)
    ImageView ivNextScroll;

    @Bind(R.id.tv_multi_share_image_title)
    TextView tvTitle;
    @Bind(R.id.tv_multi_share_team_name)
    TextView tvTeamName;
    @Bind(R.id.tv_multi_share_room_name)
    TextView tvRoomName;
    @Bind(R.id.et_multi_share_comment)
    EditText etComment;
    @Bind(R.id.lv_multi_share_thumbs)
    RecyclerView lvFileThumbs;
    @Bind(R.id.vg_multi_share_content)
    View vgComment;

    @Inject
    ShareAdapterDataView shareAdapterDataView;
    @Inject
    FileShareInteractor fileShareInteractor;

    @InjectExtra
    ArrayList<String> uris;
    private ProgressWheel progressWheel;
    private FileUploadThumbAdapter fileUploadThumbAdapter;
    private InputMethodManager inputMethodManager;

    public static MultiShareFragment create(List<Uri> uris) {
        MultiShareFragment fragment = new MultiShareFragment();
        Bundle bundle = new Bundle();
        Observable.from(uris)
                .map(Object::toString)
                .collect((Func0<ArrayList<String>>) ArrayList::new, ArrayList::add)
                .subscribe(strings -> bundle.putStringArrayList(EXTRA_URIS, strings));
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_share, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        Dart.inject(this, getArguments());

        ShareFragmentPageAdapter adapter = new ShareFragmentPageAdapter(getChildFragmentManager(), this);

        DaggerMultiShareComponent.builder()
                .multiShareModule(new MultiShareModule(this, adapter))
                .build()
                .inject(this);

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        vpShare.setAdapter(adapter);

        multiSharePresenter.initShareTarget();
        multiSharePresenter.initShareData(uris);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_share);
            if (item != null) {
                item.setEnabled(tvRoomName.length() > 0);
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @OnPageChange(R.id.vp_multi_share)
    void onFilePageSelected(int position) {
        multiSharePresenter.onFilePageChanged(position, etComment.getText().toString());
        setActionbarTitle(position, vpShare.getAdapter().getCount());

        int itemCount = fileUploadThumbAdapter.getItemCount();
        for (int idx = 0; idx < itemCount; idx++) {
            fileUploadThumbAdapter.getItem(idx).setSelected(idx == position);
        }
        fileUploadThumbAdapter.notifyDataSetChanged();
    }

    @Override
    public void setUpScrollButton(int position, int count) {
        if (position == 0) {
            ivPreviousScroll.setVisibility(View.GONE);
        } else {
            ivPreviousScroll.setVisibility(View.VISIBLE);
        }

        if (position == count - 1) {
            ivNextScroll.setVisibility(View.GONE);
        } else {
            ivNextScroll.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(value = {R.id.iv_multi_share_previous, R.id.iv_multi_share_next})
    void onScrollButtonClick(View view) {
        if (view.getId() == R.id.iv_multi_share_previous) {
            vpShare.setCurrentItem(vpShare.getCurrentItem() - 1);
        } else {
            vpShare.setCurrentItem(vpShare.getCurrentItem() + 1);

        }
    }

    @OnClick(R.id.vg_multi_share_team)
    void onTeamNameClick() {
        startActivity(new Intent(getActivity(), ShareSelectTeamActivity.class));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @OnClick(R.id.vg_multi_share_room)
    void onRoomNameClick() {
        multiSharePresenter.onRoomChange();
    }

    @Override
    public void callRoomSelector(long teamId) {
        RoomFilterActivity.startForResultWithTeamId(getActivity(), teamId,
                MainShareActivity.REQ_SELECT_ROOM);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @Override
    public void updateFiles(int pageCount) {

        shareAdapterDataView.refresh();

        setUpScrollButton(0, pageCount);
        setActionbarTitle(0, vpShare.getAdapter().getCount());
    }

    private void setActionbarTitle(int index, int childCount) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar == null) return;

        String title = String.format("%s (%d/%d)",
                getString(R.string.jandi_share_to_jandi),
                index + 1,
                childCount);
        actionBar.setTitle(title);
    }

    @Override
    public void moveIntro() {
        IntroActivity.startActivity(getActivity(), false);

        getActivity().finish();

    }

    @Override
    public void setTeamName(String teamName) {
        tvTeamName.setText(teamName);
    }

    @Override
    public void setRoomName(String roomName) {
        tvRoomName.setText(roomName);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void setMentionInfo(long teamId, long roomId) {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.reset();
        }

        if (roomId <= 0) {
            return;
        }

        mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(),
                etComment,
                teamId,
                Arrays.asList(roomId),
                MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
        mentionControlViewModel.setUpMention(etComment.getText().toString());
    }

    @Override
    public void setCommentText(String comment) {
        etComment.setText(comment);
        if (mentionControlViewModel != null) {
            mentionControlViewModel.setUpMention(comment);
        }
    }

    @Override
    public void moveRoom(long teamId, long roomId) {
        Completable.fromAction(() -> {
            if (getActivity() == null) {
                return;
            }
            UploadNotificationActivity.startActivity(getActivity(), teamId, roomId);

            getActivity().finish();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void showProgress() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
            progressWheel.setCancelable(false);
            progressWheel.setCanceledOnTouchOutside(false);
        }
        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showSelectRoomToast() {
        ColoredToast.showError(R.string.jandi_title_cdp_to_be_shared);
    }

    @Override
    public void startShare() {
        multiSharePresenter.updateComment(vpShare.getCurrentItem(), etComment.getText().toString());
        multiSharePresenter.startShare();

    }

    public void onEvent(ShareSelectTeamEvent event) {
        long teamId = event.getTeamId();
        multiSharePresenter.onSelectTeam(teamId);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        long roomId = event.getRoomId();
        multiSharePresenter.onSelectRoom(roomId);
    }

    public void onEvent(FileUploadPreviewImageClickEvent event) {
        inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (mentionControlViewModel != null) {
            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(event.getId());
            searchedItemVO.setName(event.getName());
            searchedItemVO.setType(event.getType());
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }

    @OnClick(R.id.tv_file_rename_button)
    void onClickFileRename() {
        showRenameTitleDialog();
    }

    private void showRenameTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.common_fileupload_rename_description);
        String filename = multiSharePresenter.getFileName(vpShare.getCurrentItem());
        String extension = getFileExtension(filename);
        String filenameWithoutExtension = filename.replaceAll(extension, "");
        input.setText(filenameWithoutExtension);
        input.setHint(getString(R.string.jandi_name));
        input.setSelection(filenameWithoutExtension.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    String renamedFileName = input.getText().toString() + extension;
                    multiSharePresenter.changeFileName(
                            vpShare.getCurrentItem(), renamedFileName);
                    setFileName(renamedFileName);
                    tvTitle.requestFocus();
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if (name.trim().length() <= 0
                        || TextUtils.equals(filenameWithoutExtension, s)
                        || name.contains("\\")
                        || name.contains("/")
                        || name.contains(":")
                        || name.contains("*")
                        || name.contains("?")
                        || name.contains("\"")
                        || name.contains("<")
                        || name.contains(">")
                        || name.contains("|")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    @Override
    public void setFileName(String fileName) {
        String extension = getFileExtension(fileName);
        int lastIndexOf = fileName.lastIndexOf(extension);
        final SpannableStringBuilder filenameSp = new SpannableStringBuilder(fileName);

        filenameSp.setSpan(new ForegroundColorSpan(0xff333333),
                lastIndexOf, lastIndexOf + extension.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTitle.setText(filenameSp);
    }

    @Override
    public void setFileThumbInfos(List<FileUploadThumbAdapter.FileThumbInfo> fileThumbInfos) {
        if (fileThumbInfos.size() > 1) {
            lvFileThumbs.setVisibility(View.VISIBLE);

            fileUploadThumbAdapter = new FileUploadThumbAdapter();
            fileUploadThumbAdapter.setFileThumbInfo(fileThumbInfos);
            fileUploadThumbAdapter.setItemClickListener((view, adapter, position) -> {
                vpShare.setCurrentItem(position);
            });

            lvFileThumbs.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            lvFileThumbs.addItemDecoration(new FileUploadThumbDivideItemDecorator());
            lvFileThumbs.setAdapter(fileUploadThumbAdapter);
        } else {
            lvFileThumbs.setVisibility(View.GONE);
        }


    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }
        return extension;
    }

    @Override
    public void toggleContent() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (vgComment.getVisibility() != View.VISIBLE) {
            // 보이도록 하기, 배경 흰색
            vgComment.setVisibility(View.VISIBLE);
            if (fileUploadThumbAdapter != null && fileUploadThumbAdapter.getItemCount() > 1) {
                lvFileThumbs.setVisibility(View.VISIBLE);
            }
            vpShare.setBackgroundColor(Color.WHITE);
            if (actionBar != null) {
                actionBar.show();
            }
            fileShareInteractor.onFocusContent(false);
        } else {
            // 안보이게 하기, 배경 검정
            vgComment.setVisibility(View.GONE);
            lvFileThumbs.setVisibility(View.GONE);
            vpShare.setBackgroundColor(Color.BLACK);
            if (actionBar != null) {
                actionBar.hide();
            }
            fileShareInteractor.onFocusContent(true);

            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }
}
