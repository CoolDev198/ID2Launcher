package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by CrazyInnoTech on 05-01-2017.
 */

public class RemoveUnistallDropTargetBar extends FrameLayout implements DragController.DragListener {

    public RemoveUnistallDropTargetBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoveUnistallDropTargetBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }
}
