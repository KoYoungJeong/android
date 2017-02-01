package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderListDataProvider {

    private static final String TAG = "TopicFolderListDataProv";
    private List<Pair<TopicFolderData, List<TopicItemData>>> datas;

    public TopicFolderListDataProvider(List<Pair<TopicFolderData, List<TopicItemData>>> datas) {
        this.datas = datas;
    }

    public int getGroupCount() {
        return datas.size();
    }

    public int getChildCount(int groupPosition) {
        if (groupPosition < 0 || getGroupCount() <= groupPosition) {
            return 0;
        }
        Pair<TopicFolderData, List<TopicItemData>> topicFolderDataListPair = datas.get(groupPosition);
        return topicFolderDataListPair.second != null ? topicFolderDataListPair.second.size() : 0;
    }

    @Nullable
    public TopicFolderData getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            LogUtil.e(TAG, new IndexOutOfBoundsException("groupPosition = " + groupPosition));
            return null;
        }
        return datas.get(groupPosition).first;
    }

    @Nullable
    public TopicItemData getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            LogUtil.e(TAG, new IndexOutOfBoundsException("groupPosition = " + groupPosition));
            return null;
        }

        final List<TopicItemData> children = datas.get(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            LogUtil.e(TAG, new IndexOutOfBoundsException("childPosition = " + childPosition));
            return null;
        }

        return children.get(childPosition);
    }
}
