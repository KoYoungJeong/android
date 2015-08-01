package com.tosslab.jandi.app.ui.starmention.views;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.adapter.StarMentionListAdapter;
import com.tosslab.jandi.app.ui.starmention.presentor.StarMentionListPresentor;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

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

    StarMentionListAdapter starMentionListAdapter;

    @Bean
    StarMentionListPresentor starMentionListPresentor;

    @AfterViews
    void initFragment() {
        starMentionListPresentor.setView(this);
        starMentionList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        starMentionList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        starMentionListAdapter = new StarMentionListAdapter();
        starMentionList.setAdapter(starMentionListAdapter);
        if (listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            starMentionListPresentor.addMentionMessagesToList(StarMentionListActivity.TYPE_MENTION_LIST);
        } else if (listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
            starMentionListPresentor.addMentionMessagesToList(StarMentionListActivity.TYPE_STAR_ALL);
        } else if (listType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
            starMentionListPresentor.addMentionMessagesToList(StarMentionListActivity.TYPE_STAR_FILES);
        }
    }

    private void notifyViewStatus() {
        if (starMentionListPresentor.getTotalCount() == 0) {
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
    public void onSetAndShowList(List<StarMentionVO> starMentionMessageList) {
        starMentionListAdapter.setStarMentionList(starMentionMessageList);
        starMentionListAdapter.notifyDataSetChanged();
        notifyViewStatus();
    }

}
