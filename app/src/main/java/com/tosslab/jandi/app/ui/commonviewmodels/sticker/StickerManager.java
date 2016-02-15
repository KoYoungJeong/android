package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

public class StickerManager {

    public static final String ASSET_SCHEMA = "asset:///";
    public static final String STICKER_ASSET_PATH = "stickers/default";
    private static final LoadOptions DEFAULT_OPTIONS = new LoadOptions();
    private static StickerManager stickerManager;

    private HashSet<Long> localStickerGroupIds;

    private StickerManager() {
        this.localStickerGroupIds = new HashSet<>();
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

    public void loadStickerDefaultOption(SimpleDraweeView view, long groupId, String stickerId) {

        loadSticker(view, groupId, stickerId, DEFAULT_OPTIONS);
    }

    public void loadStickerNoOption(SimpleDraweeView view, long groupId, String stickerId) {

        LoadOptions loadOptions = new LoadOptions();
        loadOptions.isClickImage = false;
        loadOptions.isFadeAnimation = false;

        loadSticker(view, groupId, stickerId, loadOptions);
    }

    public void loadSticker(SimpleDraweeView view,
                            long groupId, String stickerId, LoadOptions options) {

        String stickerAssetPath;
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
        ImageLoader.newBuilder()
                .actualScaleType(options.scaleType)
                .controllerListener(new BaseControllerListener<ImageInfo>() {
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
                .load(uri)
                .into(view);
    }

    private boolean isLocalSticker(long groupId) {
        return localStickerGroupIds.contains(groupId);
    }

    public String getStickerAssetPath(long groupId, String stickerId) {
        List<ResMessages.StickerContent> stickers = StickerRepository.getRepository().getStickers(groupId);
        ResMessages.StickerContent defaultSticker = new ResMessages.StickerContent();
        ResMessages.StickerContent stickerItem = Observable.from(stickers)
                .filter(resSticker -> TextUtils.equals(resSticker.stickerId, stickerId))
                .firstOrDefault(defaultSticker)
                .toBlocking().first();

        if (stickerItem != defaultSticker) {
            StringBuilder assetPathBuffer = new StringBuilder();
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
    private String getGroupName(long groupId) {
        String group;
        if (groupId == StickerRepository.DEFAULT_GROUP_ID_MOZZI) {
            group = "mozzi";
        } else if (groupId == StickerRepository.DEFAULT_GROUP_ID_DAY) {
            group = "day";
        } else if (groupId == StickerRepository.DEFAULT_GROUP_ID_DAY_ZH_TW) {
            group = "day/zh_tw";
        } else {
            group = "mozzi";
        }
        return group;
    }

    public static class LoadOptions {
        public boolean isFadeAnimation = true;
        public boolean isClickImage;
        public ScalingUtils.ScaleType scaleType = ScalingUtils.ScaleType.FIT_CENTER;
    }

}
