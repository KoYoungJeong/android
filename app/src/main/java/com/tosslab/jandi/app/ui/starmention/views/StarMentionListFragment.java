package com.tosslab.jandi.app.ui.starmention.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldStarMentionedEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.adapter.StarMentionListAdapter;
import com.tosslab.jandi.app.ui.starmention.presentor.StarMentionListPresentor;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 29..
 */
@EFragment(R.layout.fragment_all_star_list)
public class StarMentionListFragment extends Fragment implements StarMentionListPresentor.View {

    @FragmentArg("list_type")
    String listType;

    @ViewById(R.id.tv_no_content)
    TextView tvNoContent;

    @ViewById(R.id.rv_star_mention)
    RecyclerView lvStarMention;

    @ViewById(R.id.ll_empty_list_star_mention)
    LinearLayout llEmptyListStarMention;

    @ViewById(R.id.progress_starmentioned_list)
    ProgressBar moreLoadingProgressBar;

    @Bean
    StarMentionListPresentor starMentionListPresentor;

    AlertDialog unstarredDialog;

    StarMentionListAdapter starMentionListAdapter;

    @AfterViews
    void initFragment() {

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        starMentionListPresentor.setView(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lvStarMention.setVerticalScrollBarEnabled(true);
        lvStarMention.setLayoutManager(layoutManager);
        lvStarMention.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        starMentionListAdapter = new StarMentionListAdapter();
        starMentionListAdapter.setListType(listType);
        starMentionListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (starMentionListAdapter.getItemCount() == 0) {
                    lvStarMention.setVisibility(View.GONE);
                    llEmptyListStarMention.setVisibility(View.VISIBLE);
                } else {
                    lvStarMention.setVisibility(View.VISIBLE);
                    llEmptyListStarMention.setVisibility(View.GONE);
                }
            }
        });
        lvStarMention.setAdapter(starMentionListAdapter);
        loadStarMentionList();
        setOnItemClickListener();
        if (!listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            setOnItemLongClickListener();
        }
    }

    void loadStarMentionList() {
        starMentionListPresentor.addStarMentionMessagesToList(listType);
    }

    private void initNoContentMessage() {
        if (listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            tvNoContent.setText(R.string.jandi_mention_no_mentions);
        } else if (listType.equals(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL)) {
            tvNoContent.setText(R.string.jandi_starred_no_all);
        } else if (listType.equals(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES)) {
            tvNoContent.setText(R.string.jandi_starred_no_file);
        }
    }

    @Override
    @UiThread
    public void onAddAndShowList(List<StarMentionVO> starMentionMessageList) {
        starMentionListAdapter.addStarMentionList(starMentionMessageList);
        initNoContentMessage();
    }

    public void setOnItemClickListener() {
        StarMentionListAdapter.OnItemClickListener onItemClickListener = (adapter, position) -> {
            StarMentionVO starMentionVO = adapter.getItemsByPosition(position);
            starMentionListPresentor.executeClickEvent(starMentionVO,
                    StarMentionListFragment.this);
            if (!TextUtils.equals(listType, StarMentionListActivity.TYPE_MENTION_LIST)) {
                if (starMentionVO.getContentType() == StarMentionVO.Type.Text.getValue()) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Stars, AnalyticsValue.Action.ChooseMsg);
                } else if (starMentionVO.getContentType() == StarMentionVO.Type.File.getValue()) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Stars, AnalyticsValue.Action.ChooseFile);
                } else if (starMentionVO.getContentType() == StarMentionVO.Type.Comment.getValue()) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Stars, AnalyticsValue.Action.ChooseComment);
                }
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Mentions, AnalyticsValue.Action.ChooseMention);
            }
        };
        starMentionListAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void setOnItemLongClickListener() {
        StarMentionListAdapter.OnItemLongClickListener onItemLongClickListener
                = (adapter, position) -> starMentionListPresentor.executeLongClickEvent(
                adapter.getItemsByPosition(position), position);
        starMentionListAdapter.setOnItemLongClickListener(onItemLongClickListener);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void onShowMoreProgressBar() {
        moreLoadingProgressBar.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void onDismissMoreProgressBar() {
        moreLoadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSetNoMoreState() {
        starMentionListAdapter.setNoMoreLoad();
    }

    @Override
    public void onSetReadyMoreState() {
        starMentionListAdapter.setReadyMore();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(RefreshOldStarMentionedEvent event) {
        if (event.getType().equals(listType)) {
            loadStarMentionList();
        }
    }

    public void onEventMainThread(DeleteFileEvent event) {
        long messageId = event.getId();
        starMentionListAdapter.deleteMessage(messageId);
        starMentionListAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "message_delete")) {
            int messageId = event.getMessageId();
            starMentionListAdapter.deleteMessage(messageId);
            starMentionListAdapter.notifyDataSetChanged();
        }
    }

    public void onEventMainThread(FileCommentRefreshEvent event) {
        if (TextUtils.equals(event.getEventType(), "file_comment_deleted")) {
            starMentionListAdapter.deleteMessage(event.getCommentId());
            starMentionListAdapter.notifyDataSetChanged();
        }
    }

    public void onEvent(TopicDeleteEvent event) {

        if (TextUtils.equals(listType, StarMentionListActivity.TYPE_MENTION_LIST)) {
            starMentionListPresentor.onTopicDeleteEvent(event.getTeamId(), event.getId());
        }
    }

    @OnActivityResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH)
    void onFileDetailResult(Intent data) {
        if (data != null && data.getBooleanExtra(MessageListV2Fragment.EXTRA_FILE_DELETE, false)) {
            int fileId = data.getIntExtra(MessageListV2Fragment.EXTRA_FILE_ID, -1);
            if (fileId != -1) {
                if (starMentionListAdapter.deleteMessage(fileId)) {
                    starMentionListAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    public void onEventMainThread(SocketMessageStarEvent event) {

        if (TextUtils.equals(StarMentionListActivity.TYPE_MENTION_LIST, listType)) {
            return;
        }

        if (event.isStarred()) {
            // 현재 검색된 정보 + 1 로 새로 요청
            int requestCount = starMentionListAdapter.getItemCount() + 1;
            starMentionListAdapter.removeStarMentionListAll();
            starMentionListPresentor.reloadStartList(listType, requestCount);
        } else {
            // 현재 정보 중 있으면 삭제
            for (int idx = 0, size = starMentionListAdapter.getItemCount() - 1; idx <= size; ++idx) {
                if (event.getMessageId() == starMentionListAdapter.getItem(idx).getMessageId()) {
                    starMentionListAdapter.remove(idx);
                    break;
                }
            }
            starMentionListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onShowDialog(long teamId, long messageId, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setMessage(R.string.jandi_starred_unstar_from_item);
        LogUtil.e("messageId", messageId + "");

        builder.setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
            LogUtil.e("messageId", messageId + "");
            starMentionListPresentor.unregistStarredMessage(teamId, messageId, position);
        });

        builder.setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
            unstarredDialog.dismiss();
        });

        unstarredDialog = builder.create();

        unstarredDialog.show();

    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void onRemoveItem(int position) {
        starMentionListAdapter.removeStarMentionListAt(position);
    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), (dialog, which) -> getActivity().finish());
    }

    @UiThread
    @Override
    public void deleteItemOfTopic(long topicId) {
        int itemCount = starMentionListAdapter.getItemCount();
        for (int idx = itemCount - 1; idx >= 0; --idx) {
            if (starMentionListAdapter.getItem(idx).getRoomId() == topicId) {
                starMentionListAdapter.remove(idx);
            }
        }

        starMentionListAdapter.notifyDataSetChanged();
    }

}
