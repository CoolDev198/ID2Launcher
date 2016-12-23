package id2.id2me.com.id2launcher;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by sunita on 12/21/16.
 */

public class WallpaperContainer extends RelativeLayout implements DropTarget {
    Launcher mLauncher;

    public WallpaperContainer(Context context) {
        super(context);
    }

    public WallpaperContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WallpaperContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WallpaperContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        mLauncher = launcherApplication.getLauncher();
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {

    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return false;
    }

    @Override
    public void getCustomHitRect(Rect outRect) {
        getLocalVisibleRect(outRect);
        if(mLauncher.getScrollView()!=null) {
          int scrollY =   mLauncher.getScrollView().getScrollY();
            outRect.bottom = outRect.bottom - scrollY;
        }

    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }
}
