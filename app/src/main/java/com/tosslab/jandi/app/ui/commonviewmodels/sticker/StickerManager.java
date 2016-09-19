package com.tosslab.jandi.app.ui.commonviewmodels.sticker;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

public class StickerManager {

    public static final String ASSET_SCHEMA = "file:///android_asset/";
    public static final String STICKER_ASSET_PATH = "stickers/default";
    private static final LoadOptions DEFAULT_OPTIONS = new LoadOptions();
    private static StickerManager stickerManager;

    private HashSet<Long> localStickerGroupIds;

    private StickerManager() {
        this.localStickerGroupIds = new HashSet<>();
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_MOZZI);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_DAY);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_DAY_ZH_TW);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_DINGO);
        localStickerGroupIds.add(StickerRepository.DEFAULT_GROUP_ID_MALLOW_DOG);
    }

    public static StickerManager getInstance() {
        if (stickerManager == null) {
            stickerManager = new StickerManager();
        }

        return stickerManager;
    }

    public void loadStickerDefaultOption(ImageView view, long groupId, String stickerId) {

        loadSticker(view, groupId, stickerId, DEFAULT_OPTIONS);
    }

    public void loadStickerNoOption(ImageView view, long groupId, String stickerId) {

        LoadOptions loadOptions = new LoadOptions();
        loadOptions.isClickImage = false;
        loadOptions.isFadeAnimation = false;

        loadSticker(view, groupId, stickerId, loadOptions);
    }

    public void loadSticker(ImageView view,
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

    private void loadSticker(Uri uri, final ImageView view, final LoadOptions options) {
        ImageLoader loader = ImageLoader.newInstance()
                .actualImageScaleType(options.scaleType);
        if (options.isFadeAnimation) {
            loader.animate(android.R.anim.fade_in);
        }
        loader.uri(uri).into(view);
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
        } else if (groupId == StickerRepository.DEFAULT_GROUP_ID_DINGO) {
            group = "dingo";
        } else if (groupId == StickerRepository.DEFAULT_GROUP_ID_MOZZI) {
            group = "mozzi";
        } else if (groupId == StickerRepository.DEFAULT_GROUP_ID_MALLOW_DOG) {
            group = "mallow";
        } else {
            group = "";
        }

        return group;
    }

    public static class LoadOptions {
        public boolean isFadeAnimation = true;
        public boolean isClickImage;
        public ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_CENTER;
    }

}
