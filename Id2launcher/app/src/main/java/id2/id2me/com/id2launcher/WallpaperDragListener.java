package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by sunita on 9/4/16.
 */
public class WallpaperDragListener implements View.OnDragListener {
    PageDragListener pageDragListener;
    final String TAG = "WallpaperDragListener";
    View removeLayout, uninstallLayout;
    Rect removeRect, uninstallRect;
    Context context;
    LauncherApplication launcherApplication;
    Drawable drawable;

    public WallpaperDragListener(Context context, PageDragListener pageDragListener, View removeLayout, View uninstallLayout) {
        this.context = context;
        this.pageDragListener = pageDragListener;
        this.removeLayout = removeLayout;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        this.uninstallLayout = uninstallLayout;
        setRemoveAndUninstallRect();

    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //   Log.v(TAG, "Drag Started");
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //  Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                // Log.v(TAG, "Drag EXITED");
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {
                    Log.v(TAG, "Drag locating  :: x :: y :: " + (int) event.getX() + "   " + (int) event.getY());


                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DROP:
                //  Log.v(TAG, "DROP Action");
                checkToRemoveUninstall((int) event.getX(), (int) event.getY(), v);
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                // Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }

    private void checkToRemoveUninstall(int x, int y, View view) {
        try {

            if (removeRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
                Log.v(TAG, "remove");
                pageDragListener.removeViewFromDesktop();

            } else if (uninstallRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
                Log.v(TAG, "uninstall");
                pageDragListener.dropOutOfTheBox();

            } else {
                Log.v(TAG, "else");
                pageDragListener.dropOutOfTheBox();

            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
        Bitmap b;
        b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);

        return b;
    }


    void setRemoveAndUninstallRect() {


        removeLayout.post(new Runnable() {
            @Override
            public void run() {
                int[] l = new int[2];
                removeLayout.getLocationOnScreen(l);
                removeRect = new Rect(l[0], l[1], l[0] + removeLayout.getWidth(), l[1] + removeLayout.getHeight());

            }
        });

        uninstallLayout.post(new Runnable() {
            @Override
            public void run() {
                int[] l = new int[2];
                uninstallLayout.getLocationOnScreen(l);
                uninstallRect = new Rect(l[0], l[1], l[0] + uninstallLayout.getWidth(), l[1] + uninstallLayout.getHeight());

            }
        });


    }

}


