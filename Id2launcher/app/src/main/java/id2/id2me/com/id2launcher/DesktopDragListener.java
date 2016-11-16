package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by sunita on 11/16/16.
 */

public class DesktopDragListener implements View.OnDragListener {

    String TAG = "DesktopDragListener";

    View desktopFragment;
    LauncherApplication launcherApplication;
    Context context;

    public DesktopDragListener(Context context, View fragmentView) {

        this.context = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
        desktopFragment = fragmentView;
    }


    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                try {
                    Log.v(TAG, "DRAG_STARTED");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                Log.v(TAG, "Drag Entered");
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                Log.v(TAG, "Drag EXITED");
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                try {
                    Log.v(TAG, "Drag locating");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DROP:
                Log.v(TAG, "DROP Action");
                try {
                    onDrop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.v(TAG, "Drag ENDED");
                return true;

            default:
                break;
        }
        return true;
    }

    private void onDrop() {
        try {
            if (launcherApplication.isDragStarted) {
                launcherApplication.removeMargin();
                launcherApplication.isDragStarted = true;
                LinearLayout containerL = (LinearLayout) desktopFragment.findViewById(R.id.container);
                if (launcherApplication.dragInfo.getDropExternal()) {
                    launcherApplication.dragInfo.setIsItemCanPlaced(false);
                    PageDragListener pageDragListener = ((CellLayout) containerL.getChildAt(1)).getDragListener();

                    pageDragListener.onDropOutOfCellLayout();
                    Toast.makeText(context, context.getResources().getString(R.string.invalid_alert), Toast.LENGTH_LONG).show();
                } else {
                    PageDragListener pageDragListener = ((CellLayout) containerL.getChildAt(launcherApplication.dragInfo.getScreen())).getDragListener();

                    pageDragListener.onDrop();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
