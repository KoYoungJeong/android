package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.CircleTransform;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class UserEntitySimpleListAdapter extends BaseAdapter {
    private List<ResLeftSideMenu.User> mUserList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public UserEntitySimpleListAdapter(Context context, List<ResLeftSideMenu.User> userList) {
        this.mUserList = userList;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int i) {
        return mUserList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_select_cdp, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_select_cdp_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ResLeftSideMenu.User user = (ResLeftSideMenu.User)getItem(i);

        // 프로필 사진
        Picasso.with(this.mContext)
                .load(JandiConstants.SERVICE_ROOT_URL + user.u_photoUrl)
                .placeholder(R.drawable.jandi_icon_directmsg)
                .transform(new CircleTransform())
                .into(holder.imageView);

        holder.textView.setText(user.name);

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
