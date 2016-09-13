package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.net.Uri;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by sunita on 9/4/16.
 */
public class WallpaperDragListener implements View.OnDragListener {
    PageDragListener pageDragListener;
    final String TAG = "WallpaperDragListener";
    RelativeLayout removeLayout, uninstallLayout;
    Rect removeRect, uninstallRect;
    Context context;
    LauncherApplication launcherApplication;
    Drawable drawable;

    public WallpaperDragListener(Context context, PageDragListener pageDragListener, View removeLayout, View uninstallLayout) {
        this.context = context;
        this.pageDragListener = pageDragListener;
        this.removeLayout = (RelativeLayout) removeLayout;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        this.uninstallLayout = (RelativeLayout) uninstallLayout;
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

                    highLightHover((int) event.getX(), (int) event.getY());
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

    private void highLightHover(int x, int y) {
        if (removeRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
            Log.v(TAG, "remove");
            uninstallLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) uninstallLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_trashcan_normal_holo);
            removeLayout.setBackgroundColor(Color.BLACK);
            ((ImageView) removeLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_clear_active_holo);

        } else if (uninstallRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
            Log.v(TAG, "uninstall");
            removeLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) removeLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_clear_normal_holo);

            uninstallLayout.setBackgroundColor(Color.BLACK);
            ((ImageView) uninstallLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_trashcan_active_holo);
        } else {
            removeLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) removeLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_clear_normal_holo);

            uninstallLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) uninstallLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_trashcan_normal_holo);

            Log.v(TAG, "else");

        }
    }

    private void checkToRemoveUninstall(int x, int y, View view) {
        try {


            removeLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) removeLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_clear_normal_holo);


            uninstallLayout.setBackgroundColor(Color.TRANSPARENT);
            ((ImageView) uninstallLayout.getChildAt(0)).setImageResource(R.mipmap.ic_launcher_trashcan_normal_holo);

            if (removeRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
                Log.v(TAG, "remove");
                pageDragListener.removeViewFromDesktop();

            } else if (uninstallRect.contains(x, y) && !launcherApplication.dragInfo.getDropExternal()) {
                Log.v(TAG, "uninstall");

                pageDragListener.dropOutOfTheBox();
                // }
            } else {
                Log.v(TAG, "else");
                pageDragListener.dropOutOfTheBox();

            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void uninstallApp() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:com.example.mypackage"));
        context.startActivity(intent);
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


