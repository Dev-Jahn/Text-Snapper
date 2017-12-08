package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import kr.ac.ssu.cse.jahn.textsnapper.R;

/**
 * Created by CypressRH on 2017-12-06.
 */

public class CropView extends View
{
    public static String DEFAULT_BG_COLOR = "A8000000";
    public static String DEFAULT_BORDER_COLOR = "FF000000";
    private static String TAG = CropView.class.getSimpleName();

    private int mWidth;
    private int mHeight;
    private Boolean _drawmode = true;
    private Boolean _editmode = false;

    private WindowManager wm;
    WindowManager.LayoutParams dotParams;

    private Paint mPaint;
    private DashPathEffect dashEffect = new DashPathEffect(new float[]{25,12}, 1);
    private RectF mBackground;
    private CropRect mCropRect;
    private int bgColor;
    private int lineColor;
    private PointF mInitPoint;
    private PointF mCurrPoint;
    private ImageView[] mCropDots;
    private static int DOT_SIZE = 30;

    public CropView(Context context,  int width, int height, WindowManager wm)
    {
        this(context, width, height, DEFAULT_BG_COLOR, DEFAULT_BORDER_COLOR);
        this.wm = wm;
        for (int i=0;i<8;i++)
        {
            //mCropDots[i].setX(mCropRect.vertices[i].x);
            //mCropDots[i].setY(mCropRect.vertices[i].y);
            mCropDots[i].setVisibility(VISIBLE);
            wm.addView(mCropDots[i],dotParams);
        }
    }

    public CropView(Context context, int width, int height, String bg, String line)
    {
        super(context);
        mWidth = width;
        mHeight = height;
        bgColor = encodeColor(bg);
        lineColor = encodeColor(line);
        mPaint = new Paint();
        mInitPoint = new PointF();
        mCurrPoint = new PointF();
        mCropRect = new CropRect();
        mCropDots = new ImageView[8];
        for(int i=0;i<8;i++)
        {
            mCropDots[i] = new ImageView(context);
            mCropDots[i].setVisibility(INVISIBLE);
            mCropDots[i].setImageResource(R.drawable.cropdot);
        }
        dotParams = new WindowManager.LayoutParams(
                DOT_SIZE,DOT_SIZE,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.RGBA_8888);
        dotParams.gravity = Gravity.TOP | Gravity.LEFT;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawColor(bgColor);

        if (true)
        {
            Log.v(TAG, String.valueOf(mCropRect.vertices[0].x)+", "+String.valueOf(mCropRect.vertices[0].y));
            Log.e(TAG, "---------------------------------");
            for (int i=0;i<8;i++)
            {
                switch (i)
                {
                case 0:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2-3;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2-3;
                    break;
                case 1:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2-3;
                    break;
                case 2:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2+4;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2-3;
                    break;
                case 3:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2-3;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2;
                    break;
                case 4:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2+4;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2;
                    break;
                case 5:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2-3;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2;
                    break;
                case 6:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2+4;
                    break;
                case 7:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2+4;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2+4;
                    break;
                case 8:
                    dotParams.x = (int)mCropRect.vertices[i].x-DOT_SIZE/2;
                    dotParams.y = (int)mCropRect.vertices[i].y-DOT_SIZE/2+4;
                    break;
                }

                //mCropDots[i].setX(mCropRect.vertices[i].x);
                //mCropDots[i].setY(mCropRect.vertices[i].y);
                mCropDots[i].setVisibility(VISIBLE);
                wm.updateViewLayout(mCropDots[i], dotParams);
            }
        }

        if (_drawmode||_editmode)
        {
            PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
            mPaint.setXfermode(new PorterDuffXfermode(mode));

            // fill
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawRect(mCropRect, mPaint);

            // border
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(12.0F);
            mPaint.setPathEffect(dashEffect);
            mPaint.setColor(lineColor);
            canvas.drawRect(mCropRect, mPaint);

            mode = PorterDuff.Mode.SRC_OVER;
            mPaint.setXfermode(new PorterDuffXfermode(mode));
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

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            mInitPoint.set(event.getX(),event.getY());
            Log.e(TAG, mInitPoint.toString());
            break;
        case MotionEvent.ACTION_MOVE:
            mCurrPoint.set(event.getX(),event.getY());
            if (_drawmode)
            {
                mCropRect.set(mInitPoint,mCurrPoint);
                invalidate();
            }

            break;
        case MotionEvent.ACTION_UP:
            if (_drawmode)
            {
                for (int i=0;i<8;i++)
                {
                    mCropDots[i].setVisibility(VISIBLE);
                }
                _drawmode = false;

            }
            _editmode = true;
            invalidate();
            break;
        }
        if (_editmode)
        {

        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode==KeyEvent.KEYCODE_BACK)
        {
            if (_editmode)
            {
                _editmode = false;
                invalidate();
            }
            else if (!_drawmode&&!_editmode)
            {
                setVisibility(GONE);
                invalidate();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
