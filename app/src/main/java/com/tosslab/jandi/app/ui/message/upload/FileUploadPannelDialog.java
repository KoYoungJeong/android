package com.tosslab.jandi.app.ui.message.upload;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.poll.RequestCreatePollEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.utils.FileAccessLimitUtil;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 2017. 3. 8..
 */

public class FileUploadPannelDialog extends Dialog {

    @Bind(R.id.vg_background)
    ViewGroup background;

    @Bind(R.id.vg_picture)
    ViewGroup vgUploadPicture;

    @Bind(R.id.vg_video)
    ViewGroup vgUploadVideo;

    @Bind(R.id.vg_camera)
    ViewGroup vgUploadCamera;

    @Bind(R.id.vg_file)
    ViewGroup vgUploadFile;

    @Bind(R.id.vg_poll)
    ViewGroup vgUploadPoll;

    @Bind(R.id.vg_contact)
    ViewGroup vgUploadContact;

    @Bind(R.id.iv_cancel)
    ImageView ivCancel;

    private boolean processDismiss = false;

    private boolean hasPoll = true;

    private FileAccessLimitUtil fileAccessLimitUtil;

    public FileUploadPannelDialog(Context context, boolean hasPoll) {
        super(context, R.style.Theme_AppCompat_Translucent);
        this.hasPoll = hasPoll;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message_file_upload_pannel);
        ButterKnife.bind(this);
        if (!hasPoll) {
            vgUploadPoll.setVisibility(View.GONE);
        }
        ShowItemsAnimation();
        initItemButtons();
    }

    private void initItemButtons() {
        fileAccessLimitUtil = FileAccessLimitUtil.newInstance();

        vgUploadPicture.setOnClickListener(v -> {
            fileAccessLimitUtil.execute(getContext(), () -> {
                dismissDialog();
                Completable.complete()
                        .delay(400, TimeUnit.MILLISECONDS)
                        .subscribe(() -> {
                            EventBus.getDefault().post(
                                    new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_IMAGE_GALLERY));
                        });

            });
        });

        vgUploadVideo.setOnClickListener(v -> {
            fileAccessLimitUtil.execute(getContext(), () -> {
                dismissDialog();
                Completable.complete()
                        .delay(400, TimeUnit.MILLISECONDS)
                        .subscribe(() -> {
                            EventBus.getDefault().post(
                                    new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_VIDEO_GALARY));
                        });
            });
        });

        vgUploadCamera.setOnClickListener(v -> {
            fileAccessLimitUtil.execute(getContext(), () -> {
                dismissDialog();
                Completable.complete()
                        .delay(400, TimeUnit.MILLISECONDS)
                        .subscribe(() -> {
                            EventBus.getDefault().post(
                                    new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_TAKE_VIDEO));

                        });
            });
        });
        vgUploadFile.setOnClickListener(v -> {
            fileAccessLimitUtil.execute(getContext(), () -> {
                dismissDialog();
                Completable.complete()
                        .delay(400, TimeUnit.MILLISECONDS)
                        .subscribe(() -> {
                            EventBus.getDefault().post(
                                    new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_EXPLORER));
                        });
            });

        });
        vgUploadPoll.setOnClickListener(v -> {
            dismissDialog();
            Completable.complete()
                    .delay(400, TimeUnit.MILLISECONDS)
                    .subscribe(() -> {
                        EventBus.getDefault().post(new RequestCreatePollEvent());
                    });
        });

        vgUploadContact.setOnClickListener(v -> {
            fileAccessLimitUtil.execute(getContext(), () -> {
                dismissDialog();
                Completable.complete()
                        .delay(400, TimeUnit.MILLISECONDS)
                        .subscribe(() -> {
                            EventBus.getDefault().post(
                                    new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_CONTACT));
                        });
            });
        });
    }

    @Override
    public void onBackPressed() {
        dismissDialog();
    }

    @OnClick(R.id.iv_cancel)
    public void onClickCancel() {
        dismissDialog();
    }

    @OnClick(R.id.vg_background)
    public void onClickBackground() {
        dismissDialog();
    }

    private void dismissDialog() {
        // 중복 방지를 위해
        if (processDismiss) {
            return;
        }
        processDismiss = true;
        DismissItemsAnimation();
    }

    private void ShowItemsAnimation() {
        Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(300);
        background.startAnimation(fadeInAnimation);

        Observable.just(1)
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    vgUploadPicture.setVisibility(View.VISIBLE);
                    vgUploadPicture.startAnimation(animationSet);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    vgUploadVideo.setVisibility(View.VISIBLE);
                    vgUploadVideo.startAnimation(animationSet);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    vgUploadCamera.setVisibility(View.VISIBLE);
                    vgUploadCamera.startAnimation(animationSet);
                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    vgUploadFile.setVisibility(View.VISIBLE);
                    vgUploadFile.startAnimation(animationSet);
                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    if (hasPoll) {
                        Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                        translationAnimation.setDuration(200);
                        Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                        fadeInAnimation1.setDuration(200);

                        AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(translationAnimation);
                        animationSet.addAnimation(fadeInAnimation1);
                        vgUploadPoll.setVisibility(View.VISIBLE);
                        vgUploadPoll.startAnimation(animationSet);
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    vgUploadContact.setVisibility(View.VISIBLE);
                    vgUploadContact.startAnimation(animationSet);
                }).delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 100, 0);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    ivCancel.setVisibility(View.VISIBLE);
                    ivCancel.startAnimation(animationSet);
                })
                .subscribe();
    }

    private void DismissItemsAnimation() {
        Observable.just(1)
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);
                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    ivCancel.clearAnimation();
                    ivCancel.startAnimation(animationSet);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);
                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    vgUploadContact.clearAnimation();
                    vgUploadContact.startAnimation(animationSet);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    if (hasPoll) {
                        Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                        translationAnimation.setDuration(200);
                        Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                        fadeInAnimation1.setDuration(200);

                        AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(translationAnimation);
                        animationSet.addAnimation(fadeInAnimation1);
                        animationSet.setFillAfter(true);
                        animationSet.setFillEnabled(true);
                        vgUploadPoll.clearAnimation();
                        vgUploadPoll.startAnimation(animationSet);
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);
                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    vgUploadFile.clearAnimation();
                    vgUploadFile.startAnimation(animationSet);

                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    vgUploadCamera.clearAnimation();
                    vgUploadCamera.startAnimation(animationSet);
                }).subscribeOn(AndroidSchedulers.mainThread())
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    vgUploadVideo.clearAnimation();
                    vgUploadVideo.startAnimation(animationSet);
                }).delay(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    Animation translationAnimation = new TranslateAnimation(0, 0, 0, 200);
                    translationAnimation.setDuration(200);
                    Animation fadeInAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation1.setDuration(200);

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(translationAnimation);
                    animationSet.addAnimation(fadeInAnimation1);
                    animationSet.setFillAfter(true);
                    animationSet.setFillEnabled(true);
                    vgUploadPicture.clearAnimation();
                    vgUploadPicture.startAnimation(animationSet);
                })
                .subscribe();

        Completable.complete()
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Animation fadeInAnimation = new AlphaAnimation(1.0f, 0.0f);
                    fadeInAnimation.setDuration(200);
                    fadeInAnimation.setFillAfter(true);
                    fadeInAnimation.setFillEnabled(true);
                    background.startAnimation(fadeInAnimation);
                    fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            FileUploadPannelDialog.this.dismiss();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                });
    }

}
