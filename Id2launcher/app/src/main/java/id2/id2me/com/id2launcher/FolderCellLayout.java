package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by sunita on 1/19/17.
 */

public class FolderCellLayout extends CellLayout {
    public FolderCellLayout(Context context) {
        this(context, null);
    }

    public FolderCellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderCellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCountY = getResources().getInteger(R.integer.folder_max_count_y);

    }
}
