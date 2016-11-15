package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by sunita on 8/23/16.
 */

public class ObservableScrollView extends ScrollView {

    private ScrollViewListener scrollViewListener = null;
    String TAG = "ObservableScrollView";
    Context context;
    LauncherApplication launcherApplication;

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    public interface ScrollViewListener {
        void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        launcherApplication = (LauncherApplication) ((Activity) context).getApplication();
    }

}

