package id2.id2me.com.id2launcher;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import id2.id2me.com.id2launcher.LauncherModel;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.itemviews.WidgetImageView;

/**
 * Created by CrazyInnoTech on 15-12-2016.s
 */

public class WidgetItemView extends LinearLayout {
    private static boolean sDeletePreviewsWhenDetachedFromWindow = true;
    private final Rect mOriginalImagePadding = new Rect();
    boolean mIsAppWidget;
    private String mDimensionsFormatString;

    public WidgetItemView(Context context) {
        this(context, null);
    }

    public WidgetItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public WidgetItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources r = context.getResources();
        mDimensionsFormatString = r.getString(R.string.widget_dims_format);
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

    public static void setDeletePreviewsWhenDetachedFromWindow(boolean value) {
        sDeletePreviewsWhenDetachedFromWindow = value;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /*if (sDeletePreviewsWhenDetachedFromWindow) {
            final ImageView image = (ImageView) findViewById(R.id.widget_preview);
            if (image != null) {
                FastBitmapDrawable preview = (FastBitmapDrawable) image.getDrawable();
                if (preview != null && preview.getBitmap() != null) {
                    preview.getBitmap().recycle();
                }
                image.setImageDrawable(null);
            }
        }*/
    }

    public void applyFromAppWidgetProviderInfo(AppWidgetProviderInfo info,
                                               int maxWidth, int[] cellSpan) {
        LauncherApplication launcherApplication = LauncherApplication.getApp();
        mIsAppWidget = true;
        final ImageView image = (ImageView) findViewById(R.id.widget_preview);
        if (maxWidth > -1) {
            image.setMaxWidth(maxWidth);
        }
        //image.setContentDescription(info.label);
        final TextView name = (TextView) findViewById(R.id.widget_name);
        name.setText(info.label);
        final TextView dims = (TextView) findViewById(R.id.widget_dims);
        if (dims != null) {
            int hSpan = Math.min(cellSpan[0], launcherApplication.CELL_COUNT_X);
            int vSpan = Math.min(cellSpan[1], launcherApplication.CELL_COUNT_Y);
            dims.setText(String.format(mDimensionsFormatString, hSpan, vSpan));
        }
    }

    public void applyFromResolveInfo(PackageManager pm, ResolveInfo info) {
        mIsAppWidget = false;
        CharSequence label = info.loadLabel(pm);
        final ImageView image = (ImageView) findViewById(R.id.widget_preview);
        //image.setContentDescription(label);
        final TextView name = (TextView) findViewById(R.id.widget_name);
        name.setText(label);
        final TextView dims = (TextView) findViewById(R.id.widget_dims);
        if (dims != null) {
            dims.setText(String.format(mDimensionsFormatString, 1, 1));
        }
    }

    public int[] getPreviewSize() {
        final ImageView i = (ImageView) findViewById(R.id.widget_preview);
        int[] maxSize = new int[2];
        maxSize[0] = i.getWidth() - mOriginalImagePadding.left - mOriginalImagePadding.right;
        maxSize[1] = i.getHeight() - mOriginalImagePadding.top;
        return maxSize;
    }

    void applyPreview(FastBitmapDrawable preview, int index) {
        final WidgetImageView image =
                (WidgetImageView) findViewById(R.id.widget_preview);
        if (preview != null) {
            image.mAllowRequestLayout = false;
            image.setImageDrawable(preview);
            if (mIsAppWidget) {
                // center horizontally
                int[] imageSize = getPreviewSize();
                int centerAmount = (imageSize[0] - preview.getIntrinsicWidth()) / 2;
                image.setPadding(mOriginalImagePadding.left + centerAmount,
                        mOriginalImagePadding.top,
                        mOriginalImagePadding.right,
                        mOriginalImagePadding.bottom);
            }
            image.setAlpha(1f);
            image.mAllowRequestLayout = true;
        }
    }


}
