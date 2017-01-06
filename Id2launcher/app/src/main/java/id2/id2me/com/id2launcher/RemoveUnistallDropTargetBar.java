package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by CrazyInnoTech on 05-01-2017.
 */

public class RemoveUnistallDropTargetBar extends FrameLayout implements DragController.DragListener {

    private ButtonDropTarget mInfoDropTarget;
    private ButtonDropTarget mDeleteDropTarget;


    public RemoveUnistallDropTargetBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoveUnistallDropTargetBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setup(Launcher launcher, DragController dragController) {
        //dragController.addDragListener(this);
        /*dragController.addDragListener(mInfoDropTarget);
        dragController.addDragListener(mDeleteDropTarget);*/
        dragController.addDropTarget(mInfoDropTarget);
        dragController.addDropTarget(mDeleteDropTarget);
        //dragController.setFlingToDeleteDropTarget(mDeleteDropTarget);
        /*mInfoDropTarget.setLauncher(launcher);
        mDeleteDropTarget.setLauncher(launcher);*/
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the individual components

        mInfoDropTarget = (ButtonDropTarget) findViewById(R.id.info_target_text);
        mDeleteDropTarget = (ButtonDropTarget) findViewById(R.id.remove_target_text);

    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }
}
