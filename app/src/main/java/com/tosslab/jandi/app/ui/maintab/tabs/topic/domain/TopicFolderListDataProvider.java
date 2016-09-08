package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

import android.support.v4.util.Pair;

import java.util.List;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderListDataProvider {

    private List<Pair<TopicFolderData, List<TopicItemData>>> datas;

    public TopicFolderListDataProvider(List<Pair<TopicFolderData, List<TopicItemData>>> datas) {
        this.datas = datas;
    }

    public int getGroupCount() {
        return datas.size();
    }

    public int getChildCount(int groupPosition) {
        return datas.get(groupPosition).second.size();
    }

    public TopicFolderData getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }
        return datas.get(groupPosition).first;
    }

    public TopicItemData getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        final List<TopicItemData> children = datas.get(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("childPosition = " + childPosition);
        }

        return children.get(childPosition);
    }
}
