package com.tosslab.jandi.app.ui.invites.email;

import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.email.adapter.InviteEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.email.model.InviteEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteEmailPresenter;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteEmailPresenterImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EActivity(R.layout.activity_invite)
public class InviteEmailActivity extends BaseAnalyticsActivity
        implements InviteEmailPresenter.View {

    @Bean
    InviteEmailModel inviteModel;

    @Bean(InviteEmailPresenterImpl.class)
    InviteEmailPresenter presenter;

    @ViewById(R.id.btn_invite_send)
    Button inviteButton;

    @ViewById(R.id.edit_invite_email)
    EditText emailTextView;

    @ViewById(R.id.lv_invite)
    ListView inviteListView;

    @ViewById(R.id.invite_footer_text)
    TextView manyPeopleInviteText;

    @ViewById(R.id.layout_invite_success_items)
    View successListLayout;

    @ViewById(R.id.invite_succes_text_display)
    TextView displaySendEmailSuccesText;

    private InviteEmailListAdapter adapter;

    @AfterViews
    void initView() {
        setUpActionbar();
        adapter = new InviteEmailListAdapter(this);
        inviteListView.setAdapter(adapter);
        registerItemListViewFoldingObserver();
        setManyPeopleInviteText();
    }

    private void setUpActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        EntityManager entityManager = EntityManager.getInstance();
        trackGaInviteMember(entityManager.getDistictId());
        ActivityHelper.setOrientation(this);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        finish();
    }

    @AfterTextChange(R.id.edit_invite_email)
    void emailTextChanged(Editable text) {
        String email = text.toString();
        presenter.onEmailTextChanged(email);
    }

    @Click(R.id.btn_invite_send)
    void inviteListAddClick() {
        presenter.onInviteListAddClick(emailTextView.getText().toString());
    }

    @Override
    public void setEmailTextView(String email) {
        emailTextView.setText(email);
    }

    @Override
    public void clearEmailTextView() {
        emailTextView.setText("");
    }

    @Override
    public void setEnableAddButton(Boolean enable) {
        inviteButton.setEnabled(enable);
    }

    @Override
    @UiThread
    public void showToast(String message) {
        ColoredToast.show(getApplicationContext(), message);
    }

    @Override
    public InviteEmailListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void addEmailToList(EmailVO emailVO) {
        adapter.add(0, emailVO);
        adapter.notifyDataSetChanged();
    }

    @Override
    @UiThread
    public void removeEmailFromList(EmailVO emailVO) {
        for (int idx = adapter.getCount() - 1; idx >= 0; --idx) {
            EmailVO item = adapter.getItem(idx);
            if (item == emailVO) {
                adapter.remove(idx);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    @UiThread
    public void updateSuccessInvite(EmailVO emailVO, int isSuccess) {
        for (int idx = adapter.getCount() - 1; idx >= 0; --idx) {
            EmailVO item = adapter.getItem(idx);
            if (item == emailVO) {
                item.setSuccess(isSuccess);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @UiThread
    public void addSendEmailSuccessText() {
        displaySendEmailSuccesText.setVisibility(View.VISIBLE);
    }

    public void moveToSelection(int position) {
        inviteListView.smoothScrollToPosition(position);
    }

    private void registerItemListViewFoldingObserver() {
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (adapter.getCount() <= 0) {
                    successListLayout.setVisibility(View.GONE);
                } else {
                    successListLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setManyPeopleInviteText() {
        String inviteText = getApplicationContext().getString(R.string.jandi_invite_many_people_explain);
        int index = inviteText.indexOf("support");
        SpannableStringBuilder builder = new SpannableStringBuilder(inviteText);
        URLSpan urlSpan = new URLSpan("mailto:" + inviteText.substring(index).toString()) {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getApplicationContext().getResources().getColor(R.color.jandi_accent_color));
            }
        };
        builder.setSpan(urlSpan, index, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        manyPeopleInviteText.setText(builder);
        manyPeopleInviteText.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
