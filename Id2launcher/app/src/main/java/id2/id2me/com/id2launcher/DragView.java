package id2.id2me.com.id2launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by sunita on 11/23/16.
 */

public class DragView extends ImageView {


    String TAG = "DragView";

    boolean isLongClick=false;
    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public DragView(Context context) {
        super(context);
    }


    public void setBitmap(Bitmap bitmap , int w , int h ){
        setImageBitmap(bitmap);
        setLayoutParams(new FrameLayout.LayoutParams(w,h));

    }

    public void setBitmap(Bitmap bitmap){
        setImageBitmap(bitmap);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG," on touch event");
        return super.onTouchEvent(event);
    }
}
