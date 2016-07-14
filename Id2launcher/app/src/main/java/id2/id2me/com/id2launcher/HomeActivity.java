package id2.id2me.com.id2launcher;

import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.adapters.HorizontalPagerAdapter;
import id2.id2me.com.id2launcher.database.AppInfo;
import id2.id2me.com.id2launcher.database.DataBaseHandler;

public class HomeActivity extends AppCompatActivity {
    HorizontalPagerAdapter pageAdapter;
    private List<String>fragmentNames;
    public  static  int folderincr =0;

    public static ArrayList<String> mNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home);
            getSupportActionBar().hide();
           // setWallpaper();

            DataBaseHandler.dataBaseHandler = new DataBaseHandler(this);

            Log.v("gggggg>>>",this.getDatabasePath("launcherSettings").getAbsolutePath());

            List<Fragment> fragments = getFragments();
            pageAdapter = new HorizontalPagerAdapter(getSupportFragmentManager(), fragments,fragmentNames);
            ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
            pager.setAdapter(pageAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ll.setBackground(wallpaperDrawable);
        }
    }


    private List<Fragment> getFragments(){
        List<Fragment> fList = null;
        try {
            fList = new ArrayList<Fragment>();
            fragmentNames = new ArrayList<>();
            fragmentNames.add("Desktop");
            fList.add(FirstFragment.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fList;
    }
}
