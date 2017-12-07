package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by CypressRH on 2017-12-06.
 */

public class CropView extends RelativeLayout
{
    private int mWidth;
    private int mHeight;
    private Boolean _drawmode = false;

    private Paint mPaint;
    private RectF mBackground;
    private RectF mCropRect;
    private int bgColor;
    private int lineColor;


    public CropView(Context context, int width, int height, String bg, String line)
    {
        super(context);
        mWidth = width;
        mHeight = height;
        bgColor = encodeColor(bg);
        lineColor = encodeColor(line);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        /*
        mPaint.setColor(bgColor);
        canvas.drawRect(mBackground,mPaint);
        */
        canvas.drawColor(bgColor);

        if (_drawmode)
        {
            PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
            mPaint.setXfermode(new PorterDuffXfermode(mode));

            // fill
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawRect(mCropRect, mPaint);

            // border
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(lineColor);
            canvas.drawRect(mCropRect, mPaint);
        }
    }

    private int encodeColor(String str)
    {
        int A,R,G,B;
        A = Integer.valueOf(str.substring(0,2),16);
        R = Integer.valueOf(str.substring(2,4),16);
        G = Integer.valueOf(str.substring(4,6),16);
        B = Integer.valueOf(str.substring(6,8),16);
        return Color.argb(A,R,G,B);
    }

    private Bitmap makeBitmap(int width, int height, String color)
    {
        int[] colors = new int[width*height];
        int colorInt = encodeColor(color);
        for (int i=0;i<width*height;i++)
            colors[i] = colorInt;
        return Bitmap.createBitmap(colors,width,height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            _drawmode = true;
            break;
        case MotionEvent.ACTION_MOVE:

            break;
        case MotionEvent.ACTION_UP:
            _drawmode = false;
            break;
        }
        return super.onTouchEvent(event);
    }
}
