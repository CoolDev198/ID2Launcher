package id2.id2me.com.id2launcher;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VerticalPagerAdapter extends PagerAdapter{

    private Context mContext;
    private int mChilds;

    public VerticalPagerAdapter(Context c, int childs){
        mContext = c;
        mChilds = childs;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LinearLayout linear = new LinearLayout(mContext);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setGravity(Gravity.CENTER);
        linear.setBackgroundColor(Color.GRAY);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);

        TextView tvChild = new TextView(mContext);
        tvChild.setGravity(Gravity.CENTER_HORIZONTAL);
        tvChild.setText("Desktop " + (position+1));
        tvChild.setTextColor(Color.BLACK);
        tvChild.setTextSize(30);
        linear.addView(tvChild);

        container.addView(linear);
        return linear;
    }



    public float getPageWidth(int position) {
        return 1.f;
    }

    public float getPageHeight(int position) {
        return 1.f;
    }
}
