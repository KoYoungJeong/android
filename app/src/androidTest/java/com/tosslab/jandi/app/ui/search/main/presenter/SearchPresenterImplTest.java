package com.tosslab.jandi.app.ui.search.main.presenter;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 11. 26..
 */
@RunWith(AndroidJUnit4.class)
public class SearchPresenterImplTest {

    SearchPresenterImpl presenter;
    @Rule
    public ActivityTestRule<SearchActivity_> rule = new ActivityTestRule<>(SearchActivity_.class);
    private SearchActivity activity;

    @Before
    public void setup() throws Exception {
        activity = rule.getActivity();

        presenter = SearchPresenterImpl_.getInstance_(JandiApplication.getContext());
        presenter.setView(activity);
    }

    @Test
    public void testOnSearchText() throws Throwable {
        // Given
        String query = "가";
        presenter.searchModel.upsertQuery(0, query);
        // When
        presenter.onSearchText("가");

        // Then
        assertTrue(activity.searchQueryAdapter.getCount() > 0);
    }

    @Test
    public void testOnSearchTextChange() throws Throwable {
        String query = "가나다";
        presenter.searchModel.upsertQuery(0, query);

        rule.runOnUiThread(() -> {
            // When
            presenter.onSearchText("가");

            // Then
            assertTrue(activity.searchQueryAdapter.getCount() > 0);
        });
    }
}