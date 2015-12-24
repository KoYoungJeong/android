package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.ClosableAttachStateChangeListener;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class StickerManager {

    //    public static final String ASSET_SCHEMA = "file:///android_asset/";
    public static final String ASSET_SCHEMA = "asset:///";
    public static final String STICKER_ASSET_PATH = "stickers/default";
    private static final LoadOptions DEFAULT_OPTIONS = new LoadOptions();
    private static StickerManager stickerManager;

    private HashSet<Integer> localStickerGroupIds;

    private StickerManager() {
        this.localStickerGroupIds = new HashSet<Integer>();
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_MOZZI);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_DAY);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_DAY_ZH_TW);
    }

    public static StickerManager getInstance() {
        if (stickerManager == null) {
            stickerManager = new StickerManager();
        }

        return stickerManager;
    }

    public void loadStickerDefaultOption(SimpleDraweeView view, int groupId, String stickerId) {

        loadSticker(view, groupId, stickerId, DEFAULT_OPTIONS);
    }

    public void loadStickerNoOption(SimpleDraweeView view, int groupId, String stickerId) {

        LoadOptions loadOptions = new LoadOptions();
        loadOptions.isClickImage = false;
        loadOptions.isFadeAnimation = false;

        loadSticker(view, groupId, stickerId, loadOptions);
    }

    public void loadSticker(SimpleDraweeView view,
                            int groupId, String stickerId, LoadOptions options) {

        String stickerAssetPath = null;
        if (isLocalSticker(groupId)) {
            stickerAssetPath = getStickerAssetPath(groupId, stickerId);
        } else {
            stickerAssetPath = JandiConstantsForFlavors.SERVICE_FILE_URL +
                    "files-sticker/" + groupId + "/" + stickerId + "?size=420";
        }

        if (!TextUtils.isEmpty(stickerAssetPath)) {
            Uri uri = Uri.parse(stickerAssetPath);
            loadSticker(uri, view, options);
        }
    }

    private void loadSticker(Uri uri, final SimpleDraweeView view, final LoadOptions options) {
        GenericDraweeHierarchy hierarchy = view.getHierarchy();
        hierarchy.setActualImageScaleType(options.scaleType);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(view.getController())
                .setUri(uri)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id,
                                                ImageInfo imageInfo, Animatable animatable) {
                        if (options.isFadeAnimation) {
                            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                            animation.setDuration(300);
                            view.startAnimation(animation);
                        }
                    }
                })
                .build();

        view.setController(controller);
    }

    private boolean isLocalSticker(int groupId) {
        return localStickerGroupIds.contains(groupId);
    }

    public String getStickerAssetPath(int groupId, String stickerId) {
        List<ResMessages.StickerContent> stickers = StickerRepository.getRepository().getStickers(groupId);
        ResMessages.StickerContent defaultSticker = new ResMessages.StickerContent();
        ResMessages.StickerContent stickerItem = Observable.from(stickers)
                .filter(resSticker -> TextUtils.equals(resSticker.stickerId, stickerId))
                .firstOrDefault(defaultSticker)
                .toBlocking().first();

        if (stickerItem != defaultSticker) {
            StringBuffer assetPathBuffer = new StringBuffer();
            assetPathBuffer
                    .append(STICKER_ASSET_PATH)
                    .append("/").append(getGroupName(stickerItem.groupId))
                    .append("/").append(groupId).append("_").append(stickerId).append(".png")
                    .insert(0, ASSET_SCHEMA);
            LogUtil.e(assetPathBuffer.toString());
            return assetPathBuffer.toString();
        } else {
            return "";
        }
    }

    @NonNull
    private String getGroupName(int groupId) {
        String group;
        switch (groupId) {
            case StickerRepository.DEFAULT_GROUP_ID_MOZZI:
                group = "mozzi";
                break;

            case StickerRepository.DEFAULT_GROUP_ID_DAY:
                group = "day";
                break;

            case StickerRepository.DEFAULT_GROUP_ID_DAY_ZH_TW:
                group = "day/zh_tw";
                break;

            default:
                group = "mozzi";
                break;
        }
        return group;
    }

    public static class LoadOptions {
        public boolean isFadeAnimation = true;
        public boolean isClickImage;
        public ScalingUtils.ScaleType scaleType = ScalingUtils.ScaleType.FIT_CENTER;
    }

}
