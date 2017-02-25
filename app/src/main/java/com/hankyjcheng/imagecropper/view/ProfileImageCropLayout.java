package com.hankyjcheng.imagecropper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by hankcheng on 1/14/2017.
 */

public class ProfileImageCropLayout extends LinearLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private Paint circlePaint;
    private Paint rectPaint;
    private float centerX;
    private float centerY;
    private final float radiusMin = 100;
    private float radiusMax = radiusMin;
    private float radius = radiusMin;
    private Bitmap bitmap;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isScaling = false;
    private boolean isScrolling = false;
    private RectF rect;
    private ImageView childImageView;
    private int scaleStartDistance;

    private float touchDownX, touchDownY;
    private float minX, minY, maxX, maxY;

    public ProfileImageCropLayout(Context context) {
        super(context);
        init();
    }

    public ProfileImageCropLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfileImageCropLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setColor(Color.parseColor("#A6000000"));

        circlePaint = new Paint();
        circlePaint.setColor(Color.TRANSPARENT);
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (bitmap == null) {
            rect = new RectF(0, 0, getWidth(), getHeight());
        }
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);
        osCanvas.drawRect(rect, rectPaint);
        osCanvas.drawCircle(centerX, centerY, radius, circlePaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1 && scaleGestureDetector.onTouchEvent(event)) {
            isScaling = true;
            return true;
        }
        else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    onTouchDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    onTouchMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onTouchUp();
                    break;
            }
        }
        return true;
    }

    private void onTouchDown(MotionEvent event) {
        touchDownX = event.getX();
        touchDownY = event.getY();
        if (Math.pow(touchDownX - centerX, 2) + Math.pow(touchDownY - centerY, 2)
                <= radius * radius) {
            onScrollBegin();
        }
    }

    private void onTouchMove(MotionEvent event) {
        if (!isScaling && isScrolling) {
            onScroll(event.getX() - touchDownX, event.getY() - touchDownY);
            touchDownX = event.getX();
            touchDownY = event.getY();
        }
    }

    private void onTouchUp() {
        isScaling = false;
        isScrolling = false;
    }

    private void onScrollBegin() {
        isScrolling = true;
        minX = childImageView.getX() + radius;
        maxX = childImageView.getX() + childImageView.getWidth() - radius;
        minY = childImageView.getY() + radius;
        maxY = childImageView.getY() + childImageView.getHeight() - radius;
    }

    private void onScroll(float distanceX, float distanceY) {
        float startScreenX = centerX + distanceX;
        float startScreenY = centerY + distanceY;
        if (startScreenX < minX) {
            startScreenX = minX;
        }
        else if (startScreenX > maxX) {
            startScreenX = maxX;
        }
        if (startScreenY < minY) {
            startScreenY = minY;
        }
        else if (startScreenY > maxY) {
            startScreenY = maxY;
        }
        centerX = startScreenX;
        centerY = startScreenY;
        invalidate();
    }

    public void setChildImageView(ImageView childImageView) {
        this.childImageView = childImageView;
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
    }

    public void setRadiusMax(float radiusMax) {
        this.radiusMax = radiusMax;
    }

    public Bitmap getCroppedBitmap(Bitmap originalBitmap) {
        int x = (int) (centerX - radius - childImageView.getX());
        int y = (int) (centerY - radius - childImageView.getY());
        int size = (int) (radius * 2);
        return Bitmap.createBitmap(originalBitmap, x, y, size, size);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaleStartDistance = (int) detector.getCurrentSpan();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        int changeSpan = (int) (detector.getCurrentSpan() - scaleStartDistance);
        scaleStartDistance = (int) detector.getCurrentSpan();
        radius += changeSpan;
        // Left Bound
        if (radius > centerX - childImageView.getX()) {
            if (radius < radiusMax) {
                centerX += changeSpan;
            }
            else {
                radius = centerX - childImageView.getX();
            }
        }
        // Right Bound
        else if (radius > childImageView.getWidth() - centerX + childImageView.getX()) {
            if (radius < radiusMax) {
                centerX -= changeSpan;
            }
            else {
                radius = childImageView.getWidth() - centerX + childImageView.getX();
            }
        }
        // Top bound
        else if (radius > centerY - childImageView.getY()) {
            if (radius < radiusMax) {
                centerY += changeSpan;
            }
            else {
                radius = centerY - childImageView.getY();
            }
        }
        // Bottom Bound
        else if (radius > childImageView.getHeight() - centerY + childImageView.getY()) {
            if (radius < radiusMax) {
                centerY -= changeSpan;
            }
            else {
                radius = childImageView.getHeight() - centerY + childImageView.getY();
            }
        }
        if (radius > radiusMax) {
            radius = radiusMax;
        }
        else if (radius < radiusMin) {
            radius = radiusMin;
        }
        minX = childImageView.getX() + radius;
        maxX = childImageView.getX() + childImageView.getWidth() - radius;
        minY = childImageView.getY() + radius;
        maxY = childImageView.getY() + childImageView.getHeight() - radius;
        float startScreenX = centerX;
        float startScreenY = centerY;
        if (startScreenX < minX) {
            startScreenX = minX;
        }
        else if (startScreenX > maxX) {
            startScreenX = maxX;
        }
        if (startScreenY < minY) {
            startScreenY = minY;
        }
        else if (startScreenY > maxY) {
            startScreenY = maxY;
        }
        centerX = startScreenX;
        centerY = startScreenY;
        invalidate();
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

}