package id2.id2me.com.id2launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.FolderItemView;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.models.FolderInfo;
import id2.id2me.com.id2launcher.models.ItemInfo;
import id2.id2me.com.id2launcher.models.ShortcutInfo;

/**
 * Created by CrazyInnoTech on 16-12-2016.
 */

public class FolderIcon extends LinearLayout implements FolderInfo.FolderListener {
    private static final int TOTAL_NUM_ITEMS_IN_PREVIEW = 9;
    private static final int NUM_ITEMS_IN_ROW = 3;
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
    //private Folder mFolder;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private FolderItemView mFolder;

    public FolderIcon(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ArrayList<View> items = mFolder.getItemsInReadingOrder();

        Drawable d;
        ImageView v;
        int counter = 0;
        v = (ImageView) items.get(0);
        d = v.getDrawable();
        computePreviewDrawingParams(d);

        float transX = 0;
        float transY = 0;

        for(int i = 0 ; i < NUM_ITEMS_IN_ROW ; i++) {
            transX = mBaselineIconSize * i;
            for(int j = 0 ; j < NUM_ITEMS_IN_ROW ; j++) {
                transY = mBaselineIconSize * j;
                v = (ImageView) items.get(counter);
                d = v.getDrawable();
                mParams = computePreviewItemDrawingParams(transX, transY ,mParams);
                mParams.drawable = d;
                drawPreviewItem(canvas, mParams);
                counter++;
            }
        }
    }

    @Override
    public void onAdd(ShortcutInfo item) {

    }

    @Override
    public void onRemove(ShortcutInfo item) {

    }

    @Override
    public void onTitleChanged(CharSequence title) {

    }

    @Override
    public void onItemsChanged() {

    }

    @Override
    public void addItem(ShortcutInfo destInfo) {

    }

    public void onDragExit(Object o) {

    }

    public boolean acceptDrop(Object dragInfo) {
        return false;
    }

    public void performCreateAnimation(ShortcutInfo destInfo, View v, ShortcutInfo sourceInfo, DragView dragView, Rect folderLocation, float scale, Runnable postAnimationRunnable) {
    }


    public void onDrop(DropTarget.DragObject d) {
    }

    public void onDragEnter(ItemInfo info) {

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

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        private CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public Drawable mOuterRingDrawable = null;
        public Drawable mInnerRingDrawable = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();
        //    mOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
            //mInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
//            if (sStaticValuesDirty) {
//                sPreviewSize = res.getDimensionPixelSize(R.dimen.folder_preview_size);
//                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
//                sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
//                sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);
//                sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
//                sStaticValuesDirty = false;
//            }
        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(0f, 1f);
          //  mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                   // mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                   // mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                       // mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(0f, 1f);
        //    mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
              //      mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
               //     mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                   //     mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                     //   mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

}
