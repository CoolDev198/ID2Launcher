package id2.id2me.com.id2launcher.listingviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import id2.id2me.com.id2launcher.DragSource;
import id2.id2me.com.id2launcher.DropTarget;
import id2.id2me.com.id2launcher.Launcher;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.itemviews.AppItemView;
import id2.id2me.com.id2launcher.models.AppInfo;
import timber.log.Timber;

/**
 * Created by sunita on 12/16/16.
 */

public class ListingContainerView extends FrameLayout implements View.OnLongClickListener, View.OnClickListener, DragSource {
    public ListingContainerView(Context context) {
        super(context);
    }

    public ListingContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListingContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View child) {
        Timber.v("On CLick");
        if (child instanceof AppItemView) {
            final AppInfo appInfo = (AppInfo) child.getTag();
            Launcher mLauncher = LauncherApplication.getApp().getLauncher();
            mLauncher.startActivitySafely(child,appInfo.intent,appInfo);
        } else {
            beginDraggingWidget();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onLongClick(View child) {
        Timber.v("On Long Click");
        if (child instanceof AppItemView) {
            beginDraggingApplication(child);
        } else {
            beginDraggingWidget();
        }


        return false;
    }

    private void beginDraggingWidget() {

    }

    private void beginDraggingApplication(View child) {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        launcherApplication.getLauncher().resetPage();
        launcherApplication.getLauncher().getWokSpace().beginDragShared(child, this);
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {

    }
}
