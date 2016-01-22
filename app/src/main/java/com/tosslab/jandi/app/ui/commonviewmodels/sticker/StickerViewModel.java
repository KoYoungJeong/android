package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.ViewPagerIndicator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;

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

    public static final int VIEW_HEIGHT_DEFAULT = -1;

    ViewGroup vgOptionSpace;

    WindowManager.LayoutParams params;

    View btnStickerShow;

    @RootContext
    Context context;

    @Bean
    KeyboardHeightModel keyboardHeightModel;

    // sticker view
    private FrameLayout vgStickerSelector;
    private LinearLayout vgStickerGroups;
    private ViewPager pagerStickerItems;
    private ViewGroup vgNoItemsLayout;
    private ViewPagerIndicator viewPagerIndicator;

    private WindowManager windowManager;

    private OnStickerClick onStickerClick;
    private OnStickerDoubleTapListener onStickerDoubleTapListener;
    private OnStickerLayoutShowListener onStickerLayoutShowListener;
    private int type;

    private Pair<Long, String> lastClickedStickerInfo;
    private long lastClickedTime;
    private boolean isShow = false;
    private ImageView ivNoItems;

    private boolean isKeyboardShow;

    @AfterViews
    void initViews() {
        // STICKER INIT
        vgStickerSelector = new FrameLayout(context);
        vgStickerSelector.setVisibility(View.GONE);
        LayoutInflater.from(context).inflate(R.layout.layout_stickers_default, vgStickerSelector, true);
        vgStickerGroups = (LinearLayout) vgStickerSelector.findViewById(R.id.vg_sticker_default_groups);
        pagerStickerItems = (ViewPager) vgStickerSelector.findViewById(R.id.pager_sticker_default_items);
        vgNoItemsLayout = (ViewGroup) vgStickerSelector.findViewById(R.id.vg_sticker_default_items_no_item);
        ivNoItems = (ImageView) vgStickerSelector.findViewById(R.id.iv_sticker_default_items_no_item);
        viewPagerIndicator = (ViewPagerIndicator) vgStickerSelector.findViewById(R.id.indicator_sticker_default_items_page_indicator);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);  //윈도우 매니저
        initClicks();
        registKetboardCallback();
    }

    private void registKetboardCallback() {
        keyboardHeightModel.addOnKeyboardShowListener(isShow -> {
            if (isShow) {
                isKeyboardShow = true;
                if (StickerViewModel.this.isShowStickerSelector()) {
                    StickerViewModel.this.dismissStickerSelector(true);
                }
            } else {
                isKeyboardShow = false;
            }
        });
    }

    public void setOptionSpace(ViewGroup optionSpace) {
        this.vgOptionSpace = optionSpace;
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
            public void onStickerClick(long groupId, String stickerId) {
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
     * @param height 높이
     */
    @UiThread
    public void showStickerSelector(int height) {
        Resources resources = vgStickerSelector.getResources();

        int keyboardHeight;

        if (height < 0) {
            keyboardHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
        } else {
            keyboardHeight = height;
        }

        showStickerWindow(keyboardHeight);

        ViewGroup.LayoutParams layoutParams = vgStickerSelector.getLayoutParams();
        ViewGroup.LayoutParams vgSpaceLayoutParams = vgOptionSpace.getLayoutParams();
        layoutParams.height = keyboardHeight;
        vgSpaceLayoutParams.height = keyboardHeight;
        vgStickerSelector.setLayoutParams(layoutParams);
        vgOptionSpace.setLayoutParams(vgSpaceLayoutParams);

        setupGroupState(1, vgStickerGroups);
        updateStickerItems(1, pagerStickerItems);

        vgStickerSelector.setVisibility(View.VISIBLE);

        if (!isKeyboardShow) {
            vgOptionSpace.setVisibility(View.VISIBLE);
        } else {
            vgOptionSpace.setVisibility(View.GONE);
        }

        if (btnStickerShow != null) {
            btnStickerShow.setSelected(true);
        }

        isShow = true;

        if (onStickerLayoutShowListener != null) {
            onStickerLayoutShowListener.onStickerLayoutShow(true);
        }
    }

    public void showStickerWindow(int height) {
        //최상위 윈도우에 넣기 위한 설정
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_PHONE,//항상 최 상위. 터치 이벤트 받을 수 있음.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);                                        //투명
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        windowManager.addView(vgStickerSelector, params);      //윈도우에 뷰 넣기. permission 필요.
    }

    public void dismissStickerWindow() {
        if (vgStickerSelector != null && vgStickerSelector.getParent() != null) {
            windowManager.removeView(vgStickerSelector);
        }
    }

    @UiThread
    public void dismissStickerSelector(boolean isRemoveSpace) {
        dismissStickerWindow();
        vgStickerSelector.setVisibility(View.GONE);
        if (isRemoveSpace) {
            vgOptionSpace.setVisibility(View.GONE);
        }
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
        if (!isShowStickerSelector()) {
            return;
        }

        dismissStickerSelector(true);

        Resources resources = vgStickerSelector.getResources();
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ivNoItems.setVisibility(View.GONE);
        } else {
            ivNoItems.setVisibility(View.VISIBLE);
        }

        // 최소한 0.7 초를 주지 않으면 공간계산이 자동으로 되지 않는다. 특히 키보드 위에 스티커가 있는 경우 순간적으로 뷰가 전환되는
        // 시점에 많은 계산이 이루어 지는 것으로 보인다.
        if (keyboardHeightModel.isOpened()) {
            keyboardHeightModel.hideKeyboard();
            Observable.just(1)
                    .delay(700, TimeUnit.MILLISECONDS)
                    .subscribe(i -> {
                        showStickerSelector(VIEW_HEIGHT_DEFAULT);
                    });
        } else {
            showStickerSelector(VIEW_HEIGHT_DEFAULT);
        }
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
