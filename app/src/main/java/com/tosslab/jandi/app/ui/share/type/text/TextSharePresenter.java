package com.tosslab.jandi.app.ui.share.type.text;

import android.content.Context;
import android.widget.EditText;
import android.widget.Spinner;

import com.iangclifton.android.floatlabel.FloatLabel;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.adapter.ShareEntityAdapter;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class TextSharePresenter {

    @ViewById(R.id.spinner_share_text_entity)
    Spinner entitySpinner;

    @ViewById(R.id.txt_share_text)
    FloatLabel floatLabel;

    @RootContext
    Context context;

    private String text;
    private List<EntityInfo> entityInfos;
    private ShareEntityAdapter shareEntityAdapter;

    public void setText(String text) {

        this.text = text;
        if (floatLabel != null) {
            floatLabel.getEditText().setText(text);
        }
    }

    @AfterInject
    void initObject() {
        shareEntityAdapter = new ShareEntityAdapter(context);
    }

    @AfterViews
    void initView() {
        EditText editText = floatLabel.getEditText();
        editText.setText(text);

        for (EntityInfo entityInfo : entityInfos) {
            shareEntityAdapter.add(entityInfo);
        }

        entitySpinner.setAdapter(shareEntityAdapter);

    }

    public void setEntityInfos(List<EntityInfo> entityInfos) {
        this.entityInfos = entityInfos;
    }

    public String getMessageText() {
        return floatLabel.getEditText().getText().toString();
    }

    public EntityInfo getSelectedEntityInfo() {
        return shareEntityAdapter.getItem(entitySpinner.getSelectedItemPosition());
    }
}
