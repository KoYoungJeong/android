package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.CircleTransform;

import java.util.ArrayList;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
public class EntityExpandableListAdapter extends BaseExpandableListAdapter {
    public static int TYPE_PUBLIC   = 0;
    public static int TYPE_PRIVATE  = 1;

    private int mType;
    private Context mContext;
    private ArrayList<String> mEntityTitles = null;
    private ArrayList<ArrayList<FormattedEntity>> mFormattedEntities = null;
    private LayoutInflater inflater = null;

    public EntityExpandableListAdapter(Context c, ArrayList<ArrayList<FormattedEntity>> formattedEntities, int type) {
        mContext = c;
        mFormattedEntities = formattedEntities;
        mType = type;
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
    public String getGroup(int groupPosition) {
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
            convertView = inflater.inflate(R.layout.item_entity_title, parent, false);
            viewHolder = new TitleViewHolder();

            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.txt_entity_list_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TitleViewHolder)convertView.getTag();
        }

//        // 그룹을 펼칠때와 닫을때 아이콘을 변경해 준다.
//        if(isExpanded){
//            viewHolder.iv_image.setBackgroundColor(Color.GREEN);
//        }else{
//            viewHolder.iv_image.setBackgroundColor(Color.WHITE);
//        }

        viewHolder.textViewTitle.setText(getGroup(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup paren) {
        final FormattedViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new FormattedViewHolder();
            convertView = inflater.inflate(R.layout.item_entity_body, null);
            viewHolder.imageViewIcon
                    = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.textViewName
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.textViewAdditional
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount
                    = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FormattedViewHolder) convertView.getTag();

        }
        FormattedEntity formattedEntity = getChild(groupPosition, childPosition);
        drawChildView(formattedEntity, viewHolder);
        return null;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    private void drawChildView(FormattedEntity formattedEntity, FormattedViewHolder viewHolder) {
        if (formattedEntity == null) {
            return;
        }

        // 아이콘
        if (formattedEntity.isUser()) {
            Picasso.with(mContext)
                    .load(formattedEntity.getUserSmallProfileUrl())
                    .placeholder(R.drawable.jandi_profile)
                    .transform(new CircleTransform())
                    .into(viewHolder.imageViewIcon);
        }
        if (formattedEntity.isChannel()) {
            // 채널 아이콘의 색상이 자신의 ID에 따라 자동으로 변하도록...
            if (formattedEntity.isJoined) {
                viewHolder.imageViewIcon.setColorFilter(formattedEntity.getMyColor(),
                        android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                viewHolder.imageViewIcon.clearColorFilter();
//                viewBlindForUnjoined.setVisibility(VISIBLE);
            }
        }
        if (formattedEntity.isPrivateGroup()) {
            viewHolder.imageViewIcon.setImageResource(R.drawable.jandi_icon_privategroup);
        }
        // 이름
        viewHolder.textViewName.setText(formattedEntity.getName());
        // 추가 정보
        if (formattedEntity.isUser()) {
            viewHolder.textViewAdditional.setText(formattedEntity.getUserEmail());
        } else {
            viewHolder.textViewAdditional.setText(formattedEntity.getMemberCount() + " Users");
        }
        // 뱃지 카운트
        if (formattedEntity.alarmCount > 0) {
            viewHolder.textViewBadgeCount.setVisibility(View.VISIBLE);
            viewHolder.textViewBadgeCount.setText(formattedEntity.alarmCount + "");
        }
    }

    class TitleViewHolder {
        public TextView textViewTitle;
    }

    class FormattedViewHolder {
        public ImageView imageViewIcon;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
    }
}
