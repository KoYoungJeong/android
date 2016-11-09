package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import java.util.List;

public class EntitySimpleListAdapter extends BaseAdapter {
    private final Context context;
    private List<SimpleEntity> mFormattedEntities;

    public EntitySimpleListAdapter(Context context, List<SimpleEntity> formattedEntities) {
        this.mFormattedEntities = formattedEntities;
        this.context = context;
    }

    public void setEntities(List<SimpleEntity> entities) {
        mFormattedEntities = entities;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFormattedEntities.size();
    }

    @Override
    public SimpleEntity getItem(int i) {
        return mFormattedEntities.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_cdp, viewGroup, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.tv_select_cdp);
            holder.imageView = (ImageView) convertView.findViewById(R.id.iv_select_cdp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SimpleEntity entity = getItem(i);

        // dummy entity는 이름이 없다. 지정된 string resource id 만 가져옴.
        if (entity.isDummy()) {
            holder.textView.setText(R.string.jandi_search_category_everywhere);
        } else {
            holder.textView.setText(entity.getName());
        }

        ImageView ivIcon = holder.imageView;
        ivIcon.clearColorFilter();
        // user 는 개별 프로필 사진이 존재하기에 별도로 가져온다.
        ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
        if (!TeamInfoLoader.getInstance().isJandiBot(entity.getId())) {
            layoutParams.height = layoutParams.width;
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
        }
        ivIcon.setLayoutParams(layoutParams);
        if (entity.isUser()) {
            // 프로필 사진
            ImageUtil.loadProfileImage(ivIcon,
                    entity.getPhotoUrl(), R.drawable.profile_img_comment);
        } else {
            ImageLoader.loadFromResources(ivIcon, entity.getIconImageResId());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public static class SimpleEntity {
        private long id;
        private String name;
        private String photoUrl;
        private boolean isUser;
        private boolean isDummy;
        private boolean isPublic;
        private boolean starred;

        public boolean isPublic() {
            return isPublic;
        }

        public void setPublic(boolean aPublic) {
            isPublic = aPublic;
        }

        public boolean isStarred() {
            return starred;
        }

        public void setStarred(boolean starred) {
            this.starred = starred;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public boolean isUser() {
            return isUser;
        }

        public void setUser(boolean user) {
            isUser = user;
        }

        public boolean isDummy() {
            return isDummy;
        }

        public void setDummy(boolean dummy) {
            isDummy = dummy;
        }

        public int getIconImageResId() {
            if (isPublic) {
                return starred ? R.drawable.topiclist_icon_topic_fav : R.drawable.topiclist_icon_topic;
            } else if (isDummy()) {
                return R.drawable.topiclist_icon_topic;
            } else {
                return starred
                        ? R.drawable.topiclist_icon_topic_private_fav
                        : R.drawable.topiclist_icon_topic_private;
            }
        }
    }
}
