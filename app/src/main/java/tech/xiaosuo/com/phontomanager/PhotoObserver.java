package tech.xiaosuo.com.phontomanager;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/**
 * Created by wangshumin on 6/12/2018.
 */

public class PhotoObserver extends ContentObserver {
    private final String TAG = "PhotoObserver";
    Context context;
    PhotoRecylerAdapter adapter;
    Handler handler;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public PhotoObserver(Context context, Handler handler, PhotoRecylerAdapter adapter) {
        super(handler);
        this.context = context;
        this.adapter = adapter;
        this.handler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG," photoobserver onchange ");
        ScanPhotosAsyncTask scanPhotosAsyncTask = new ScanPhotosAsyncTask(context,adapter,handler,null);
        scanPhotosAsyncTask.execute();
    }
}
