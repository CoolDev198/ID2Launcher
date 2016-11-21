package id2.id2me.com.id2launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by bliss76 on 27/05/16.
 */
public class AppGridView extends GridView
{
    Context mContext;

    public AppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AppGridView(Context context)
    {
        super(context);
    }

    public AppGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



}
