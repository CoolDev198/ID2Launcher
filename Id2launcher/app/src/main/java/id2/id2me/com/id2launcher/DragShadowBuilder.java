package id2.id2me.com.id2launcher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by CrazyInnoTech on 31-10-2016.
 */

public class DragShadowBuilder extends View.DragShadowBuilder {

    View v;
    private Point lastTouch;

    public DragShadowBuilder(View v, Point lastTouch) {
        super(v);
        this.v = v;
        this.lastTouch = lastTouch;
    }

    public DragShadowBuilder(View v) {
        super(v);
    }

    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
        super.onProvideShadowMetrics(size, touch);

        // The touch point must be set in order for the canvas to properly fit the view
        if (lastTouch != null) {
            touch.set(lastTouch.x, lastTouch.y);
        }
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        super.onDrawShadow(canvas);
        //canvas.drawColor(Color.CYAN);
        canvas.drawBitmap(getBitmapFromView(v), 0, 0, null);
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Matrix m = new Matrix();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
      //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);

        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.TRANSPARENT);
        // draw the view on the canvas
        view.draw(canvas);

        //return the bitmap
        return returnedBitmap;
    }

}
