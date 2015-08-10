package com.tosslab.jandi.app.ui.sticker;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.views.ViewPagerIndicator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
@EBean
public class StickerViewModel {

    public static final int STICKER_GROUP_RECENT = 0;
    public static final int STICKER_GROUP_MOZZI = 1;
    @ViewById(R.id.vg_message_sticker_selector)
    ViewGroup vgStickerSelector;

    @ViewById(R.id.btn_message_sticker)
    View btnStickerShow;

    @RootContext
    Context context;
    private LinearLayout vgStickerGroups;
    private ViewPager pagerStickerItems;
    private ViewGroup vgNoItemsLayout;
    private ViewPagerIndicator viewPagerIndicator;

    private OnStickerClick onStickerClick;

    @AfterViews
    void initViews() {
        LayoutInflater.from(context).inflate(R.layout.layout_stickers_default, vgStickerSelector, true);
        vgStickerGroups = (LinearLayout) vgStickerSelector.findViewById(R.id.vg_sticker_default_groups);
        pagerStickerItems = (ViewPager) vgStickerSelector.findViewById(R.id.pager_sticker_default_items);
        vgNoItemsLayout = (ViewGroup) vgStickerSelector.findViewById(R.id.vg_sticker_default_items_no_item);
        viewPagerIndicator = (ViewPagerIndicator) vgStickerSelector.findViewById(R.id.indicator_sticker_default_items_page_indicator);

        initClicks();
    }

    private void initClicks() {
        for (int idx = 0, size = vgStickerGroups.getChildCount(); idx < size; ++idx) {
            final int finalIdx = idx;
            vgStickerGroups.getChildAt(idx).setOnClickListener(view -> {
                setupGroupState(finalIdx, vgStickerGroups);
                updateStickerItems(finalIdx, pagerStickerItems);
            });
        }
    }

    private void updateStickerItems(int groupIdx, ViewPager vgStickerItems) {
        List<ResMessages.StickerContent> stickers;
        switch (groupIdx) {
            case STICKER_GROUP_RECENT:
                stickers = StickerRepository.getRepository().getRecentStickers();
                break;
            case STICKER_GROUP_MOZZI:
                stickers = StickerRepository.getRepository().getStickers(StickerRepository
                        .DEFAULT_GROUP_ID_MOZZI);
                break;
            default:
                stickers = new ArrayList<>();
                break;
        }

        addStickerView(stickers, vgStickerItems);

    }

    private void addStickerView(List<ResMessages.StickerContent> stickers, ViewPager vgStickerItems) {
        int size = stickers.size();

        if (size <= 0) {
            vgNoItemsLayout.setVisibility(View.VISIBLE);
            viewPagerIndicator.setVisibility(View.GONE);
        } else {
            vgNoItemsLayout.setVisibility(View.GONE);
            viewPagerIndicator.setVisibility(View.VISIBLE);
        }

        StickerViewPagerAdapter adapter = new StickerViewPagerAdapter(context, stickers, onStickerClick);
        vgStickerItems.setAdapter(adapter);
        viewPagerIndicator.setCurrentPosition(0);
        viewPagerIndicator.setIndicatorCount(adapter.getCount());
        viewPagerIndicator.invalidate();

        vgStickerItems.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (viewPagerIndicator != null) {
                    viewPagerIndicator.setCurrentPosition(position);
                }
            }
        });

    }


    private void setupGroupState(int groupIdx, LinearLayout vgStickerGroups) {
        for (int idx = 0, size = vgStickerGroups.getChildCount(); idx < size; ++idx) {
            if (groupIdx != idx) {
                vgStickerGroups.getChildAt(idx).setSelected(false);
            } else {
                vgStickerGroups.getChildAt(idx).setSelected(true);
            }
        }
    }

    public void showStickerSelector(int keyboardHeight) {
        ViewGroup.LayoutParams layoutParams = vgStickerSelector.getLayoutParams();
        if (layoutParams.height != keyboardHeight) {
            layoutParams.height = keyboardHeight;
            vgStickerSelector.setLayoutParams(layoutParams);
        }

        setupGroupState(1, vgStickerGroups);
        updateStickerItems(1, pagerStickerItems);

        vgStickerSelector.setVisibility(View.VISIBLE);
        btnStickerShow.setSelected(true);
    }

    public void dismissStickerSelector() {
        vgStickerSelector.setVisibility(View.GONE);
        btnStickerShow.setSelected(false);
    }

    public void setOnStickerClick(OnStickerClick onStickerClick) {
        this.onStickerClick = onStickerClick;
    }

    public boolean isShowStickerSelector() {
        return vgStickerSelector.getVisibility() == View.VISIBLE;
    }


    public interface OnStickerClick {
        void onStickerClick(int groupId, String stickerId);
    }
}
