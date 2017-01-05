package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by CrazyInnoTech on 05-01-2017.
 */

public class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener {
    protected final int mTransitionDuration;
    private int mBottomDragPadding;

    /** Whether this drop target is active for the current drag */
    protected boolean mActive;

    /** The paint applied to the drag view on hover */
    protected int mHoverColor = 0;

    protected Launcher mLauncher;

    public ButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources r = getResources();
        mTransitionDuration = r.getInteger(R.integer.config_dropTargetBgTransitionDuration);
        mBottomDragPadding = r.getDimensionPixelSize(R.dimen.drop_target_drag_padding);
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        // Nothing to Do
    }

    @Override
    public void onDragEnd() {
        // Nothing to Do
    }

    @Override
    public boolean isDropEnabled() {
        return mActive;
    }

    @Override
    public void onDrop(DragObject dragObject) {

    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        dragObject.dragView.setColor(mHoverColor);
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        //Nothing To Do
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        dragObject.dragView.setColor(0);
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
        //Nothing To Do
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
        outRect.bottom += mBottomDragPadding;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }
}
