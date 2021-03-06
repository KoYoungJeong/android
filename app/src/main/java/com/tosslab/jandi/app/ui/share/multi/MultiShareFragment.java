package com.tosslab.jandi.app.ui.share.multi;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;
import com.tosslab.jandi.app.team.TeamInfoLoader;
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
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FileAccessLimitUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.PricingPlanWarningViewController;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

public class MultiShareFragment extends Fragment implements MultiSharePresenter.View, MainShareActivity.Share, FileShareInteractor.Wrapper {

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
    @Bind(R.id.vg_coordinator)
    ViewGroup vgCoordinator;

    @Bind(R.id.vg_multi_share_team)
    ViewGroup vgMultiShareTeam;
    @Bind(R.id.vg_multi_share_room)
    ViewGroup vgMultiShareRoom;
    @Bind(R.id.v_restrict_warning)
    View vRestrictWarning;
    @Bind(R.id.vg_restrict_warning)
    ViewGroup vgRestrictWarning;


    @Inject
    ShareAdapterDataView shareAdapterDataView;
    @Inject
    FileShareInteractor fileShareInteractor;
    @InjectExtra
    ArrayList<String> uris;
    private ProgressWheel progressWheel;
    private FileUploadThumbAdapter fileUploadThumbAdapter;
    private InputMethodManager inputMethodManager;

    private FileAccessLimitUtil fileAccessLimitUtil;
    private long teamId = -1;

    private BottomSheetBehavior bottomSheetBehavior;
    private PublishSubject<Object> scrollButtonPublishSubject;
    private Subscription subscribe;
    private int viewPagerCurrentPosition = 0;

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

    // (노티바- 스크린 크기 - 액션바 크기 - 아이템리스트뷰 - 하단 바텀시트) /2 - scroll높이 /2
    // 위의 공식대로 하면 좌우 스크롤 이미지 뷰의 마진 높이를 구할 수 있다.
    private void setScrollPosition() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final int height = display.getHeight();
        int contentAreaHeight = height;
        contentAreaHeight -= UiUtils.getPixelFromDp(24f); // 노티바
        contentAreaHeight -= UiUtils.getPixelFromDp(249.5f); // 바텀시트
        if (lvFileThumbs.getVisibility() == View.VISIBLE) {
            contentAreaHeight -= UiUtils.getPixelFromDp(60f); //아이템 리스트 뷰
        }
        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
        contentAreaHeight -= actionBarHeight; // 액션바
        int scrollHeight = (int) UiUtils.getPixelFromDp(60f);
        int scrollmargin = contentAreaHeight / 2 - scrollHeight / 2;
        RelativeLayout.LayoutParams layoutParamsPreviewScroll =
                (RelativeLayout.LayoutParams) ivPreviousScroll.getLayoutParams();
        layoutParamsPreviewScroll.addRule(RelativeLayout.CENTER_VERTICAL, 0);
        layoutParamsPreviewScroll.setMargins(0, scrollmargin, 0, 0);
        ivPreviousScroll.setLayoutParams(layoutParamsPreviewScroll);
        RelativeLayout.LayoutParams layoutParamsNextScroll =
                (RelativeLayout.LayoutParams) ivNextScroll.getLayoutParams();
        layoutParamsNextScroll.addRule(RelativeLayout.CENTER_VERTICAL, 0);
        layoutParamsNextScroll.setMargins(0, scrollmargin, 0, 0);
        ivNextScroll.setLayoutParams(layoutParamsNextScroll);
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

        fileAccessLimitUtil = FileAccessLimitUtil.newInstance();

        setTeamDefaultName();

        long teamId = TeamInfoLoader.getInstance().getTeamId();

        if (teamId > 0) {
            fileAccessLimitUtil.execute(getContext(), teamId, () -> {
                multiSharePresenter.initShareTarget();
            });
        }

        multiSharePresenter.initShareData(uris);

        setHasOptionsMenu(true);

        fileAccessLimitUtil = FileAccessLimitUtil.newInstance();

        bottomSheetBehavior = BottomSheetBehavior.from(vgComment);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        etComment.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        setKeyboardVisibleEvent();
        setOnPageChanged();

        setPricingLimitView();
    }

    private void setKeyboardVisibleEvent() {
        KeyboardVisibilityEvent.setEventListener(
                getActivity(), isOpen -> {
                    if (isOpen) {
                        if (vgRestrictWarning.getVisibility() == View.VISIBLE) {
                            vgRestrictWarning.setVisibility(View.INVISIBLE);
                        }
                        etComment.setMaxLines(9);
                        vgMultiShareTeam.setVisibility(View.GONE);
                        vgMultiShareRoom.setVisibility(View.GONE);
                        Completable.complete()
                                .delay(400, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    bottomSheetBehavior.setPeekHeight((int) UiUtils.getPixelFromDp(249.5f));
                                });
                    } else {
                        if (vgRestrictWarning.getVisibility() == View.INVISIBLE) {
                            if (etComment.getLineCount() > 1) {
                                vgRestrictWarning.setVisibility(View.GONE);
                            } else {
                                vgRestrictWarning.setVisibility(View.VISIBLE);
                            }
                        }
                        vgMultiShareTeam.setVisibility(View.VISIBLE);
                        vgMultiShareRoom.setVisibility(View.VISIBLE);
                        if (lvFileThumbs.getVisibility() == View.VISIBLE) {
                            etComment.setMaxLines(15);
                        } else {
                            etComment.setMaxLines(18);
                        }
                    }
                });
    }

    private void setOnPageChanged() {
        vpShare.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                multiSharePresenter.onFilePageChanged(position, etComment.getText().toString());
                setActionbarTitle(position, vpShare.getAdapter().getCount());

                int itemCount = fileUploadThumbAdapter.getItemCount();
                for (int idx = 0; idx < itemCount; idx++) {
                    fileUploadThumbAdapter.getItem(idx).setSelected(idx == position);
                }
                fileUploadThumbAdapter.notifyDataSetChanged();
                lvFileThumbs.getLayoutManager().scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void setTeamId(long teamId) {
        this.teamId = teamId;
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
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void setUpScrollButton(int position, int count) {
        viewPagerCurrentPosition = position;
        if (position != 0) {
            ivPreviousScroll.setVisibility(View.VISIBLE);
        } else {
            ivPreviousScroll.setVisibility(View.GONE);
        }

        if (position != count - 1) {
            ivNextScroll.setVisibility(View.VISIBLE);
        } else {
            ivNextScroll.setVisibility(View.GONE);
        }

        if (scrollButtonPublishSubject == null) {
            scrollButtonPublishSubject = PublishSubject.create();
            subscribe = scrollButtonPublishSubject.throttleWithTimeout(3000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        if (viewPagerCurrentPosition != 0) {
                            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
                            animation.setDuration(200);
                            animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
                            animation.setAnimationListener(new SimpleEndAnimationListener() {
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    ivPreviousScroll.setVisibility(View.GONE);
                                }
                            });
                            ivPreviousScroll.startAnimation(animation);
                        }

                        if (viewPagerCurrentPosition != count - 1) {
                            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
                            animation.setDuration(200);
                            animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
                            animation.setAnimationListener(new SimpleEndAnimationListener() {
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    ivNextScroll.setVisibility(View.GONE);
                                }
                            });
                            ivNextScroll.startAnimation(animation);
                        }
                    });
        }

        scrollButtonPublishSubject.onNext(new Object());
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
        if (teamId <= 0) {
            teamId = -1;
        }

        startActivity(Henson.with(getContext())
                .gotoShareSelectTeamActivity()
                .selectedTeamId(teamId)
                .build());

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
    public void setTeamDefaultName() {
        tvTeamName.setHint(
                JandiApplication.getContext().getString(R.string.common_sharetojandi_choose_team));
    }

    @Override
    public void setRoomName(String roomName) {
        tvRoomName.setText(roomName);
        getActivity().invalidateOptionsMenu();
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
        if (teamId != -1) {
            fileAccessLimitUtil.execute(getContext(), teamId, () -> {
                multiSharePresenter.updateComment(vpShare.getCurrentItem(), etComment.getText().toString());
                multiSharePresenter.startShare();
            });
        }
    }

    public void onEvent(ShareSelectTeamEvent event) {
        long teamId = event.getTeamId();
        if (teamId != -1) {
            fileAccessLimitUtil.execute(getContext(), teamId, () -> {
                this.teamId = teamId;
                multiSharePresenter.onSelectTeam(teamId);
            });
        }
    }

    public void onEvent(ShareSelectRoomEvent event) {
        long roomId = event.getRoomId();
        multiSharePresenter.onSelectRoom(roomId);
        Completable.complete()
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                });
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

    @OnClick(R.id.vg_multi_share_image_title)
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
                    Completable.complete()
                            .delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                            });

                })
                .setNegativeButton(getString(R.string.jandi_cancel), (dialog, which) -> {
                    Completable.complete()
                            .delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                            });
                });

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
            etComment.setMaxLines(15);
        } else {
            lvFileThumbs.setVisibility(View.GONE);
            etComment.setMaxLines(18);
        }
        setScrollPosition();
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
            if (vgRestrictWarning.getVisibility() == View.INVISIBLE) {
                vgRestrictWarning.setVisibility(View.VISIBLE);
            }
            if (fileUploadThumbAdapter != null && fileUploadThumbAdapter.getItemCount() > 1) {
                lvFileThumbs.setVisibility(View.VISIBLE);
            }
            vpShare.setBackgroundColor(Color.WHITE);
            if (actionBar != null) {
                actionBar.show();
            }
            fileShareInteractor.onFocusContent(false);
            setScrollPosition();
        } else {
            // 안보이게 하기, 배경 검정
            vgComment.setVisibility(View.GONE);
            if (vgRestrictWarning.getVisibility() == View.VISIBLE) {
                vgRestrictWarning.setVisibility(View.INVISIBLE);
            }
            lvFileThumbs.setVisibility(View.GONE);
            vpShare.setBackgroundColor(Color.BLACK);
            if (actionBar != null) {
                actionBar.hide();
            }
            fileShareInteractor.onFocusContent(true);

            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);

            RelativeLayout.LayoutParams nextScrollLayoutParams =
                    (RelativeLayout.LayoutParams) ivNextScroll.getLayoutParams();
            nextScrollLayoutParams.setMargins(0, 0, 0, 0);
            nextScrollLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            ivNextScroll.setLayoutParams(nextScrollLayoutParams);

            RelativeLayout.LayoutParams prevScrollLayoutParams =
                    (RelativeLayout.LayoutParams) ivPreviousScroll.getLayoutParams();
            prevScrollLayoutParams.setMargins(0, 0, 0, 0);
            prevScrollLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            ivPreviousScroll.setLayoutParams(prevScrollLayoutParams);
        }
    }

    public void setPricingLimitView() {
        long fileSize = TeamInfoLoader.getInstance().getTeamUsage().getFileSize();
        boolean isExceedThreshold = fileSize > 1024 * 1024 * 1024 * 4.5;
        boolean isFree = TeamInfoLoader.getInstance().getTeamPlan().getPricing().equals("free");
        boolean isNotShowWithin3Days = JandiPreference.isExceedPopupWithin3Days();
        if (isExceedThreshold && isFree && !isNotShowWithin3Days) {
            vgRestrictWarning.setVisibility(View.VISIBLE);
            PricingPlanWarningViewController pricingPlanWarningViewController
                    = PricingPlanWarningViewController.with(getActivity(), vRestrictWarning);
            pricingPlanWarningViewController.bind();
            pricingPlanWarningViewController.setOnClickRemoveViewListener(() -> {
                vgRestrictWarning.setVisibility(View.GONE);
            });
        } else {
            vgRestrictWarning.setVisibility(View.GONE);
        }
    }

}
