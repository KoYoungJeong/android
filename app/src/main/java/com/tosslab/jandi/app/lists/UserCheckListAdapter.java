package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 15..
 */
public class UserCheckListAdapter extends BaseAdapter {
    private List<CdpItem> listSelectCdp;
    private LayoutInflater layoutInflater;
    private Context context;

    public UserCheckListAdapter(Context context, List<CdpItem> listSelectCdp) {
        this.listSelectCdp = listSelectCdp;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listSelectCdp.size();
    }

    @Override
    public Object getItem(int i) {
        return listSelectCdp.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_select_cdp, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_select_cdp_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_select_cdp_icon);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_select_cdp);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    CdpItem item = (CdpItem)compoundButton.getTag();
                    item.isSelected = b;
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CdpItem item = (CdpItem)getItem(i);
        if (item.type == JandiConstants.TYPE_DIRECT_MESSAGE) {
            // 프로필 사진
            Picasso.with(this.context).load(item.getProfileUrl()).fit().into(holder.imageView);
        }
        holder.textView.setText(item.name);
        holder.checkBox.setTag(item);

        return convertView;
    }

    public List<Integer> getSelectedCdpIds() {
        ArrayList<Integer> selectedCdp = new ArrayList<Integer>();
        for (CdpItem selectedItem : listSelectCdp) {
            if (selectedItem.isSelected) {
                selectedCdp.add(selectedItem.id);
            }
        }
        return selectedCdp;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
        CheckBox checkBox;
    }
}
