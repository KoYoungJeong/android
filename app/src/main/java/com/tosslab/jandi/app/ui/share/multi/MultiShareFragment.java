package com.tosslab.jandi.app.ui.share.multi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.share.MainShareActivity;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareAdapterDataView;
import com.tosslab.jandi.app.ui.share.multi.adapter.ShareFragmentPageAdapter;
import com.tosslab.jandi.app.ui.share.multi.dagger.DaggerMultiShareComponent;
import com.tosslab.jandi.app.ui.share.multi.dagger.MultiShareModule;
import com.tosslab.jandi.app.ui.share.multi.presenter.MultiSharePresenter;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

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

public class MultiShareFragment extends Fragment implements MultiSharePresenter.View, MainShareActivity.Share {

    private static final int REQ_SELECT_TEAM = 1001;
    private static final int REQ_SELECT_ROOM = 1002;
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
    @Inject
    ShareAdapterDataView shareAdapterDataView;

    private List<String> uris;
    private ProgressWheel progressWheel;

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

        ShareFragmentPageAdapter adapter = new ShareFragmentPageAdapter(getFragmentManager());

        DaggerMultiShareComponent.builder()
                .multiShareModule(new MultiShareModule(this, adapter))
                .build()
                .inject(this);

        vpShare.setAdapter(adapter);

        uris = initUris(getArguments());

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

    private List<String> initUris(Bundle arguments) {
        List<String> uris = new ArrayList<>();
        if (arguments != null && arguments.containsKey(EXTRA_URIS)) {
            uris.addAll(arguments.getStringArrayList(EXTRA_URIS));
        }
        return uris;
    }

    @OnPageChange(R.id.vp_multi_share)
    void onFilePageSelected(int position) {
        multiSharePresenter.onFilePageChanged(position, etComment.getText().toString());
        setActionbarTitle(position, vpShare.getAdapter().getCount());
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
        ShareSelectRoomActivity_
                .intent(this)
                .teamId(teamId)
                .start();
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
    public void setFileTitle(String fileName) {
        tvTitle.setText(fileName);
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

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (mentionControlViewModel != null) {
            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(event.getId());
            searchedItemVO.setName(event.getName());
            searchedItemVO.setType(event.getType());
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }

}
