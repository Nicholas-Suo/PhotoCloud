package tech.xiaosuo.com.phontomanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;

/**
 * Created by wangshumin on 5/28/2018.
 */

public class ImageResizer {
    public static final String TAG = "ImageResizer";
    public static Bitmap decodeBitmapFromResource(Resources rs,int resId,int reqWidth,int reqHeight){
        Bitmap bitmap = null;
        int sample = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(rs,resId,options);
        options.inSampleSize = getSampleValue(reqWidth,reqHeight,options.outWidth,options.outHeight);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(rs,resId,options);
        return bitmap;
    }

    public static Bitmap decodeBitmapFromFile(String path, int reqWidth, int reqHeight){
        Bitmap bitmap = null;
        int sample = 1;
        Log.d(TAG," reqWidth: " + reqWidth + " reqHeight: " + reqHeight);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = getSampleValue(reqWidth,reqHeight,options.outWidth,options.outHeight);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor descriptor, int reqWidth, int reqHeight){
        Bitmap bitmap = null;
        int sample = 1;
        Log.d(TAG," reqWidth: " + reqWidth + " reqHeight: " + reqHeight);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(descriptor,null,options);
        options.inSampleSize = getSampleValue(reqWidth,reqHeight,options.outWidth,options.outHeight);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFileDescriptor(descriptor,null,options);
        return bitmap;
    }


    public static int getSampleValue(int reqWidth,int reqHeiht,int sWidth,int sHeight){
         int sample = 1;

         while (sWidth >= reqWidth ||sHeight >=  reqHeiht){
             sample*=2;
             sWidth = sWidth/sample;
             sHeight = sHeight/sample;
         }
        Log.d(TAG," the sample value is: " + sample);
        return sample;
    }

    public static float dp2px(Context context,int dp){
          float scale = context.getResources().getDisplayMetrics().density;
          Log.d(TAG," the phone dp is: "+scale);
          return (dp*scale + 0.5f);
    }
}
