package id2.id2me.com.id2launcher.drawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id2.id2me.com.id2launcher.R;

/**
 * Created by bliss76 on 21/06/16.
 */
public class SideBar extends View
{
    public static List<Character> mLetter = null;
    List<Object> symbols;
    Canvas canvas;
    private ListView list;
    private SectionIndexer sectionIndexter;
    private TextView mDialogText;
    private WindowManager windowManager;
    private int position;
    private DrawerLayout drawer;
   // private SearchBox searchBox;

    public SideBar(Context context) {
        super(context);
        init();
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
    }

    void setScroll() {
        list.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                return false;
            }
        });
        list.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


                try {
                    TextView textView = (TextView) view
                            .findViewById(R.id.txt_symbol);
                    if (textView != null) {
                        if (!textView.getText().toString().isEmpty()) {
                            textView.getText().toString().charAt(0);
                            invalidate();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.TRANSPARENT);

        try {
            this.canvas = canvas;
            Paint paint = new Paint();
            int textHeight = 0;

            if (list != null) {

                if (mLetter != null) {

                    if (mLetter.size() > 0) {
                        textHeight = (getHeight() / mLetter.size()) - 9;
                    }
                    final int width = getWidth();
                    int visibleChildCount = (list.getLastVisiblePosition() - list
                            .getFirstVisiblePosition()) + 1;
                    int k = 0;
                    int pos = 0;
                    //if (searchBox.getText().toString().isEmpty()) {
                    pos = list.getFirstVisiblePosition();
//			} else {
//				pos = searchBox.getPosition();
//			}
                    for (int i = 0; i < mLetter.size(); i++) {

                        if (i == pos) {
                            paint.setColor(Color.WHITE);
                            //if (searchBox.getText().toString().isEmpty()) {
                            k++;
                            if (k < visibleChildCount && (i + 1) < mLetter.size()) {
                                pos = i + 1;
                            }
                            // }
                        } else {
                            paint.setColor(getResources().getColor(
                                    R.color.symbol_circle_color));
                        }
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setAntiAlias(true);
                        paint.setTextSize(22);
                        final float xPos = width / 2
                                - paint.measureText(String.valueOf(mLetter.get(i))) / 2;

                        final float yPos = textHeight * i + textHeight;

                        canvas.drawText(String.valueOf(mLetter.get(i)), xPos, yPos,
                                paint);
                        paint.reset();
                    }
                } else {
                    Log.v("letter Exception", "letter Exception");
                }
            } else {
                Log.v("letter Exception", "letter Exception");
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float y = (int) event.getY();

        try {
            if (drawer != null) {
                if (drawer.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                }
            }

            int idx = (int) (y / getHeight() * mLetter.size());

            if (idx >= mLetter.size()) {
                if (mDialogText != null)
                    mDialogText.setVisibility(INVISIBLE);
            } else if (idx < 0) {
                if (mDialogText != null)
                    mDialogText.setVisibility(INVISIBLE);
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                y = (int) event.getY();

                if (idx >= 0 && idx < mLetter.size()) {
                    mDialogText.setVisibility(View.VISIBLE);
                    mDialogText.setText(String.valueOf(mLetter.get(idx)));
                    mLetter.get(idx);

                    if (sectionIndexter == null) {
                        sectionIndexter = (SectionIndexer) list.getAdapter();
                    }

                    position = idx;//sectionIndexter.getPositionForSection(mLetter
                    //.get(idx));
                    //  position=3;
                    try {
                        updateOverlayTextViewToWindow(0, (y - getResources()
                                .getDimension(R.dimen.indicator_y_diff)));
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    if (idx == -1) {
                        return true;
                    }

                    list.setSelection(idx);

                    invalidate();

                } else {
                    mDialogText.setVisibility(INVISIBLE);
                }

            } else {
                try {
                    if (drawer != null) {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                    mDialogText.setVisibility(INVISIBLE);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return true;
    }

    public void reDrawSideBar() {
        invalidate();
    }

    public void updateOverlayTextViewToWindow(float xPos, float yPos) {
        try {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);

            lp.x = (int) (list.getLeft() + this.getWidth()) - 30;
            lp.y = (int) yPos;

            windowManager.updateViewLayout(mDialogText, lp);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void setListView(ListView _list) {
        list = _list;
        sectionIndexter = (SectionIndexer) _list.getAdapter();
        setScroll();
    }

    public void setSideBarLetters(List<Object> list) {
        if (mLetter==null) {
            mLetter = new ArrayList<Character>();
            for (int i = 0; i < list.size(); i++) {
                mLetter.add((list.get(i).toString()).charAt(0));
            }
        }
    }

    public void setDrawer(DrawerLayout drawer) {
        this.drawer = drawer;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void setTextView(TextView mDialogText) {
        this.mDialogText = mDialogText;
    }

    /*public void setSearchBox(SearchBox searchBox) {

        this.searchBox = searchBox;

    }*/
}
