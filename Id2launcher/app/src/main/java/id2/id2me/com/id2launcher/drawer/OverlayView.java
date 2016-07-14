package id2.id2me.com.id2launcher.drawer;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import id2.id2me.com.id2launcher.R;

/**
 * Created by bliss76 on 21/06/16.
 */
public class OverlayView {
    @SuppressLint("InflateParams")
    public static View initOverlay(LayoutInflater layoutInflater,
                                   WindowManager windowManager) {
        TextView overlayTextView = (TextView) layoutInflater.inflate(
                R.layout.selection_overlay_textview, null);
        overlayTextView.setVisibility(View.INVISIBLE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(overlayTextView, lp);

        return overlayTextView;
    }
}
