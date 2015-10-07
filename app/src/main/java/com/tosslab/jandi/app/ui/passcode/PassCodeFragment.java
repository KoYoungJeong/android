package com.tosslab.jandi.app.ui.passcode;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.passcode.adapter.PassCodeAdapter;
import com.tosslab.jandi.app.views.decoration.RecyclerViewDivider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 10. 7..
 */
@EFragment(R.layout.fragment_passcode)
public class PassCodeFragment extends Fragment {

    @ViewById(R.id.gv_passcode)
    RecyclerView gvPassCode;
    private PassCodeAdapter adapter;

    @AfterViews
    void initViews() {
        Context baseContext = getActivity().getBaseContext();
        adapter = new PassCodeAdapter(baseContext);
        GridLayoutManager layoutManager = new GridLayoutManager(baseContext, 3);
        gvPassCode.setLayoutManager(layoutManager);
        gvPassCode.setAdapter(adapter);
        float dividerSize = baseContext.getResources().getDisplayMetrics().density * 10;
        gvPassCode.addItemDecoration(new RecyclerViewDivider(dividerSize));
    }

}
