package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */

public class StickerViewModel {

    public static final int STICKER_GROUP_RECENT = 0;
    public static final int STICKER_GROUP_DEAN = 1;
    public static final int STICKER_GROUP_BANILA = 2;
    public static final int STICKER_GROUP_MALLOW = 3;
    public static final int STICKER_GROUP_DINGO = 4;
    public static final int STICKER_GROUP_DAY = 5;
    public static final int STICKER_GROUP_MOZZI = 6;

    public static final int TYPE_MESSAGE = 11;
    public static final int TYPE_TOPIC = 12;
    public static final int TYPE_FILE_DETAIL = 13;
    public static final int TYPE_POLL_DETAIL = 14;

    // sticker view
    private LinearLayout vgStickerGroups;
    private ViewPager pagerStickerItems;
    private ViewGroup vgNoItemsLayout;
    private ViewPagerIndicator viewPagerIndicator;

    private OnStickerClick onStickerClick;
    private OnStickerDoubleTapListener onStickerDoubleTapListener;
    private OnStickerLayoutShowListener onStickerLayoutShowListener;
    private int type;

    private Pair<Long, String> lastClickedStickerInfo;
    private long lastClickedTime;
    private boolean isShow = false;
    private View vgStickers;

    @Inject
    public StickerViewModel() { }

    public void showStickerPanel(ViewGroup root) {
        vgStickers = LayoutInflater.from(root.getContext()).inflate(R.layout.layout_stickers_default, root, true);

        vgStickerGroups = (LinearLayout) vgStickers.findViewById(R.id.vg_sticker_default_groups);
        pagerStickerItems = (ViewPager) vgStickers.findViewById(R.id.pager_sticker_default_items);
        vgNoItemsLayout = (ViewGroup) vgStickers.findViewById(R.id.vg_sticker_default_items_no_item);
        viewPagerIndicator =
                (ViewPagerIndicator) vgStickers.findViewById(
                        R.id.indicator_sticker_default_items_page_indicator);

        initClicks();

        int tabIndex = getStickerTabIndex();

        setupGroupState(tabIndex, vgStickerGroups);
        updateStickerItems(tabIndex, pagerStickerItems);

        if (onStickerLayoutShowListener != null) {
            onStickerLayoutShowListener.onStickerLayoutShow(isShow = true);
        }
    }

    private void initClicks() {
        for (int idx = 0, size = vgStickerGroups.getChildCount(); idx < size; ++idx) {
            final int finalIdx = idx;
            vgStickerGroups.getChildAt(idx).setOnClickListener(view -> {
                setupGroupState(finalIdx, vgStickerGroups);
                updateStickerItems(finalIdx, pagerStickerItems);
                sendAnalyticsAction(finalIdx);
            });
        }
    }

    private void updateStickerItems(int groupIdx, ViewPager vgStickerItems) {
        List<ResMessages.StickerContent> stickers;
        StickerRepository stickerRepository = StickerRepository.getRepository();

        switch (groupIdx) {
            case STICKER_GROUP_RECENT:
                stickers = stickerRepository.getRecentStickers();
                break;

            case STICKER_GROUP_DINGO:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_DINGO);
                break;
            case STICKER_GROUP_DAY:
                Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
                if (!Locale.TAIWAN.equals(locale)) {
                    stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_DAY);
                } else {
                    stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_DAY_ZH_TW);
                }
                break;
            case STICKER_GROUP_MOZZI:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_MOZZI);
                break;
            case STICKER_GROUP_MALLOW:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_MALLOW_DOG);
                break;
            case STICKER_GROUP_BANILA:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_BANILA);
                break;
            case STICKER_GROUP_DEAN:
                stickers = stickerRepository.getStickers(StickerRepository.DEFAULT_GROUP_ID_DEAN);
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

        StickerViewPagerAdapter adapter = new StickerViewPagerAdapter(stickers,
                (groupId, stickerId) -> {
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
                });
        vgStickerItems.setAdapter(adapter);

        vgStickerItems.setCurrentItem(adapter.getActualCount() * StickerViewPagerAdapter.PAGE_MULTIPLE / 2);
        viewPagerIndicator.setCurrentPosition(0);
        viewPagerIndicator.setIndicatorCount(adapter.getActualCount());
        viewPagerIndicator.invalidate();

        vgStickerItems.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (viewPagerIndicator != null) {
                    viewPagerIndicator.setCurrentPosition(adapter.getAcualPosition(position, adapter.getActualCount()));
                }
            }
        });

    }

    public boolean isDoubleTap(long lastClickedTime) {
        return System.currentTimeMillis() - lastClickedTime <= ViewConfiguration.getJumpTapTimeout();
    }

    public boolean isSameSticker(long groupId, String stickerId) {
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
        }
    }

    private void sendAnalyticsAction(int groupIdx) {
        AnalyticsValue.Screen screen = getScreen();

        if (groupIdx == 0) {
            AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.Sticker_RecentTab);
        } else {
            String group;
            switch (groupIdx) {
                case STICKER_GROUP_DEAN:
                    group = "(Dean)";
                    break;
                case STICKER_GROUP_BANILA:
                    group = "(Dingo2)";
                    break;
                case STICKER_GROUP_MALLOW:
                    group = "(Mallow)";
                    break;
                case STICKER_GROUP_DINGO:
                    group = "(Dingo)";
                    break;
                case STICKER_GROUP_DAY:
                    group = "(Day_Emily)";
                    break;
                case STICKER_GROUP_MOZZI:
                    group = "(Mozzi)";
                    break;

                default:
                    group = "";
            }

            AnalyticsValue.Action action = AnalyticsValue.Action.Sticker_StickerTab;
            String resultAction = new StringBuilder(action.name())
                    .append(group)
                    .toString();
            AnalyticsUtil.sendEvent(screen.name(), resultAction, "");
        }
    }

    private AnalyticsValue.Screen getScreen() {
        switch (type) {
            case TYPE_POLL_DETAIL:
                return AnalyticsValue.Screen.PollDetail;
            case TYPE_FILE_DETAIL:
                return AnalyticsValue.Screen.FileDetail;
            case TYPE_MESSAGE:
                return AnalyticsValue.Screen.Message;
            default:
            case TYPE_TOPIC:
                return AnalyticsValue.Screen.TopicChat;

        }
    }

    private int getStickerTabIndex() {
        List<ResMessages.StickerContent> recentStickers =
                StickerRepository.getRepository().getRecentStickers();
        if (recentStickers != null && !recentStickers.isEmpty()) {
            return STICKER_GROUP_RECENT;
        }
        return STICKER_GROUP_DEAN;
    }

    public void setOnStickerClick(OnStickerClick onStickerClick) {
        this.onStickerClick = onStickerClick;
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

    public interface OnStickerClick {
        void onStickerClick(long groupId, String stickerId);
    }

    public interface OnStickerDoubleTapListener {
        void onStickerDoubleTap(long groupId, String stickerId);
    }

    public interface OnStickerLayoutShowListener {
        void onStickerLayoutShow(boolean isShow);
    }
}
