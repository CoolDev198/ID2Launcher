package id2.id2me.com.id2launcher.itemviews;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id2.id2me.com.id2launcher.DragSource;
import id2.id2me.com.id2launcher.IconCache;
import id2.id2me.com.id2launcher.LauncherApplication;
import id2.id2me.com.id2launcher.R;
import id2.id2me.com.id2launcher.models.ShortcutInfo;
import id2.id2me.com.id2launcher.models.AppInfo;

/**
 * Created by sunita on 11/2/16.
 */

public class AppItemView extends LinearLayout  {

     AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void setTextVisibility(int value){
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        if(textView!=null)
            textView.setVisibility(value);
    }


    public void setAppInfoModel(AppInfo appInfoModel) {
        ImageView imageView = (ImageView) findViewById(R.id.drawer_grid_image);
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        if(textView!=null)
        textView.setText(appInfoModel.title);
        if(imageView!=null)
        imageView.setImageBitmap(appInfoModel.iconBitmap);
        this.setTag(appInfoModel);
    }
    public void setShortCutModel(ShortcutInfo shortCutModel) {
        LauncherApplication launcherApplication=LauncherApplication.getApp();
        ImageView imageView = (ImageView) findViewById(R.id.drawer_grid_image);
        TextView textView =(TextView)findViewById(R.id.drawer_grid_text) ;
        if(textView!=null)
            textView.setText(shortCutModel.title);
        if(imageView!=null)
            imageView.setImageBitmap(shortCutModel.getIcon(launcherApplication.mIconCache));
        this.setTag(shortCutModel);
    }

}
