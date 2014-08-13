package com.robert.uitracker.app;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MySurfaceView extends ImageView {

  float[] points = new float[10000];//constant should be fixed
  int currentPoint = 0;
  Paint mPaint = new Paint(Color.BLACK);
  public boolean hidePoints = false;

  public MySurfaceView(Context c) {//this is the constructor that actually gets used
    super(c);
    mPaint.setStrokeWidth(5f);
  }

  public MySurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaint.setStrokeWidth(5f);
  }

  public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mPaint.setStrokeWidth(5f);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if(!hidePoints) canvas.drawPoints(points,mPaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if(event.getAction() == MotionEvent.ACTION_DOWN) {
      points[currentPoint++] = event.getX();
      points[currentPoint++] = event.getY();
      invalidate();
    }
    return true;
  } // end of touch events for image

  public void clearTouches(){
    points = new float[1000];
    invalidate();
  }

  public void invalidateme(){
    invalidate();
  }

}//end of class