package id2.id2me.com.id2launcher.listingviews;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.WidgetRecycleViewAdapter;


/**
 * Created by sunita on 8/2/16.
 */
public class WidgetsListingView extends ListingContainerView {

    Context context;
    private WidgetRecycleViewAdapter widgetRecycleViewAdapter;

    public WidgetsListingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        GridLayoutManager manager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        widgetRecycleViewAdapter=new WidgetRecycleViewAdapter(this);
        recyclerView.setAdapter(widgetRecycleViewAdapter);
    }


}
