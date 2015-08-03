package com.tosslab.jandi.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 7. 31..
 * 만들다 중단. Crop 용 외부 라이브러리 사용함
 */
public class CropView extends View {

    private Rect inLineRect;
    private PointF lastTouchPoint;
    private int viewWidth;
    private int viewHeight;
    private Paint outLinePaint;
    private Paint inLinePaint;
    private Paint gridLinePaint;

    private TouchMode touchMode;

    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        inLineRect = new Rect();

        lastTouchPoint = new PointF(0, 0);

        outLinePaint = new Paint();

        int alphaBlack = getResources().getColor(R.color.jandi_black_25);
        int alphaWhite = getResources().getColor(R.color.jandi_transparent_white_30p);
        outLinePaint.setColor(alphaBlack);
        outLinePaint.setStyle(Paint.Style.FILL);
        outLinePaint.setFlags(outLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);

        float inLineStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources()
                .getDisplayMetrics());

        inLinePaint = new Paint();
        inLinePaint.setColor(alphaWhite);
        inLinePaint.setStyle(Paint.Style.STROKE);
        inLinePaint.setStrokeWidth(inLineStrokeWidth);
        inLinePaint.setFlags(inLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);


        gridLinePaint = new Paint();
        gridLinePaint.setColor(alphaWhite);
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(inLineStrokeWidth / 2);
        gridLinePaint.setFlags(gridLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.save();

        drawOutline(canvas);
        drawInline(canvas);
        drawGridLine(canvas);

        canvas.restore();

    }

    private void drawGridLine(Canvas canvas) {

        int width = Math.abs(inLineRect.left - inLineRect.right);
        int height = Math.abs(inLineRect.top - inLineRect.bottom);

        int intervalX = width / 3;
        int intervalY = height / 3;

        canvas.drawLine(inLineRect.left + intervalX, inLineRect.top,
                inLineRect.left + intervalX, inLineRect.bottom, gridLinePaint);
        canvas.drawLine(inLineRect.left + intervalX * 2, inLineRect.top,
                inLineRect.left + intervalX * 2, inLineRect.bottom, gridLinePaint);

        canvas.drawLine(inLineRect.left, inLineRect.top + intervalY,
                inLineRect.right, inLineRect.top + intervalY, gridLinePaint);
        canvas.drawLine(inLineRect.left, inLineRect.top + intervalY * 2,
                inLineRect.right, inLineRect.top + intervalY * 2, gridLinePaint);
    }

    private void drawInline(Canvas canvas) {
        Path mPath = new Path();

        mPath.moveTo(inLineRect.left, inLineRect.top);
        mPath.lineTo(inLineRect.right, inLineRect.top);
        mPath.lineTo(inLineRect.right, inLineRect.bottom);
        mPath.lineTo(inLineRect.left, inLineRect.bottom);
        mPath.lineTo(inLineRect.left, inLineRect.top);
        canvas.drawPath(mPath, inLinePaint);
    }

    private void drawOutline(Canvas canvas) {
        Path mPath = new Path();

        mPath.moveTo(inLineRect.left, inLineRect.top);
        mPath.lineTo(inLineRect.right, inLineRect.top);
        mPath.lineTo(inLineRect.right, inLineRect.bottom);
        mPath.lineTo(inLineRect.left, inLineRect.bottom);
        mPath.lineTo(inLineRect.left, inLineRect.top);

        mPath.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CCW);
        mPath.setFillType(Path.FillType.WINDING);
        canvas.drawPath(mPath, outLinePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);


        int centerX = viewWidth / 2;
        int centerY = viewHeight / 2;

        int rectSize = Math.min(viewWidth, viewHeight);

        int rectHalf = rectSize / 2;
        inLineRect.left = centerX - rectHalf;
        inLineRect.top = centerY - rectHalf;
        inLineRect.right = centerX + rectHalf;
        inLineRect.bottom = centerY + rectHalf;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPoint.x = event.getX();
                lastTouchPoint.y = event.getY();

                touchMode = getTouchMode(lastTouchPoint, inLineRect);

                return true;
            case MotionEvent.ACTION_MOVE:



                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onTouchEvent(event);
    }

    private TouchMode getTouchMode(PointF lastTouchPoint, Rect inLineRect) {

        return inLineRect.contains(((int) lastTouchPoint.x), ((int) lastTouchPoint.y)) ?
                TouchMode.IN : TouchMode.OUT;
    }

    private enum TouchMode {
        OUT, IN
    }
}
