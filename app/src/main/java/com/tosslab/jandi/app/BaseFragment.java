package com.tosslab.jandi.app;

import android.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public abstract class BaseFragment extends Fragment {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
