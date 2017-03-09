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

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 2017. 3. 8..
 */

public class FileUploadPannelDialog extends Dialog {

    @Bind(R.id.vg_background)
    ViewGroup background;

    @Bind(R.id.vg_picture)
    ViewGroup vgPicture;

    @Bind(R.id.vg_video)
    ViewGroup vgVideo;

    @Bind(R.id.vg_camera)
    ViewGroup vgCamera;

    @Bind(R.id.vg_file)
    ViewGroup vgFile;

    @Bind(R.id.vg_poll)
    ViewGroup vgPoll;

    @Bind(R.id.vg_contract)
    ViewGroup vgContract;

    @Bind(R.id.iv_cancel)
    ImageView ivCancel;

    private boolean processDismiss = false;

    public FileUploadPannelDialog(Context context) {
        super(context, R.style.Theme_AppCompat_Translucent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message_file_upload_pannel);
        ButterKnife.bind(this);
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
                    vgPicture.setVisibility(View.VISIBLE);
                    vgPicture.startAnimation(animationSet);
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
                    vgVideo.setVisibility(View.VISIBLE);
                    vgVideo.startAnimation(animationSet);
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
                    vgCamera.setVisibility(View.VISIBLE);
                    vgCamera.startAnimation(animationSet);
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
                    vgFile.setVisibility(View.VISIBLE);
                    vgFile.startAnimation(animationSet);
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
                    vgPoll.setVisibility(View.VISIBLE);
                    vgPoll.startAnimation(animationSet);
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
                    vgContract.setVisibility(View.VISIBLE);
                    vgContract.startAnimation(animationSet);
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
        Animation fadeInAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeInAnimation.setDuration(550);
        fadeInAnimation.setFillAfter(true);
        fadeInAnimation.setFillEnabled(true);
        background.startAnimation(fadeInAnimation);

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
                    vgContract.clearAnimation();
                    vgContract.startAnimation(animationSet);
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
                    vgPoll.clearAnimation();
                    vgPoll.startAnimation(animationSet);
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
                    vgFile.clearAnimation();
                    vgFile.startAnimation(animationSet);
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
                    vgCamera.clearAnimation();
                    vgCamera.startAnimation(animationSet);
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
                    vgVideo.clearAnimation();
                    vgVideo.startAnimation(animationSet);
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
                    animationSet.setAnimationListener(new Animation.AnimationListener() {
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
                    vgPicture.clearAnimation();
                    vgPicture.startAnimation(animationSet);
                })
                .subscribe();
    }

}
