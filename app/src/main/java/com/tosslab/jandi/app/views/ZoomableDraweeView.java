package com.tosslab.jandi.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by tonyjs on 15. 12. 5..
 */
public class ZoomableDraweeView extends SimpleDraweeView implements IPhotoView {
    private PhotoViewAttacher photoViewAttacher;
    private ScaleType pendingScaleType;

    public ZoomableDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        initPhotoViewAttacher();
    }

    public ZoomableDraweeView(Context context) {
        super(context);
        initPhotoViewAttacher();
    }

    public ZoomableDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPhotoViewAttacher();
    }

    public ZoomableDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPhotoViewAttacher();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZoomableDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPhotoViewAttacher();
    }

    private void initPhotoViewAttacher() {
        if(null == this.photoViewAttacher || null == this.photoViewAttacher.getImageView()) {
            this.photoViewAttacher = new PhotoViewAttacher(this);
        }

        if(null != this.pendingScaleType) {
            this.setScaleType(this.pendingScaleType);
            this.pendingScaleType = null;
        }
    }

    /** @deprecated */
    public void setPhotoViewRotation(float rotationDegree) {
        this.photoViewAttacher.setRotationTo(rotationDegree);
    }

    public void setRotationTo(float rotationDegree) {
        this.photoViewAttacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        this.photoViewAttacher.setRotationBy(rotationDegree);
    }

    public boolean canZoom() {
        return this.photoViewAttacher.canZoom();
    }

    public RectF getDisplayRect() {
        return this.photoViewAttacher.getDisplayRect();
    }

    public Matrix getDisplayMatrix() {
        return this.photoViewAttacher.getDisplayMatrix();
    }

    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return this.photoViewAttacher.setDisplayMatrix(finalRectangle);
    }

    /** @deprecated */
    @Deprecated
    public float getMinScale() {
        return this.getMinimumScale();
    }

    public float getMinimumScale() {
        return this.photoViewAttacher.getMinimumScale();
    }

    /** @deprecated */
    @Deprecated
    public float getMidScale() {
        return this.getMediumScale();
    }

    public float getMediumScale() {
        return this.photoViewAttacher.getMediumScale();
    }

    /** @deprecated */
    @Deprecated
    public float getMaxScale() {
        return this.getMaximumScale();
    }

    public float getMaximumScale() {
        return this.photoViewAttacher.getMaximumScale();
    }

    public float getScale() {
        return this.photoViewAttacher.getScale();
    }

    public ScaleType getScaleType() {
        return this.photoViewAttacher.getScaleType();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        this.photoViewAttacher.setAllowParentInterceptOnEdge(allow);
    }

    /** @deprecated */
    @Deprecated
    public void setMinScale(float minScale) {
        this.setMinimumScale(minScale);
    }

    public void setMinimumScale(float minimumScale) {
        this.photoViewAttacher.setMinimumScale(minimumScale);
    }

    /** @deprecated */
    @Deprecated
    public void setMidScale(float midScale) {
        this.setMediumScale(midScale);
    }

    public void setMediumScale(float mediumScale) {
        this.photoViewAttacher.setMediumScale(mediumScale);
    }

    /** @deprecated */
    @Deprecated
    public void setMaxScale(float maxScale) {
        this.setMaximumScale(maxScale);
    }

    public void setMaximumScale(float maximumScale) {
        this.photoViewAttacher.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        this.photoViewAttacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if(null != this.photoViewAttacher) {
            this.photoViewAttacher.update();
        }

    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if(null != this.photoViewAttacher) {
            this.photoViewAttacher.update();
        }

    }

    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if(null != this.photoViewAttacher) {
            this.photoViewAttacher.update();
        }

    }

    public void setOnMatrixChangeListener(PhotoViewAttacher.OnMatrixChangedListener listener) {
        this.photoViewAttacher.setOnMatrixChangeListener(listener);
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.photoViewAttacher.setOnLongClickListener(l);
    }

    public void setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener listener) {
        this.photoViewAttacher.setOnPhotoTapListener(listener);
    }

    public PhotoViewAttacher.OnPhotoTapListener getOnPhotoTapListener() {
        return this.photoViewAttacher.getOnPhotoTapListener();
    }

    public void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener) {
        this.photoViewAttacher.setOnViewTapListener(listener);
    }

    public PhotoViewAttacher.OnViewTapListener getOnViewTapListener() {
        return this.photoViewAttacher.getOnViewTapListener();
    }

    public void setScale(float scale) {
        this.photoViewAttacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        this.photoViewAttacher.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        this.photoViewAttacher.setScale(scale, focalX, focalY, animate);
    }

    public void setScaleType(ScaleType scaleType) {
        if(null != this.photoViewAttacher) {
            this.photoViewAttacher.setScaleType(scaleType);
        } else {
            this.pendingScaleType = scaleType;
        }

    }

    public void setZoomable(boolean zoomable) {
        this.photoViewAttacher.setZoomable(zoomable);
    }

    public Bitmap getVisibleRectangleBitmap() {
        return this.photoViewAttacher.getVisibleRectangleBitmap();
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.photoViewAttacher.setZoomTransitionDuration(milliseconds);
    }

    public IPhotoView getIPhotoViewImplementation() {
        return this.photoViewAttacher;
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        this.photoViewAttacher.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    public void setOnScaleChangeListener(PhotoViewAttacher.OnScaleChangeListener onScaleChangeListener) {
        this.photoViewAttacher.setOnScaleChangeListener(onScaleChangeListener);
    }

    public void setOnSingleFlingListener(PhotoViewAttacher.OnSingleFlingListener onSingleFlingListener) {
        this.photoViewAttacher.setOnSingleFlingListener(onSingleFlingListener);
    }

    protected void onDetachedFromWindow() {
        this.photoViewAttacher.cleanup();
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        this.initPhotoViewAttacher();
        super.onAttachedToWindow();
    }
}
