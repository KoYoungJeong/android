package com.tosslab.jandi.app.ui.profile.email.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;

import java.util.ArrayList;
import java.util.List;

public class EmailChooseAdapter extends BaseAdapter implements EmailChooseAdapterDataModel,
        EmailChooseAdapterViewModel {

    private Context context;
    private List<AccountEmail> accountEmails;

    public EmailChooseAdapter(Context context) {
        this.context = context;
        accountEmails = new ArrayList<AccountEmail>();
    }

    @Override
    public int getCount() {
        return accountEmails.size();
    }

    @Override
    public AccountEmail getItem(int position) {
        return accountEmails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_email_choose, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.img_email_choose_icon);
        TextView nameView = (TextView) convertView.findViewById(R.id.txt_email_choose_name);

        AccountEmail item = getItem(position);


        if (!(item instanceof AccountEmail.DummyEmail)) {
            imageView.setImageResource(R.drawable.btn_radio_selector);
            nameView.setText(item.getEmail());
            nameView.setTextColor(context.getResources().getColor((R.color.jandi_email_selector_item_text_color)));
            imageView.setSelected(item.isSelected());
            nameView.setSelected(item.isSelected());
            nameView.setEnabled(item.isConfirmed());
        } else {
            imageView.setImageResource(R.drawable.icon_add_folder);
            nameView.setTextColor(context.getResources().getColor(R.color.jandi_accent_color));
            nameView.setText(R.string.jandi_add_an_email_address);
            nameView.setEnabled(false);
        }

        return convertView;
    }

    @Override
    public void setAccountEmails(List<AccountEmail> accountEmails) {
        this.accountEmails.clear();
        this.accountEmails.addAll(accountEmails);
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }
}
