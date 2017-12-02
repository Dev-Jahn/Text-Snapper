package kr.ac.ssu.cse.jahn.textsnapper.ocr;

import android.graphics.PointF;
import android.graphics.RectF;

import static kr.ac.ssu.cse.jahn.textsnapper.ocr.CroppingRect.Position.*;

/**
 * Created by CypressRH on 2017-12-02.
 */

public class CroppingRect
{
    private final static String TAG = CroppingRect.class.getSimpleName();
    public enum Position
    {
        TOP_LEFT, TOP, TOP_RIGHT, LEFT, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }
    private PointF mPoints[];

    public CroppingRect(RectF cropRect)
    {
        mPoints[TOP_LEFT.ordinal()] = new PointF(cropRect.left, cropRect.top);
        mPoints[TOP.ordinal()] = new PointF(cropRect.centerX(), cropRect.top);
        mPoints[TOP_RIGHT.ordinal()] = new PointF(cropRect.right, cropRect.top);
        mPoints[LEFT.ordinal()] = new PointF(cropRect.left, cropRect.centerY());
        mPoints[RIGHT.ordinal()] = new PointF(cropRect.right, cropRect.centerY());
        mPoints[BOTTOM_LEFT.ordinal()] = new PointF(cropRect.left, cropRect.bottom);
        mPoints[BOTTOM.ordinal()] = new PointF(cropRect.centerX(), cropRect.bottom);
        mPoints[BOTTOM_RIGHT.ordinal()] = new PointF(cropRect.right, cropRect.bottom);
    }
}
