package id2.id2me.com.id2launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;

/**
 * Created by sunita on 9/8/16.
 */
public class DragView extends View {

    private  Paint mPaint;
    private  Bitmap mBitmap;

    public DragView(Context context, Bitmap bitmap,
                    int left, int top, int width, int height) {
        super(context);

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);

        // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
        int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(ms, ms);
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);


    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    public void setColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        }
        if (color != 0) {
            mPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        } else {
            mPaint.setColorFilter(null);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        mPaint.setAlpha((int) (255 * alpha));
        invalidate();
    }
}
