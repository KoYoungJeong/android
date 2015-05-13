package com.tosslab.jandi.app.ui.maintab.topic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class TopicListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private Map<Type, List<FormattedEntity>> items;

    public TopicListAdapter(Context context) {
        this.context = context;
        items = new HashMap<Type, List<FormattedEntity>>();
        for (Type type : Type.values()) {
            items.put(type, new ArrayList<FormattedEntity>());
        }
    }

    public TopicListAdapter joinEntities(List<FormattedEntity> entities) {

        items.put(Type.JOINED, entities);

        return this;
    }

    public List<FormattedEntity> getJoinEntities() {
        return items.get(Type.JOINED);
    }

    public TopicListAdapter unjoinEntities(List<FormattedEntity> entities) {
        items.put(Type.UNJOINED, entities);
        return this;
    }

    @Override
    public int getGroupCount() {

        if (items.get(Type.UNJOINED).isEmpty()) {
            return items.size() - 1;
        }

        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return items.get(Type.values()[groupPosition]).size();
    }

    @Override
    public Type getGroup(int groupPosition) {
        return Type.values()[groupPosition];
    }

    @Override
    public FormattedEntity getChild(int groupPosition, int childPosition) {
        return items.get(Type.values()[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final TitleViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_title, parent, false);
            viewHolder = new TitleViewHolder();

            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.txt_entity_list_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TitleViewHolder) convertView.getTag();
        }

        Type group = getGroup(groupPosition);
        switch (group) {
            case JOINED:
                viewHolder.textViewTitle.setText(R.string.jandi_entity_joined_topic);
                break;
            case UNJOINED:
                viewHolder.textViewTitle.setText(R.string.jandi_entity_unjoined_topic);
                break;
        }

        // 그룹 뷰는 항상 펼쳐져있다.
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final FormattedViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new FormattedViewHolder();
            viewHolder.context = context;
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body, parent, false);
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
            viewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);
            viewHolder.disableLineThrouthView = convertView.findViewById(R.id.img_entity_listitem_line_through);
            viewHolder.disableWarningView = convertView.findViewById(R.id.img_entity_listitem_warning);
            viewHolder.disableCoverView = convertView.findViewById(R.id.view_entity_listitem_warning);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FormattedViewHolder) convertView.getTag();

        }

        FormattedEntity formattedEntity = getChild(groupPosition, childPosition);
        viewHolder.draw(formattedEntity);
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private enum Type {
        JOINED, UNJOINED
    }

    static class TitleViewHolder {
        public TextView textViewTitle;
    }

    static class FormattedViewHolder {
        public Context context;
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
        public View disableLineThrouthView;
        public View disableWarningView;
        public View disableCoverView;

        private void init() {
            imageViewFavorite.setVisibility(View.INVISIBLE);
            textViewBadgeCount.setVisibility(View.GONE);
            disableCoverView.setVisibility(View.INVISIBLE);
            imageViewIcon.setImageResource(R.drawable.jandi_topic_icon);
        }

        public void draw(FormattedEntity formattedEntity) {
            if (formattedEntity == null) {
                return;
            }

            init();

            // 즐겨 찾기
            if (formattedEntity.isStarred) {
                imageViewFavorite.setVisibility(View.VISIBLE);
            }

            // 뱃지 카운트
            if (formattedEntity.alarmCount > 0) {
                textViewBadgeCount.setVisibility(View.VISIBLE);
                textViewBadgeCount.setBackgroundResource(R.drawable.jandi_badge_starred);
                textViewBadgeCount.setText(formattedEntity.alarmCount + "");
            }
            // 아이콘

            disableLineThrouthView.setVisibility(View.GONE);
            disableWarningView.setVisibility(View.GONE);


            if (formattedEntity.isPublicTopic()) {
                if (formattedEntity.isJoined) {
                    disableCoverView.setVisibility(View.GONE);
                } else {
                    disableCoverView.setVisibility(View.VISIBLE);
                    textViewBadgeCount.setVisibility(View.GONE);
                }
            } else if (formattedEntity.isPrivateGroup()) {
                disableCoverView.setVisibility(View.GONE);
                imageViewIcon.setImageResource(R.drawable.jandi_private_topic_icon);
            } else if (formattedEntity.isUser()) {
                Ion.with(imageViewIcon)
                        .placeholder(R.drawable.jandi_profile)
                        .error(R.drawable.jandi_profile)
                        .transform(new IonCircleTransform())
                        .load(formattedEntity.getUserSmallProfileUrl());
            }
            // 이름
            textViewName.setText(formattedEntity.getName());
            // 추가 정보
            if (formattedEntity.isUser()) {
                textViewAdditional.setText(formattedEntity.getUserEmail());
            } else {
                textViewAdditional.setText(formattedEntity.getMemberCount() + " Users");
            }
        }
    }
}
