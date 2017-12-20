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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import kr.ac.ssu.cse.jahn.textsnapper.R;

import static android.view.KeyEvent.KEYCODE_APP_SWITCH;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

/**
 * Created by CypressRH on 2017-12-06.
 */

public class CropView extends View
{
    public static String DEFAULT_BG_COLOR = "A8000000";
    public static String DEFAULT_BORDER_COLOR = "FF000000";
    private static String TAG = CropView.class.getSimpleName();

    private int statusbarHeight;
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
    private CropDot[] mCropDots;
    private static int DOT_SIZE = 30;

    /**
     * 초기화를 진행할 생성자
     */
    public CropView(Context context, int width, int height, String bg, String line)
    {
        super(context);
        statusbarHeight = MainActivity.statusbarHeight;
        bgColor = encodeColor(bg);
        lineColor = encodeColor(line);
        mPaint = new Paint();
        mInitPoint = new PointF();
        mCurrPoint = new PointF();
        mCropRect = new CropRect();
        mCropDots = new CropDot[8];
        for(int i=0;i<8;i++)
        {
            mCropDots[i] = new CropDot(context,i);
            mCropDots[i].setVisibility(INVISIBLE);
            mCropDots[i].setImageResource(R.drawable.cropdot);
            mCropDots[i].setOnTouchListener(new CropDotListener());
        }
        dotParams = new WindowManager.LayoutParams(
                DOT_SIZE,DOT_SIZE,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                FLAG_LAYOUT_NO_LIMITS|
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBA_8888);
        dotParams.gravity = Gravity.TOP | Gravity.LEFT;

        requestFocus();
    }

    /**
     * 실제로 호출할 생성자
     */
    public CropView(Context context,  int width, int height, WindowManager wm)
    {
        this(context, width, height, DEFAULT_BG_COLOR, DEFAULT_BORDER_COLOR);
        this.wm = wm;
        for (int i=0;i<8;i++)
        {
            wm.addView(mCropDots[i],dotParams);
        }
    }

    /**
     *  현재 CropRect를 리턴
     *  해당 Rect 위치의 화면을 ImageProjection으로 부터 잘라서 OCR하는데 사용
     */
    public RectF getCurrentRect()
    {
        return mCropRect;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //super.onDraw(canvas);
        canvas.drawColor(bgColor);
        CropRect curRect = new CropRect(mCropRect);
        RectF adjusted = new RectF(curRect.left+4,curRect.top+4,curRect.right-4,curRect.bottom-4);

        if (_drawmode||_editmode)
        {
            for (int i=0;i<8;i++)
            {
                if (i==0||i==3||i==5)
                    dotParams.x = (int)curRect.vertices[i].x-DOT_SIZE/2;
                else if (i==1||i==6)
                    dotParams.x = (int)curRect.vertices[i].x-DOT_SIZE/2;
                else if (i==2||i==4||i==7)
                    dotParams.x = (int)curRect.vertices[i].x-DOT_SIZE/2;

                if (i>=0&&i<=2)
                    dotParams.y = (int)curRect.vertices[i].y-DOT_SIZE/2;
                else if (i>=3&&i<=4)
                    dotParams.y = (int)curRect.vertices[i].y-DOT_SIZE/2;
                else if (i>=5&&i<=7)
                    dotParams.y = (int)curRect.vertices[i].y-DOT_SIZE/2;
                wm.updateViewLayout(mCropDots[i], dotParams);
            }

            PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
            mPaint.setXfermode(new PorterDuffXfermode(mode));

            // fill
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawRect(adjusted, mPaint);

            // border
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(12.0F);
            mPaint.setPathEffect(dashEffect);
            mPaint.setColor(lineColor);
            canvas.drawRect(adjusted, mPaint);

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

    /**
     * 뷰내의 터치이벤트를 처리
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            if (_drawmode)
            {

                mInitPoint.set(event.getX(),event.getY());
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (_drawmode)
            {
                for (int i=0;i<8;i++)
                    mCropDots[i].setVisibility(VISIBLE);
                mCurrPoint.set(event.getX(),event.getY());
                mCropRect.set(mInitPoint,mCurrPoint);
                invalidate();
            }
            break;
        case MotionEvent.ACTION_UP:
            if (_drawmode)
            {
                _drawmode = false;
                dotParams.flags = FLAG_LAYOUT_NO_LIMITS|FLAG_NOT_FOCUSABLE;
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
    /**
     * 뷰내의 키이벤트를 처리
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
        case KEYCODE_HOME:
        case KEYCODE_APP_SWITCH:
        case KEYCODE_BACK:
            collapse();
        }
        return super.onKeyDown(keyCode, event);
    }



    /**
     * 이미지뷰 인덱싱을 위해 재정의
     */
    private class CropDot extends android.support.v7.widget.AppCompatImageView
    {
        protected int index;

        public CropDot(Context context)
        {
            super(context);
        }

        public CropDot(Context context, int i)
        {
            this(context);
            index = i;
        }
    }
    /**
     * CropDot 이미지 뷰의 터치이벤트를 처리
     */
    private class CropDotListener implements OnTouchListener
    {
        CropDot selected;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            //Log.e(TAG, String.valueOf(((CropDot)v).index)+"번 꼭지점 터치");
            //Log.e(TAG, " ");
            switch (event.getAction())
            {
            case MotionEvent.ACTION_DOWN:
                selected = (CropDot)v;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (selected==v)
                {
                    mCropRect.grow(CropRect.Position.values()[selected.index],new PointF(event.getRawX(), event.getRawY()-statusbarHeight));
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                selected = null;
                return true;
            }
            return false;
        }
    }

    public void collapse()
    {
        if (mCropDots[0]!=null)
            for (int i=0;i<8;i++)
                mCropDots[i].setVisibility(GONE);
        setVisibility(GONE);
        invalidate();
        FloatingService.set_cropmode(false);
    }
}
