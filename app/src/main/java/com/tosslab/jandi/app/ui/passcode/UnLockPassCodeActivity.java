package com.tosslab.jandi.app.ui.passcode;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.KeyEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.passcode.adapter.PassCodeAdapter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.views.decoration.RecyclerViewDivider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 10. 7..
 */
@EActivity(R.layout.activity_unlock_passcode)
public class UnLockPassCodeActivity extends BaseAppCompatActivity {

    public static final String KEY_CALLING_COMPONENT_NAME = "calling_component_name";


    @ViewById(R.id.gv_passcode)
    RecyclerView gvPassCode;
    private PassCodeAdapter adapter;

    @AfterViews
    void initViews() {
        Context baseContext = getBaseContext();
        adapter = new PassCodeAdapter(baseContext);
        GridLayoutManager layoutManager = new GridLayoutManager(baseContext, 3);
        gvPassCode.setLayoutManager(layoutManager);
        gvPassCode.setAdapter(adapter);
        float dividerSize = baseContext.getResources().getDisplayMetrics().density * 10;
        gvPassCode.addItemDecoration(new RecyclerViewDivider(dividerSize));
    }

    private void finishWithSuccess() {
        UnLockPassCodeManager.getInstance().setUnLocked(true);

        Intent intent = getIntent();
        Parcelable parcelableExtra = intent.getParcelableExtra(KEY_CALLING_COMPONENT_NAME);
        if (parcelableExtra != null) {
            intent.setComponent((ComponentName) parcelableExtra);
            startActivity(intent);
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
