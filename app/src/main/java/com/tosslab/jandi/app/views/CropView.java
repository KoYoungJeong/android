package com.tosslab.jandi.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 7. 31..
 */
public class CropView extends View {

    private Point[] inLinePoints;
    private Point[] outLinePoints;
    private int viewWidth;
    private int viewHeight;
    private Paint outLinePaint;
    private Paint inLinePaint;
    private Paint gridLinePaint;

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

        inLinePoints = new Point[4];
        outLinePoints = new Point[4];
        for (int idx = 0; idx < 4; ++idx) {
            inLinePoints[idx] = new Point();
            outLinePoints[idx] = new Point();
        }

        outLinePaint = new Paint();

        outLinePaint.setColor(getResources().getColor(R.color.jandi_black_de));
        outLinePaint.setStyle(Paint.Style.FILL);
        outLinePaint.setFlags(outLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);

        float inLineStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources()
                .getDisplayMetrics());

        inLinePaint = new Paint();
        inLinePaint.setColor(Color.WHITE);
        inLinePaint.setStyle(Paint.Style.STROKE);
        inLinePaint.setStrokeWidth(inLineStrokeWidth);
        inLinePaint.setFlags(inLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);


        gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.WHITE);
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(inLineStrokeWidth / 2);
        gridLinePaint.setFlags(gridLinePaint.getFlags() | Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.restore();


        drawOutline(canvas);
        drawInline(canvas);
        drawGridLine(canvas);

        canvas.save();

    }

    private void drawGridLine(Canvas canvas) {

        int width = Math.abs(inLinePoints[0].x - inLinePoints[1].x);
        int height = Math.abs(inLinePoints[0].y - inLinePoints[2].y);

        int intervalX = width / 3;
        int intervalY = height / 3;

        canvas.drawLine(inLinePoints[0].x + intervalX, inLinePoints[0].y,
                inLinePoints[2].x + intervalX, inLinePoints[2].y, gridLinePaint);
        canvas.drawLine(inLinePoints[0].x + intervalX * 2, inLinePoints[1].y,
                inLinePoints[0].x + intervalX * 2, inLinePoints[2].y, gridLinePaint);

        canvas.drawLine(inLinePoints[0].x, inLinePoints[0].y + intervalY,
                inLinePoints[2].x, inLinePoints[2].y + intervalY, gridLinePaint);
        canvas.drawLine(inLinePoints[1].x, inLinePoints[1].y + intervalY * 2,
                inLinePoints[2].x, inLinePoints[2].y + intervalY * 2, gridLinePaint);
    }

    private void drawInline(Canvas canvas) {
        Path mPath = new Path();

        mPath.moveTo(inLinePoints[0].x, inLinePoints[0].y);
        mPath.lineTo(inLinePoints[1].x, inLinePoints[1].y);
        mPath.lineTo(inLinePoints[3].x, inLinePoints[3].y);
        mPath.lineTo(inLinePoints[2].x, inLinePoints[2].y);
        mPath.lineTo(inLinePoints[0].x, inLinePoints[0].y);
        canvas.drawPath(mPath, inLinePaint);
    }

    private void drawOutline(Canvas canvas) {
        Path mPath = new Path();

        mPath.moveTo(inLinePoints[0].x, inLinePoints[0].y);
        mPath.lineTo(inLinePoints[1].x, inLinePoints[1].y);
        mPath.lineTo(inLinePoints[3].x, inLinePoints[3].y);
        mPath.lineTo(inLinePoints[2].x, inLinePoints[2].y);
        mPath.lineTo(inLinePoints[0].x, inLinePoints[0].y);

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
        inLinePoints[0].set(centerX - rectHalf, centerY - rectHalf);
        inLinePoints[1].set(centerX + rectHalf, centerY - rectHalf);
        inLinePoints[2].set(centerX - rectHalf, centerY + rectHalf);
        inLinePoints[3].set(centerX + rectHalf, centerY + rectHalf);

    }
}
