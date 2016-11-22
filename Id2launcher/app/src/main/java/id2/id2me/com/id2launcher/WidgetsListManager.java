package id2.id2me.com.id2launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import id2.id2me.com.id2launcher.models.AppInfoModel;
import id2.id2me.com.id2launcher.models.WidgetInfoModel;

/**
 * Created by sunita on 8/2/16.
 */
public class WidgetsListManager {
    Context context;
    static final String TAG = "WidgetsListManager";
    private float mAppIconSize;

    // Used for drawing widget previews
    CanvasCache mCachedAppWidgetPreviewCanvas = new CanvasCache();
    RectCache mCachedAppWidgetPreviewSrcRect = new RectCache();
    RectCache mCachedAppWidgetPreviewDestRect = new RectCache();
    PaintCache mCachedAppWidgetPreviewPaint = new PaintCache();

    private final float sWidgetPreviewIconPaddingPercentage = 0.25f;

   // private IconCache mIconCache;


    public WidgetsListManager(Context context) {
        this.context = context;
        mAppIconSize = context.getResources().getDimension(R.dimen.app_icon_size);
        getInstalledWidgets();
    }

    public ArrayList<WidgetInfoModel> getInstalledWidgets() {

        ArrayList<WidgetInfoModel> widgetInfos = null;
        try {
            widgetInfos = new ArrayList<>();
            PackageManager mPackageManager = context.getPackageManager();
            List<AppWidgetProviderInfo> widgets = AppWidgetManager.getInstance(context).getInstalledProviders();
            Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            List<ResolveInfo> shortcuts = mPackageManager.queryIntentActivities(shortcutsIntent, 0);
            for (AppWidgetProviderInfo widget : widgets) {
                if (widget.minWidth > 0 && widget.minHeight > 0) {
                    int[] spanXY = getSpanForWidget(context, widget.provider, widget.minWidth, widget.minHeight);
                    int[] minSpanXY = getSpanForWidget(context, widget.provider, widget.minResizeWidth, widget.minResizeHeight);

                    int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
                    int minSpanY = Math.min(spanXY[1], minSpanXY[1]);

//                    if (minSpanX <= ((LauncherApplication) ((Activity) context).getApplication()).getCellCountX() &&
//                            minSpanY <=  ((LauncherApplication) ((Activity) context).getApplication()).getCellCountY()) {
                    WidgetInfoModel widgetInfo = new WidgetInfoModel();
                    widgetInfo.setPname(widget.provider.getPackageName());
                    widgetInfo.setSpanX(spanXY[0]);
                    widgetInfo.setSpanY(spanXY[1]);
                    widgetInfo.setComponentName(widget.provider);
                    widgetInfo.setAppWidgetProviderInfo(widget);
                    widgetInfo.setPreview(widget.previewImage);
                    //widgetInfo.setDrawableImage(widget.loadPreviewImage(context,(int)LauncherApplication.getScreenDensity()));
                    //widgetInfo.setWidgetIcon(widget.loadIcon(context,(int)LauncherApplication.getScreenDensity()));
                    widgetInfo.setMinSpanX(minSpanXY[0]);
                    widgetInfo.setMinSpanY(minSpanXY[1]);
                    widgetInfo.setWidgetName(widget.loadLabel(mPackageManager));
                    widgetInfos.add(widgetInfo);
                    //  }

                } else {
                    Log.e(TAG, "Widget " + widget.provider + " can not fit on this device (" +
                            widget.minWidth + ", " + widget.minHeight + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(widgetInfos, new Comparator<WidgetInfoModel>() {
            @Override
            public int compare(WidgetInfoModel widgetInfoModel, WidgetInfoModel t1) {
                return widgetInfoModel.getWidgetName().compareToIgnoreCase(t1.getWidgetName());
            }
        });
        return widgetInfos;
    }


    public Bitmap getWidgetPreview(ComponentName provider, int previewImage, int maxWidth,
                                   int maxHeight, int cellHSpan, int cellVSpan ) {
        // Load the preview image if possible
        String packageName = provider.getPackageName();
        if (maxWidth < 0) maxWidth = Integer.MAX_VALUE;
        if (maxHeight < 0) maxHeight = Integer.MAX_VALUE;
        PackageManager mPackageManager = context.getPackageManager();
        Drawable drawable = null;
        if (previewImage != 0) {
            drawable = mPackageManager.getDrawable(packageName, previewImage, null);
            if (drawable == null) {
                Log.w(TAG, "Can't load widget preview drawable 0x" +
                        Integer.toHexString(previewImage) + " for provider: " + provider);
            }
        }

        int bitmapWidth;
        int bitmapHeight;
        boolean widgetPreviewExists = (drawable != null);
        Bitmap defaultPreview = null;
        Bitmap preview = null;
        try {
            if (widgetPreviewExists) {
                bitmapWidth = drawable.getIntrinsicWidth();
                bitmapHeight = drawable.getIntrinsicHeight();
            } else {
                // Generate a preview image if we couldn't load one
                if (cellHSpan < 1) cellHSpan = 1;
                if (cellVSpan < 1) cellVSpan = 1;

                BitmapDrawable previewDrawable = (BitmapDrawable) context.getResources().getDrawable(R.mipmap.widget_preview_tile);
                final int previewDrawableWidth = previewDrawable
                        .getIntrinsicWidth();
                final int previewDrawableHeight = previewDrawable
                        .getIntrinsicHeight();
                bitmapWidth = previewDrawableWidth * cellHSpan; // subtract 2 dips
                bitmapHeight = previewDrawableHeight * cellVSpan;

                defaultPreview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                        Bitmap.Config.ARGB_8888);
                final Canvas c = mCachedAppWidgetPreviewCanvas.get();
                c.setBitmap(defaultPreview);
                previewDrawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
                previewDrawable.setTileModeXY(Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT);
                previewDrawable.draw(c);
                c.setBitmap(null);

                // Draw the icon in the top left corner
                int minOffset = (int) (mAppIconSize * sWidgetPreviewIconPaddingPercentage);
                int smallestSide = Math.min(bitmapWidth, bitmapHeight);
                float iconScale = Math.min((float) smallestSide
                        / (mAppIconSize + 2 * minOffset), 1f);

                try {
                    Drawable icon = null;
                    int hoffset =
                            (int) ((previewDrawableWidth - mAppIconSize * iconScale) / 2);
                    int yoffset =
                            (int) ((previewDrawableHeight - mAppIconSize * iconScale) / 2);
                    //if (iconId > 0)
                        //icon = mIconCache.getFullResIcon(packageName, iconId);
                    icon = AllAppsList.getIconForWidget(packageName);
                    if (icon != null) {
                        renderDrawableToBitmap(icon, defaultPreview, hoffset,
                                yoffset, (int) (mAppIconSize * iconScale),
                                (int) (mAppIconSize * iconScale));
                    }
                } catch (Resources.NotFoundException e) {
                }
            }


            // Scale to fit width only - let the widget preview be clipped in the
            // vertical dimension
            float scale = 1f;
            if (bitmapWidth > maxWidth) {
                scale = maxWidth / (float) bitmapWidth;
            }
            if (scale != 1f) {
                bitmapWidth = (int) (scale * bitmapWidth);
                bitmapHeight = (int) (scale * bitmapHeight);
            }

            preview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                    Bitmap.Config.ARGB_8888);


            // Draw the scaled preview into the final bitmap
            if (widgetPreviewExists) {
                renderDrawableToBitmap(drawable, preview, 0, 0, bitmapWidth,
                        bitmapHeight);
            } else {
                final Canvas c = mCachedAppWidgetPreviewCanvas.get();
                final Rect src = mCachedAppWidgetPreviewSrcRect.get();
                final Rect dest = mCachedAppWidgetPreviewDestRect.get();
                c.setBitmap(preview);
                src.set(0, 0, defaultPreview.getWidth(), defaultPreview.getHeight());
                dest.set(0, 0, preview.getWidth(), preview.getHeight());

                Paint p = mCachedAppWidgetPreviewPaint.get();
                if (p == null) {
                    p = new Paint();
                    p.setFilterBitmap(true);
                    mCachedAppWidgetPreviewPaint.set(p);
                }
                c.drawBitmap(defaultPreview, src, dest, p);
                c.setBitmap(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preview;
    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h) {
        renderDrawableToBitmap(d, bitmap, x, y, w, h, 1f);
    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h,
                                        float scale) {
        if (bitmap != null) {
            Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            Rect oldBounds = d.copyBounds();
            d.setBounds(x, y, x + w, y + h);
            d.draw(c);
            d.setBounds(oldBounds); // Restore the bounds
            c.setBitmap(null);
        }
    }


    int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
                           int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        int[] xy = rectToCell(requiredWidth, requiredHeight);
        return xy;
    }

    public int[] rectToCell(int width, int height) {
        int[] result = new int[2];
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        int actualWidth = ((LauncherApplication) ((Activity) context).getApplication()).getCellWidth();
        int actualHeight = ((LauncherApplication) ((Activity) context).getApplication()).getCellHeight();
       /* int actualWidth = context.getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        int actualHeight = context.getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);*/
        int smallerSize = Math.min(actualWidth, actualHeight);

        // Always round up to next largest cell
        int spanX = (int) Math.ceil(width / (float) smallerSize);
        int spanY = (int) Math.ceil(height / (float) smallerSize);

        if (result == null) {
            return new int[] { spanX, spanY };
        }
        result[0] = spanX;
        result[1] = spanY;
        return result;
    }

/*
    public int[] rectToCell(int width, int height) {
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        int spanX = 0;
        int spanY = 0;
        try {
            int actualWidth = ((LauncherApplication) ((Activity) context).getApplication()).getCellWidth();
            int actualHeight = ((LauncherApplication) ((Activity) context).getApplication()).getCellHeight();
            // Always round up to next largest cell
            spanX = (int) Math.ceil(width / (float) actualWidth);
            spanY = (int) Math.ceil(height / (float) actualHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new int[]{spanX, spanY};
    }
*/

    abstract class WeakReferenceThreadLocal<T> {
        private ThreadLocal<WeakReference<T>> mThreadLocal;
        public WeakReferenceThreadLocal() {
            mThreadLocal = new ThreadLocal<WeakReference<T>>();
        }

        abstract T initialValue();

        public void set(T t) {
            mThreadLocal.set(new WeakReference<T>(t));
        }

        public T get() {
            WeakReference<T> reference = mThreadLocal.get();
            T obj;
            if (reference == null) {
                obj = initialValue();
                mThreadLocal.set(new WeakReference<T>(obj));
                return obj;
            } else {
                obj = reference.get();
                if (obj == null) {
                    obj = initialValue();
                    mThreadLocal.set(new WeakReference<T>(obj));
                }
                return obj;
            }
        }
    }

    class CanvasCache extends WeakReferenceThreadLocal<Canvas> {
        @Override
        protected Canvas initialValue() {
            return new Canvas();
        }
    }

    class PaintCache extends WeakReferenceThreadLocal<Paint> {
        @Override
        protected Paint initialValue() {
            return null;
        }
    }

    class BitmapCache extends WeakReferenceThreadLocal<Bitmap> {
        @Override
        protected Bitmap initialValue() {
            return null;
        }
    }

    class RectCache extends WeakReferenceThreadLocal<Rect> {
        @Override
        protected Rect initialValue() {
            return new Rect();
        }
    }

}