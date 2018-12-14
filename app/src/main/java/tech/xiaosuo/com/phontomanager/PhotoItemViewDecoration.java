package tech.xiaosuo.com.phontomanager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by wangshumin on 5/28/2018.
 */

public class PhotoItemViewDecoration extends RecyclerView.ItemDecoration {
    private static final  String TAG = "PhotoItemViewDecoration";
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        /*Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setColor(parent.getContext().getResources().getColor(R.color.colorPrimary));


        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            if (i == 0) {
                continue;
            }

            View childView = parent.getChildAt(i);


            float x = childView.getX();
            float y = childView.getY();
            int width = childView.getWidth();
            int height = childView.getHeight();


            c.drawLine(x, y, x + width, y, paint);
            c.drawLine(x, y, x, y + height, paint);
            c.drawLine(x + width, y, x + width, y + height, paint);
            c.drawLine(x, y + height, x + width, y + height, paint);
        }*/
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        Log.d(TAG," outRect.left " + outRect.left + " outRect.right " + outRect.right + " outRect.bottom " + outRect.bottom + " outRect.top " + outRect.top + " parent.getChildAdapterPosition(view) " + parent.getChildAdapterPosition(view));
        outRect.top = 4;

       // outRect.bottom = 10;
        int currPosition = parent.getChildAdapterPosition(view);
        if( currPosition%3 == 0){//totoal 3 column,the right need the magin.
            outRect.left = 0;
        }
        if((currPosition - 1)%3 == 0){//totoal 3 column,the right need the magin.
            outRect.left = 1;
        }
        if((currPosition + 1)%3 == 0){//totoal 3 column,the right need the magin.
            outRect.left = 2;
        }

    }
}

/*
ldpi文件夹下对应的密度为120dpi,对应的分辨率为240*320
        mdpi文件夹下对应的密度为160dpi,对应的分辨率为320*480
        hdpi文件夹下对应的密度为240dpi,对应的分辨率为480*800
        xhdpi文件夹下对应的密度为320dpi,对应的分辨率为720*1280
        xxhdpi文件夹下对应的密度为480dpi,对应的分辨率为1080*1920

        由于各种屏幕密度的不同，导致了同一张图片在不同的手机屏幕上显示不同；在屏幕大小相同的情况下，高密度的屏幕包含了更多的像素点。android系统将密度为160dpi的屏幕作为标准对于mdpi文件夹，在此屏幕的手机上1dp=1px。从上面系统屏幕密度可以得出各个密度值之间的换算；在mdpi中1dp=1px,在hdpi中1dp=1.5px，在xhdpi中1dp=2px,在xxhpi中1dp=3px。换算比例如下：ldpi:mdpi:hdpi:xhdpi:xxhdpi=3:4:6:8:12。

        单位换算方法*/
