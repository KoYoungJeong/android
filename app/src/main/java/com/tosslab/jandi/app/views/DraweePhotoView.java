package com.tosslab.jandi.app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by tonyjs on 15. 12. 4..
 */
public class DraweePhotoView extends PhotoView {

    private DraweeHolder<GenericDraweeHierarchy> draweeHolder;

    public DraweePhotoView(Context context) {
        super(context);
        init();
    }

    public DraweePhotoView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public DraweePhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    @Override
    protected void init() {
        super.init();
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .build();
        draweeHolder = DraweeHolder.create(hierarchy, getContext());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        draweeHolder.onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        draweeHolder.onDetach();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        draweeHolder.onAttach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        draweeHolder.onAttach();
    }

    @Override
    protected boolean verifyDrawable(Drawable drawable) {
        if (drawable == draweeHolder.getHierarchy().getTopLevelDrawable()) {
            return true;
        }
        return super.verifyDrawable(drawable);
    }

    @Override
    @Deprecated
    public void setImageDrawable(Drawable drawable) {
        draweeHolder.setController(null);
        super.setImageDrawable(drawable);
    }

    @Override
    @Deprecated
    public void setImageBitmap(Bitmap bm) {
        draweeHolder.setController(null);
        super.setImageBitmap(bm);
    }

    @Override
    @Deprecated
    public void setImageResource(int resId) {
        draweeHolder.setController(null);
        super.setImageResource(resId);
    }

    @Override
    @Deprecated
    public void setImageURI(Uri uri) {
        draweeHolder.setController(null);
        super.setImageURI(uri);
    }

    public void setHierarchy(GenericDraweeHierarchy hierarchy) {
        draweeHolder.setHierarchy(hierarchy);
    }

    public void setController(DraweeController controller) {
        draweeHolder.setController(controller);
        super.setImageDrawable(draweeHolder.getTopLevelDrawable());
    }

    public DraweeController getController() {
        return draweeHolder.getController();
    }

    public GenericDraweeHierarchy getHierarchy() {
        return draweeHolder.getHierarchy();
    }

}
