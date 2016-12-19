package id2.id2me.com.id2launcher.itemviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;

import id2.id2me.com.id2launcher.R;

/**
 * Created by CrazyInnoTech on 16-12-2016.
 */

public class FolderIcon extends LinearLayout {
    private static final float PERSPECTIVE_SCALE_FACTOR = 1f;
    private static final float PERSPECTIVE_SHIFT_FACTOR = 1f;
    private float mMaxPerspectiveShift;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private float mBaselineIconScale;
    private int mIntrinsicIconSize;
    private int mTotalWidth = -1;
    private Drawable drawable;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);

    public FolderIcon(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        Drawable d = ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher);
        computePreviewDrawingParams(d);

        float transX = 0;
        float transY = 0;

        for(int i = 0 ; i < 3 ; i++) {
            transX = mBaselineIconSize * i;
            for(int j = 0 ; j < 3 ; j++) {
                transY = mBaselineIconSize * j;
                mParams = computePreviewItemDrawingParams(transX, transY ,mParams);
                mParams.drawable = d;
                drawPreviewItem(canvas, mParams);
            }
        }
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;

    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;

            final int previewSize = getResources().getDimensionPixelSize(R.dimen.folder_preview_size);
            final int previewPadding = getResources().getDimensionPixelSize(R.dimen.folder_preview_padding);

            mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
            // cos(45) = 0.707  + ~= 0.1) = 0.8f
            int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

            int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));
            mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

            mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
            mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

            //mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
            mPreviewOffsetX = previewPadding;
            mPreviewOffsetY = previewPadding;
        }
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(float transX , float transY,
                                                                     PreviewItemDrawingParams params) {

        float r = 0.75f;
        float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

        // We want to imagine our coordinates from the bottom left, growing up and to the
        // right. This is natural for the x-axis, but for the y-axis, we have to invert things.
        /*float transY = mAvailableSpaceInPreview - (offset + scaledSize + scaleOffsetCorrection);
        float transX = offset + scaleOffsetCorrection;*/

        float totalScale = mBaselineIconScale * scale;
        final int overlayAlpha = (int) (80 * (1 - r));

        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
        } else {
            params.transX = transX;
            params.transY = transY;

            params.scale = totalScale;
            params.overlayAlpha = overlayAlpha;
        }

        return params;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            d.setFilterBitmap(true);
            d.setColorFilter(Color.argb(params.overlayAlpha, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
            d.draw(canvas);
            d.clearColorFilter();
            d.setFilterBitmap(false);
        }
        canvas.restore();
    }


}
