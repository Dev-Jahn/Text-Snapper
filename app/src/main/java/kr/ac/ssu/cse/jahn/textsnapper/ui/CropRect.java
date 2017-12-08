package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.BOTTOM;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.BOTTOM_LEFT;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.BOTTOM_RIGHT;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.LEFT;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.RIGHT;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.TOP;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.TOP_LEFT;
import static kr.ac.ssu.cse.jahn.textsnapper.ui.CropRect.Position.TOP_RIGHT;

/**
 * Created by CypressRH on 2017-12-02.
 */

public class CropRect extends RectF
{
    private final static String TAG = CropRect.class.getSimpleName();
    public enum Position
    {
        TOP_LEFT, TOP, TOP_RIGHT, LEFT, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    public PointF[] vertices = new PointF[8];;

    public CropRect(RectF rect)
    {
        super(rect);
        vertices[TOP_LEFT.ordinal()] = new PointF(rect.left, rect.top);
        vertices[TOP.ordinal()] = new PointF(rect.centerX(), rect.top);
        vertices[TOP_RIGHT.ordinal()] = new PointF(rect.right, rect.top);
        vertices[LEFT.ordinal()] = new PointF(rect.left, rect.centerY());
        vertices[RIGHT.ordinal()] = new PointF(rect.right, rect.centerY());
        vertices[BOTTOM_LEFT.ordinal()] = new PointF(rect.left, rect.bottom);
        vertices[BOTTOM.ordinal()] = new PointF(rect.centerX(), rect.bottom);
        vertices[BOTTOM_RIGHT.ordinal()] = new PointF(rect.right, rect.bottom);
    }

    public CropRect(CropRect cr)
    {
        this.left = cr.left;
        this.right = cr.right;
        this.top = cr.top;
        this.bottom = cr.bottom;

        for (int i=0;i<8;i++)
            this.vertices[i] = cr.vertices[i];
    }

    public CropRect()
    {
        this(0,0,0,0);
    }

    public CropRect(Rect r)
    {
        this(new RectF(r));
    }

    public CropRect(float left, float top, float right, float bottom)
    {
        this(new RectF(left,top,right,bottom));
    }

    public void set(PointF topleft, PointF bottomright)
    {
        RectF rect = new RectF(topleft.x,topleft.y,bottomright.x,bottomright.y);
        super.set(rect);

        vertices[TOP_LEFT.ordinal()].set(rect.left, rect.top);
        vertices[TOP.ordinal()].set(rect.centerX(), rect.top);
        vertices[TOP_RIGHT.ordinal()].set(rect.right, rect.top);
        vertices[LEFT.ordinal()].set(rect.left, rect.centerY());
        vertices[RIGHT.ordinal()].set(rect.right, rect.centerY());
        vertices[BOTTOM_LEFT.ordinal()].set(rect.left, rect.bottom);
        vertices[BOTTOM.ordinal()].set(rect.centerX(), rect.bottom);
        vertices[BOTTOM_RIGHT.ordinal()].set(rect.right, rect.bottom);
    }

    public void grow(Position pos, PointF p)
    {
        if (pos==TOP_LEFT||pos==TOP||pos==TOP_RIGHT)
            this.top = p.y;
        else if (pos==BOTTOM_LEFT||pos==BOTTOM||pos==BOTTOM_RIGHT)
            this.bottom = p.y;

        if (pos==TOP_LEFT||pos==LEFT||pos==BOTTOM_LEFT)
            this.left = p.x;
        else if (pos==TOP_RIGHT||pos==RIGHT||pos==BOTTOM_RIGHT)
            this.right = p.x;

        vertices[TOP_LEFT.ordinal()].set(this.left, this.top);
        vertices[TOP.ordinal()].set(this.centerX(), this.top);
        vertices[TOP_RIGHT.ordinal()].set(this.right, this.top);
        vertices[LEFT.ordinal()].set(this.left, this.centerY());
        vertices[RIGHT.ordinal()].set(this.right, this.centerY());
        vertices[BOTTOM_LEFT.ordinal()].set(this.left, this.bottom);
        vertices[BOTTOM.ordinal()].set(this.centerX(), this.bottom);
        vertices[BOTTOM_RIGHT.ordinal()].set(this.right, this.bottom);
    }
}
