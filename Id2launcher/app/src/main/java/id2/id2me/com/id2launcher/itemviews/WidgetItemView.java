package id2.id2me.com.id2launcher.itemviews;

import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;
import android.widget.LinearLayout;

import id2.id2me.com.id2launcher.R;

/**
 * Created by CrazyInnoTech on 15-12-2016.
 */

public class WidgetItemView extends LinearLayout {
    private final Rect mOriginalImagePadding = new Rect();
    public WidgetItemView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final ImageView image = (ImageView) findViewById(R.id.widget_preview);
        mOriginalImagePadding.left = image.getPaddingLeft();
        mOriginalImagePadding.top = image.getPaddingTop();
        mOriginalImagePadding.right = image.getPaddingRight();
        mOriginalImagePadding.bottom = image.getPaddingBottom();
    }
}
