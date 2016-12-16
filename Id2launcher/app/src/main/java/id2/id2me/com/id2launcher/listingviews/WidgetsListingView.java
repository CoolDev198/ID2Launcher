package id2.id2me.com.id2launcher.listingviews;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.WidgetRecycleViewAdapter;


/**
 * Created by sunita on 8/2/16.
 */
public class WidgetsListingView extends FrameLayout {

    Context context;
    private RecyclerView recyclerView;

    public WidgetsListingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        recyclerView = (RecyclerView) findViewById(R.id.widget_recycle_view);
        GridLayoutManager manager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new WidgetRecycleViewAdapter(context,recyclerView));
    }
}
