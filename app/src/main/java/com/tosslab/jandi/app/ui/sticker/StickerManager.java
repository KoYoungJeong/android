package com.tosslab.jandi.app.ui.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.StateSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class StickerManager {

    public static final int DEFAULT_GROUP_MOZZI = 100;
    public static final int DEFAULT_GROUP_DAY = 101;
    private static final LoadOptions DEFAULT_OPTIONS = new LoadOptions();
    private static StickerManager stickerManager;

    private HashSet<Integer> localStickerGroupIds;

    private StickerManager() {
        this.localStickerGroupIds = new HashSet<Integer>();
        localStickerGroupIds.add(DEFAULT_GROUP_MOZZI);
        localStickerGroupIds.add(DEFAULT_GROUP_DAY);
    }

    public static StickerManager getInstance() {
        if (stickerManager == null) {
            stickerManager = new StickerManager();
        }

        return stickerManager;
    }

    public void loadStickerDefaultOption(ImageView view, int groupId, String stickerId) {

        loadSticker(view, groupId, stickerId, DEFAULT_OPTIONS);
    }

    public void loadStickerNoOption(ImageView view, int groupId, String stickerId) {

        LoadOptions loadOptions = new LoadOptions();
        loadOptions.isClickImage = false;
        loadOptions.isFadeAnimation = false;

        loadSticker(view, groupId, stickerId, loadOptions);
    }

    public void loadSticker(ImageView view, int groupId, String stickerId, LoadOptions options) {

        if (isLocalSticker(groupId)) {
            Context context = view.getContext();
            String stickerAssetPath = getStickerAssetPath(groupId, stickerId);

            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(300);

            DrawableTypeRequest<Uri> glideRequestor = Glide.with(context)
                    .load(Uri.parse(stickerAssetPath));

            glideRequestor.asBitmap()
                    .fitCenter()
                    .into(new BitmapImageViewTarget(view) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            if (options.isClickImage) {
                                StateListDrawable stateListDrawable = new StateListDrawable();
                                BitmapDrawable drawable = new BitmapDrawable(context.getResources(), resource);
                                drawable.setAlpha(153);
                                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, drawable);

                                stateListDrawable.addState(StateSet.WILD_CARD, new BitmapDrawable(context.getResources(), resource));
                                view.setImageDrawable(stateListDrawable);
                            } else {
                                view.setImageDrawable(new GlideBitmapDrawable(context.getResources(), resource));
                            }

                            if (options.isFadeAnimation) {
                                AlphaAnimation animation1 = new AlphaAnimation(0f, 1f);
                                animation1.setDuration(300);
                                view.startAnimation(animation1);
                            }

                        }
                    });

        } else {

        }
    }

    private boolean isLocalSticker(int groupId) {
        return localStickerGroupIds.contains(groupId);
    }

    private String getStickerAssetPath(int groupId, String stickerId) {
        List<ResMessages.StickerContent> stickers = StickerRepository.getRepository().getStickers(groupId);

        ResMessages.StickerContent defaultSticker = new ResMessages.StickerContent();
        ResMessages.StickerContent stickerItem = Observable.from(stickers)
                .filter(resSticker -> TextUtils.equals(resSticker.stickerId, stickerId))
                .firstOrDefault(defaultSticker)
                .toBlocking().first();

        if (stickerItem != defaultSticker) {
            String filePathFormat = "file:///android_asset/stickers/default/%s/%s.png";

            String fileName = stickerItem.groupId + "_" + stickerItem.stickerId;

            String group;
            switch (stickerItem.groupId) {
                case StickerRepository.DEFAULT_GROUP_ID_MOZZI :
                    group = "mozzi";
                    break;

                case StickerRepository.DEFAULT_GROUP_ID_DAY :
                    group = "day";
                    break;

                default:
                    group = "mozzi";
                    break;
            }

            String stickerFilePath = String.format(filePathFormat, group, fileName);
            LogUtil.e(stickerFilePath);
            return stickerFilePath;
        } else {
            return "";
        }
    }

    public static class LoadOptions {
        public boolean isFadeAnimation = true;
        public boolean isClickImage;
    }

}
