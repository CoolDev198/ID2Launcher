package id2.id2me.com.id2launcher.wallpaperEditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.isseiaoki.simplecropview.util.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id2.id2me.com.id2launcher.R;


public class ResultActivity extends FragmentActivity {
    private static final String TAG = ResultActivity.class.getSimpleName();
    private ImageView mImageView;
    private ExecutorService mExecutor;

    public static Intent createIntent(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.setData(uri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // apply custom font
        FontUtils.setFont((ViewGroup) findViewById(R.id.layout_root));


    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }


}
