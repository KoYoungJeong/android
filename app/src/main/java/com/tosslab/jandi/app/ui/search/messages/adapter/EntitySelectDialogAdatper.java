package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class EntitySelectDialogAdatper extends BaseAdapter {

    private final Context context;
    private List<SimpleEntityInfo> entityInfoList;

    public EntitySelectDialogAdatper(Context context) {
        this.context = context;
        entityInfoList = new ArrayList<SimpleEntityInfo>();
    }

    @Override
    public int getCount() {
        return entityInfoList.size();
    }

    @Override
    public SimpleEntityInfo getItem(int position) {
        return entityInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_cdp, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_select_cdp_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SimpleEntityInfo entity = getItem(position);

        // dummy entity는 이름이 없다. 지정된 string resource id 만 가져옴.
        if (position == 0) {
            holder.textView.setText(R.string.jandi_file_category_everywhere);
        } else {
            holder.textView.setText(entity.getName());
        }

        // user 는 개별 프로필 사진이 존재하기에 별도로 가져온다.
        if (entity.getType() == JandiConstants.TYPE_DIRECT_MESSAGE) {
            // 프로필 사진
            Ion.with(holder.imageView)
                    .placeholder(R.drawable.profile_img_comment)
                    .error(R.drawable.profile_img_comment)
                    .transform(new IonCircleTransform())
                    .load(entity.getPhoto());
        } else if (entity.getType() == JandiConstants.TYPE_PUBLIC_TOPIC) {
            holder.imageView.setImageResource(R.drawable.topiclist_icon_topic);
        } else if (entity.getType() == JandiConstants.TYPE_PRIVATE_TOPIC) {
            holder.imageView.setImageResource(R.drawable.topiclist_icon_topic_private);
        } else {
            holder.imageView.setImageResource(R.drawable.topiclist_icon_topic);
        }
        return convertView;
    }

    public void add(SimpleEntityInfo simpleEntityInfo) {
        entityInfoList.add(simpleEntityInfo);
    }

    public void addAll(List<SimpleEntityInfo> simpleEntityInfos) {
        entityInfoList.addAll(simpleEntityInfos);
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public static class SimpleEntityInfo {
        private final int type;
        private final String name;
        private final int id;
        private final String photo;

        public SimpleEntityInfo(int type, String name, int id, String photo) {
            this.type = type;
            this.name = name;
            this.id = id;
            this.photo = photo;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getPhoto() {
            return photo;
        }
    }
}
