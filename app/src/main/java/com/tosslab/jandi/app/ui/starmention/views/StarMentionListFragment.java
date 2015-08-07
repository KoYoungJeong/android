package com.tosslab.jandi.app.ui.starmention.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldStarMentionedEvent;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.adapter.StarMentionListAdapter;
import com.tosslab.jandi.app.ui.starmention.presentor.StarMentionListPresentor;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

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
    RecyclerView starMentionList;

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
        starMentionListPresentor.setView(this);
        starMentionList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        starMentionList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        starMentionListAdapter = new StarMentionListAdapter();
        starMentionListAdapter.setListType(listType);
        starMentionList.setAdapter(starMentionListAdapter);
        loadStarMentionList();
        setOnItemClickListener();
        if (!listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            setOnItemLongClickListener();
        }
    }

    private void loadStarMentionList() {
        try {
            starMentionListPresentor.addMentionMessagesToList(listType);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyViewStatus() {
        if (starMentionListPresentor.isEmpty()) {
            starMentionList.setVisibility(View.GONE);
            llEmptyListStarMention.setVisibility(View.VISIBLE);
            if (listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
                tvNoContent.setText(R.string.jandi_mention_no_mentions);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
                tvNoContent.setText(R.string.jandi_starred_no_all);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
                tvNoContent.setText(R.string.jandi_starred_no_file);
            }
        } else {
            starMentionList.setVisibility(View.VISIBLE);
            llEmptyListStarMention.setVisibility(View.GONE);
        }
    }

    @Override
    @UiThread
    public void onAddAndShowList(List<StarMentionVO> starMentionMessageList) {
        starMentionListAdapter.addStarMentionList(starMentionMessageList);
        notifyViewStatus();
    }

    public void setOnItemClickListener() {
        StarMentionListAdapter.OnItemClickListener onItemClickListener = (adapter, position) -> {
            starMentionListPresentor.executeClickEvent(adapter.getItemsByPosition(position), getActivity());

        };
        starMentionListAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void setOnItemLongClickListener() {
        StarMentionListAdapter.OnItemLongClickListener onItemLongClickListener = (adapter, position) -> {
            return starMentionListPresentor.executeLongClickEvent(adapter.getItemsByPosition(position), position);
        };
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

        if (event.getType().equals(StarMentionListActivity.TYPE_STAR_FILES)
                && listType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
            loadStarMentionList();
        } else if (event.getType().equals(StarMentionListActivity.TYPE_STAR_ALL)
                && listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
            loadStarMentionList();
        } else if (event.getType().equals(StarMentionListActivity.TYPE_MENTION_LIST)
                && listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
            loadStarMentionList();
        }

    }

    public void onEvent(MessageStarredEvent event) {
        starMentionListAdapter.removeStarMentionListAll();
        starMentionListPresentor.refreshList(listType);
    }

    @Override
    public void onShowDialog(int teamId, int messageId, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.jandi_starred_unstar_from_item);

        builder.setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
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
        ColoredToast.show(getActivity(), message);
    }

}
