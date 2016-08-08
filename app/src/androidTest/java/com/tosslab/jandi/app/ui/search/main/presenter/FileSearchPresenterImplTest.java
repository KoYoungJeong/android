package com.tosslab.jandi.app.ui.search.main.presenter;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.search.main.model.FileSearchModel;
import com.tosslab.jandi.app.ui.search.main.view.FileSearchActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 11. 26..
 */
@RunWith(AndroidJUnit4.class)
public class FileSearchPresenterImplTest {

    @Rule
    public ActivityTestRule<FileSearchActivity> rule = new ActivityTestRule<>(FileSearchActivity.class);
    FileSearchPresenterImpl presenter;
    private FileSearchActivity activity;

    @Before
    public void setup() throws Exception {
        activity = rule.getActivity();

        presenter = new FileSearchPresenterImpl(activity, new FileSearchModel());
    }

    @Test
    public void testOnSearchText() throws Throwable {
        // Given
        String query = "가";
        presenter.fileSearchModel.upsertQuery(query);
        // When
        presenter.onSearchText("가");

        // Then
        assertTrue(activity.searchQueryAdapter.getCount() > 0);
    }

    @Test
    public void testOnSearchTextChange() throws Throwable {
        String query = "가나다";
        presenter.fileSearchModel.upsertQuery(query);

        rule.runOnUiThread(() -> {
            // When
            presenter.onSearchText("가");

            // Then
            assertTrue(activity.searchQueryAdapter.getCount() > 0);
        });
    }
}