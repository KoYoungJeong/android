package com.tosslab.jandi.app.lists.entities;

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
import java.util.List;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
@Deprecated
public class EntityExpandableListAdapter extends BaseExpandableListAdapter {
    public static final int TYPE_PUBLIC_ENTITY_LIST = 0;
    public static final int TYPE_PRIVATE_ENTITY_LIST = 1;

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<Integer> mEntityTitles;
    private final List<List<FormattedEntity>> mFormattedEntities;

    public EntityExpandableListAdapter(Context c, int type) {
        super();
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
     *
     * @param first joined public topic || private topic
     * @param last  unjoined public topic || DM
     */
    public void retrieveChildList(List<FormattedEntity> first,
                                  List<FormattedEntity> last) {
        mFormattedEntities.clear();
        if (first != null)
            mFormattedEntities.add(first);
        if (last != null)
            mFormattedEntities.add(last);
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
            viewHolder = (TitleViewHolder) convertView.getTag();
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
            viewHolder.textViewName
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.imageViewIcon
                    = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.imageViewFavorite
                    = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
            viewHolder.textViewAdditional
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);

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
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
        public View viewMaskUnjoined;

        private void init() {
            imageViewFavorite.setVisibility(View.INVISIBLE);
            textViewBadgeCount.setVisibility(View.INVISIBLE);
            viewMaskUnjoined.setVisibility(View.INVISIBLE);
            imageViewIcon.setImageResource(R.drawable.jandi_icon_topic);
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
                if (formattedEntity.isStarred) {
                    textViewBadgeCount.setBackgroundResource(R.drawable.jandi_badge_starred);
                } else {
                    textViewBadgeCount.setBackgroundResource(R.drawable.jandi_badge_common);
                }
                textViewBadgeCount.setText(formattedEntity.alarmCount + "");
            }
            // 아이콘
            if (formattedEntity.isPublicTopic()) {
                if (!formattedEntity.isJoined) {
                    viewMaskUnjoined.setVisibility(View.VISIBLE);
                    textViewBadgeCount.setVisibility(View.INVISIBLE);
                }
            } else if (formattedEntity.isPrivateGroup()) {
                imageViewIcon.setImageResource(R.drawable.jandi_icon_chat);
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
