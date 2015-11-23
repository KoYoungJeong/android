package com.tosslab.jandi.app.ui.sticker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
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
    public static final int STICKER_GROUP_DAY = 1;
    public static final int STICKER_GROUP_MOZZI = 2;

    public static final int TYPE_MESSAGE = 11;
    public static final int TYPE_TOPIC = 12;
    public static final int TYPE_FILE_DETAIL = 13;
    @ViewById(R.id.vg_message_sticker_selector)
    ViewGroup vgStickerSelector;

    View btnStickerShow;

    @RootContext
    Context context;
    private LinearLayout vgStickerGroups;
    private ViewPager pagerStickerItems;
    private ViewGroup vgNoItemsLayout;
    private ViewPagerIndicator viewPagerIndicator;

    private OnStickerClick onStickerClick;
    private OnStickerDoubleTapListener onStickerDoubleTapListener;
    private OnStickerLayoutShowListener onStickerLayoutShowListener;
    private int type;

    private Pair<Integer, String> lastClickedStickerInfo;
    private long lastClickedTime;
    private boolean isShow;
    private ImageView ivNoItems;

    @AfterViews
    void initViews() {
        LayoutInflater.from(context).inflate(R.layout.layout_stickers_default, vgStickerSelector, true);
        vgStickerGroups = (LinearLayout) vgStickerSelector.findViewById(R.id.vg_sticker_default_groups);
        pagerStickerItems = (ViewPager) vgStickerSelector.findViewById(R.id.pager_sticker_default_items);
        vgNoItemsLayout = (ViewGroup) vgStickerSelector.findViewById(R.id.vg_sticker_default_items_no_item);
        ivNoItems = (ImageView) vgStickerSelector.findViewById(R.id.iv_sticker_default_items_no_item);
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

    public void setStickerButton(View btnStickerShow) {
        this.btnStickerShow = btnStickerShow;
    }

    private void updateStickerItems(int groupIdx, ViewPager vgStickerItems) {
        List<ResMessages.StickerContent> stickers;
        StickerRepository stickerRepository = StickerRepository.getRepository();
        switch (groupIdx) {
            case STICKER_GROUP_RECENT:
                stickers = stickerRepository.getRecentStickers();
                break;
            case STICKER_GROUP_MOZZI:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_MOZZI);
                break;
            case STICKER_GROUP_DAY:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_DAY);
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

        StickerViewPagerAdapter adapter = new StickerViewPagerAdapter(context, stickers, new OnStickerClick() {
            @Override
            public void onStickerClick(int groupId, String stickerId) {
                if (onStickerClick != null) {
                    onStickerClick.onStickerClick(groupId, stickerId);
                }

                if (lastClickedStickerInfo != null) {
                    if (isSameSticker(groupId, stickerId)
                            && isDoubleTap(lastClickedTime)
                            && onStickerDoubleTapListener != null) {
                        onStickerDoubleTapListener.onStickerDoubleTap(groupId, stickerId);
                        lastClickedStickerInfo = null;
                    } else {
                        lastClickedStickerInfo = Pair.create(groupId, stickerId);
                    }
                } else {
                    lastClickedStickerInfo = Pair.create(groupId, stickerId);
                }

                lastClickedTime = System.currentTimeMillis();
            }
        });
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

    public boolean isDoubleTap(long lastClickedTime) {
        return System.currentTimeMillis() - lastClickedTime <= ViewConfiguration.getJumpTapTimeout();
    }

    public boolean isSameSticker(int groupId, String stickerId) {
        return lastClickedStickerInfo.first == groupId
                && TextUtils.equals(lastClickedStickerInfo.second, stickerId);
    }


    private void setupGroupState(int groupIdx, LinearLayout vgStickerGroups) {
        for (int idx = 0, size = vgStickerGroups.getChildCount(); idx < size; ++idx) {
            if (groupIdx != idx) {
                vgStickerGroups.getChildAt(idx).setSelected(false);
            } else {
                vgStickerGroups.getChildAt(idx).setSelected(true);
            }

            AnalyticsValue.Screen screen;
            AnalyticsValue.Action action;
            screen = getScreen();

            if (groupIdx == 0) {
                action = AnalyticsValue.Action.Sticker_RecentTab;
            } else {
                action = AnalyticsValue.Action.Sticker_StickerTab;
            }
            AnalyticsUtil.sendEvent(screen.name(), action.name() + String.valueOf(groupIdx));
        }
    }

    private AnalyticsValue.Screen getScreen() {
        switch (type) {
            case TYPE_FILE_DETAIL:
                return AnalyticsValue.Screen.FileDetail;
            case TYPE_MESSAGE:
                return AnalyticsValue.Screen.Message;
            default:
            case TYPE_TOPIC:
                return AnalyticsValue.Screen.TopicChat;

        }
    }

    /**
     * @param keyboardHeight 0 보다 커여 함
     */
    public void showStickerSelector(int keyboardHeight) {
        ViewGroup.LayoutParams layoutParams = vgStickerSelector.getLayoutParams();
        Resources resources = vgStickerSelector.getResources();
        int keyboardMaxHeight;
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int maxHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
            keyboardMaxHeight = Math.min(maxHeight, keyboardHeight);
        } else {
            keyboardMaxHeight = keyboardHeight;
        }
        if (layoutParams.height != keyboardMaxHeight) {
            layoutParams.height = keyboardMaxHeight;
            vgStickerSelector.setLayoutParams(layoutParams);
        }

        setupGroupState(1, vgStickerGroups);
        updateStickerItems(1, pagerStickerItems);

        vgStickerSelector.setVisibility(View.VISIBLE);
        if (btnStickerShow != null) {
            btnStickerShow.setSelected(true);
        }

        isShow = true;

        if (onStickerLayoutShowListener != null) {
            onStickerLayoutShowListener.onStickerLayoutShow(true);
        }

    }

    public void dismissStickerSelector() {
        vgStickerSelector.setVisibility(View.GONE);
        if (btnStickerShow != null) {
            btnStickerShow.setSelected(false);
        }

        isShow = false;

        if (onStickerLayoutShowListener != null) {
            onStickerLayoutShowListener.onStickerLayoutShow(false);
        }

    }

    public void setOnStickerClick(OnStickerClick onStickerClick) {
        this.onStickerClick = onStickerClick;
    }

    public boolean isShowStickerSelector() {
        return vgStickerSelector.getVisibility() == View.VISIBLE;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setOnStickerDoubleTapListener(OnStickerDoubleTapListener onStickerDoubleTapListener) {
        this.onStickerDoubleTapListener = onStickerDoubleTapListener;
    }

    public StickerViewModel setOnStickerLayoutShowListener(OnStickerLayoutShowListener onStickerLayoutShowListener) {
        this.onStickerLayoutShowListener = onStickerLayoutShowListener;
        return this;
    }

    public boolean isShow() {
        return isShow;
    }

    public void onConfigurationChanged() {
        if (vgStickerSelector.getVisibility() != View.VISIBLE) {
            return;
        }

        ViewGroup.LayoutParams layoutParams = vgStickerSelector.getLayoutParams();
        int height = layoutParams.height;
        Resources resources = vgStickerSelector.getResources();
        int keyboardMaxHeight;
        if (height > 0
                && resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int maxHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
            keyboardMaxHeight = Math.min(maxHeight, height);
            layoutParams.height = keyboardMaxHeight;
            ivNoItems.setVisibility(View.GONE);
        } else {
            int keyboardHeight = JandiPreference.getKeyboardHeight(vgStickerSelector.getContext());
            if (keyboardHeight > 0) {
                layoutParams.height = keyboardHeight;
            } else {
                layoutParams.height = resources.getDisplayMetrics().heightPixels * 2 / 5;
            }
            ivNoItems.setVisibility(View.VISIBLE);
        }
        vgStickerSelector.setLayoutParams(layoutParams);


        int childCount = vgStickerGroups.getChildCount();
        for (int idx = 1; idx < childCount; idx++) {
            boolean selected = vgStickerGroups.getChildAt(idx).isSelected();
            if (selected) {
                updateStickerItems(idx, pagerStickerItems);
                break;
            }
        }
    }


    public interface OnStickerClick {
        void onStickerClick(int groupId, String stickerId);
    }

    public interface OnStickerDoubleTapListener {
        void onStickerDoubleTap(int groupId, String stickerId);
    }

    public interface OnStickerLayoutShowListener {
        void onStickerLayoutShow(boolean isShow);
    }
}
