package id2.id2me.com.id2launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.TextView;

import timber.log.Timber;

/**
 * Created by CrazyInnoTech on 05-01-2017.
 */

public class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener {
    private ButtonDropTarget mDeleteDropTarget;
    protected final int mTransitionDuration;
    private int mBottomDragPadding;

    private Launcher launcher;
    private Point rectPoint = new Point();
    /** Whether this drop target is active for the current drag */
    protected boolean mActive = true;

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

        //rectPoint.set(this.getWidth(),this.getHeight());

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //mDeleteDropTarget = (ButtonDropTarget) findViewById(R.id.remove_target_text);
        Timber.v("Drop target Rect X : " + this.getX() + " Y : " + this.getY());
        Timber.v("Drop target Rect Width  W : " + this.getWidth() + " H : " + this.getHeight());
        Timber.v("Drop target Rect Width  R : " + this.getRight() + " B : " + this.getBottom());

        /*Timber.v("Drop target Rect Width  DR : " + mDeleteDropTarget.getRight() + " DB : " + mDeleteDropTarget.getBottom());
        Timber.v("Drop target Rect DX : " + mDeleteDropTarget.getX() + " DY : " + mDeleteDropTarget.getY());
        Timber.v("Drop target Rect Width  DW : " + mDeleteDropTarget.getWidth() + " DH : " + mDeleteDropTarget.getHeight());*/

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
        this.getHitRect(outRect);
        //outRect.bottom += mBottomDragPadding;
        //outRect.set(0, 0, rectPoint.x, rectPoint.y);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    protected Drawable getCurrentDrawable() {
        Drawable[] drawables = getCompoundDrawables();
        for (int i = 0; i < drawables.length; ++i) {
            if (drawables[i] != null) {
                return drawables[i];
            }
        }
        return null;
    }

    Rect getIconRect(int itemWidth, int itemHeight, int drawableWidth, int drawableHeight) {
        DragLayer dragLayer = mLauncher.getDragLayer();

        // Find the rect to animate to (the view is center aligned)
        Rect to = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, to);
        int width = drawableWidth;
        int height = drawableHeight;
        int left = to.left + getPaddingLeft();
        int top = to.top + (getMeasuredHeight() - height) / 2;
        to.set(left, top, left + width, top + height);

        // Center the destination rect about the trash icon
        int xOffset = (int) -(itemWidth - width) / 2;
        int yOffset = (int) -(itemHeight - height) / 2;
        to.offset(xOffset, yOffset);

        return to;
    }

}
