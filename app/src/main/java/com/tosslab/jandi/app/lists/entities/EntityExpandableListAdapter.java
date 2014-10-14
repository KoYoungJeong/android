package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.CircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
public class EntityExpandableListAdapter extends BaseExpandableListAdapter {
    public static final int TYPE_PUBLIC_ENTITY_LIST     = 0;
    public static final int TYPE_PRIVATE_ENTITY_LIST    = 1;

    private int mType;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Integer> mEntityTitles = null;
    private List<List<FormattedEntity>> mFormattedEntities = null;

    public EntityExpandableListAdapter(Context c, int type) {
        super();
        mType = type;
        mContext = c;
        mInflater = LayoutInflater.from(mContext);

        mEntityTitles = new ArrayList<Integer>();
        if (type == TYPE_PUBLIC_ENTITY_LIST) {
            mEntityTitles.add(R.string.jandi_entity_joined_topic);
            mEntityTitles.add(R.string.jandi_entity_unjoined_topic);
        } else {
            mEntityTitles.add(R.string.jandi_tab_private_group);
            mEntityTitles.add(R.string.jandi_tab_direct_message);
        }
        mFormattedEntities = new ArrayList<List<FormattedEntity>>();
        mFormattedEntities.add(new ArrayList<FormattedEntity>());
        mFormattedEntities.add(new ArrayList<FormattedEntity>());

    }

    /**
     * Topic을 구분하여 삽입
     * @param joined
     * @param unjoined
     */
    public void retrievePublicList(List<FormattedEntity> joined,
                                   List<FormattedEntity> unjoined) {
        mFormattedEntities.clear();
        if (joined != null)
            mFormattedEntities.add(joined);
        if (unjoined != null)
            mFormattedEntities.add(unjoined);
        notifyDataSetChanged();
    }

    /**
     * 그룹과 1:1을 삽입
     * @param group
     * @param directMessage
     */
    public void retrievePrivateList(ArrayList<FormattedEntity> group,
                                    ArrayList<FormattedEntity> directMessage) {
        mFormattedEntities.clear();
        if (group != null)
            mFormattedEntities.add(group);
        if (directMessage != null)
            mFormattedEntities.add(directMessage);
        notifyDataSetChanged();
    }

    public void setReadMarker(int groupPosition, int childPosition) {
        FormattedEntity entity = getChild(groupPosition, childPosition);
        entity.alarmCount = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mEntityTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mFormattedEntities.get(groupPosition).size();
    }

    @Override
    public Integer getGroup(int groupPosition) {
        return mEntityTitles.get(groupPosition);
    }

    @Override
    public FormattedEntity getChild(int groupPosition, int childPosition) {
        return mFormattedEntities.get(groupPosition).get(childPosition);
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
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        final TitleViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_entity_title, parent, false);
            viewHolder = new TitleViewHolder();

            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.txt_entity_list_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TitleViewHolder)convertView.getTag();
        }

        viewHolder.textViewTitle.setText(getGroup(groupPosition));

        // 그룹 뷰는 항상 펼쳐져있다.
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup paren) {
        final FormattedViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new FormattedViewHolder();
            convertView = mInflater.inflate(R.layout.item_entity_body, null);
            viewHolder.imageViewIcon
                    = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.textViewName
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.textViewAdditional
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);
            viewHolder.viewMaskUnjoined
                    = convertView.findViewById(R.id.view_entity_unjoined);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FormattedViewHolder) convertView.getTag();

        }

        FormattedEntity formattedEntity = getChild(groupPosition, childPosition);
        viewHolder.draw(formattedEntity);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    class TitleViewHolder {
        public TextView textViewTitle;
    }

    class FormattedViewHolder {
        public ImageView imageViewIcon;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
        public View viewMaskUnjoined;

        private void init() {
            textViewBadgeCount.setVisibility(View.INVISIBLE);
            viewMaskUnjoined.setVisibility(View.INVISIBLE);
            imageViewIcon.setImageResource(R.drawable.jandi_icon_topic);
        }

        public void draw(FormattedEntity formattedEntity) {
            if (formattedEntity == null) {
                return;
            }

            init();
            // 아이콘
            if (formattedEntity.isChannel()) {
                // 채널 아이콘의 색상이 자신의 ID에 따라 자동으로 변하도록...
                if (formattedEntity.isJoined == false) {
                    viewMaskUnjoined.setVisibility(View.VISIBLE);
                }
            } else if (formattedEntity.isPrivateGroup()) {
                imageViewIcon.setImageResource(R.drawable.jandi_icon_chat);
            } else if (formattedEntity.isUser()) {
                Picasso.with(mContext)
                        .load(formattedEntity.getUserSmallProfileUrl())
                        .placeholder(R.drawable.jandi_profile)
                        .transform(new CircleTransform())
                        .into(imageViewIcon);
            }

            // 이름
            textViewName.setText(formattedEntity.getName());
            // 추가 정보
            if (formattedEntity.isUser()) {
                textViewAdditional.setText(formattedEntity.getUserEmail());
            } else {
                textViewAdditional.setText(formattedEntity.getMemberCount() + " Users");
            }
            // 뱃지 카운트
            if (formattedEntity.alarmCount > 0) {
                textViewBadgeCount.setVisibility(View.VISIBLE);
                textViewBadgeCount.setText(formattedEntity.alarmCount + "");
            }
        }
    }
}
