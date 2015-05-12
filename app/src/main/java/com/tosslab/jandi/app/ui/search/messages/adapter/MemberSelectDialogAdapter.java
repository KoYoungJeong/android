package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class MemberSelectDialogAdapter extends BaseAdapter {

    private final Context context;
    private List<SimpleMemberInfo> memberInfos;

    public MemberSelectDialogAdapter(Context context) {
        this.context = context;
        memberInfos = new ArrayList<SimpleMemberInfo>();
    }

    @Override
    public int getCount() {
        return memberInfos.size();
    }

    @Override
    public SimpleMemberInfo getItem(int position) {
        return memberInfos.get(position);
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

        SimpleMemberInfo user = getItem(position);

        // 프로필 사진
        Ion.with(holder.imageView)
                .placeholder(R.drawable.jandi_profile_comment)
                .error(R.drawable.jandi_profile_comment)
                .transform(new IonCircleTransform())
                .load(user.getPhoto());

        holder.textView.setText(user.getName());

        return convertView;
    }

    public void addAll(List<SimpleMemberInfo> simpleMemberInfos) {
        memberInfos.addAll(simpleMemberInfos);
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public static class SimpleMemberInfo {
        private final String name;
        private final int memberId;
        private final String photo;

        public SimpleMemberInfo(int memberId, String name, String photo) {
            this.name = name;
            this.memberId = memberId;
            this.photo = photo;
        }

        public int getMemberId() {
            return memberId;
        }

        public String getName() {
            return name;
        }

        public String getPhoto() {
            return photo;
        }
    }
}
