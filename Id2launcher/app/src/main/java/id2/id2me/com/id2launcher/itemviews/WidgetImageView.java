package id2.id2me.com.id2launcher.itemviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by CrazyInnoTech on 15-12-2016.
 */

public class WidgetImageView extends ImageView {
    public boolean mAllowRequestLayout = true;

    public WidgetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void requestLayout() {
        if (mAllowRequestLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getScrollX() + getPaddingLeft(),
                getScrollY() + getPaddingTop(),
                getScrollX() + getRight() - getLeft() - getPaddingRight(),
                getScrollY() + getBottom() - getTop() - getPaddingBottom());

        super.onDraw(canvas);
        canvas.restore();

    }
}

