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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.WidgetInfo;

/**
 * Created by sunita on 8/2/16.
 */
public class WidgetsListManager {
    Context context;
    static final String TAG = "WidgetsListManager";


    public WidgetsListManager(Context context) {
        this.context = context;
        getInstalledWidgets();
    }

    public ArrayList<WidgetInfo> getInstalledWidgets() {

        ArrayList<WidgetInfo> widgetInfos = null;
        try {
            widgetInfos = new ArrayList<>();
            PackageManager mPackageManager = context.getPackageManager();
            List<AppWidgetProviderInfo> widgets =
                    AppWidgetManager.getInstance(context).getInstalledProviders();
            Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            List<ResolveInfo> shortcuts = mPackageManager.queryIntentActivities(shortcutsIntent, 0);
            for (AppWidgetProviderInfo widget : widgets) {
                if (widget.minWidth > 0 && widget.minHeight > 0) {

                    int[] spanXY = getSpanForWidget(context, widget.provider, widget.minWidth, widget.minHeight);
                    int[] minSpanXY = getSpanForWidget(context, widget.provider, widget.minResizeWidth, widget.minResizeHeight);

                    int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
                    int minSpanY = Math.min(spanXY[1], minSpanXY[1]);

                    if (minSpanX <= ((LauncherApplication) ((Activity) context).getApplication()).getCellCountX() &&
                            minSpanY <=  ((LauncherApplication) ((Activity) context).getApplication()).getCellCountY()) {
                        WidgetInfo widgetInfo = new WidgetInfo();
                        widgetInfo.setPname(widget.provider.getPackageName());
                        widgetInfo.setSpanX(spanXY[0]);
                        widgetInfo.setSpanY(spanXY[1]);
                        widgetInfo.setComponentName(widget.provider);
                        widgetInfo.setAppWidgetProviderInfo(widget);
                        widgetInfo.setPreview(widget.previewImage);
                        widgetInfo.setMinSpanX(minSpanXY[0]);
                        widgetInfo.setMinSpanY(minSpanXY[1]);
                        widgetInfo.setWidgetName(widget.loadLabel(mPackageManager));
                        widgetInfos.add(widgetInfo);
                    }

                } else {
                    Log.e(TAG, "Widget " + widget.provider + " can not fit on this device (" +
                            widget.minWidth + ", " + widget.minHeight + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return widgetInfos;
    }


    public Bitmap getWidgetPreview(ComponentName provider, int previewImage, int maxWidth,
                                    int maxHeight) {
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

        int bitmapWidth = 0;
        int bitmapHeight = 0;
        boolean widgetPreviewExists = (drawable != null);
        Bitmap preview=null;
        try {
            if (widgetPreviewExists) {
                bitmapWidth = drawable.getIntrinsicWidth();
                bitmapHeight = drawable.getIntrinsicHeight();

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
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        // Draw the scaled preview into the final bitmap
        if (widgetPreviewExists) {
            renderDrawableToBitmap(drawable, preview, 0, 0, bitmapWidth,
                    bitmapHeight);
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

   }