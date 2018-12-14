package tech.xiaosuo.com.phontomanager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import tech.xiaosuo.com.phontomanager.tools.DBUtils;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;

/**
 * Created by wangshumin on 5/28/2018.
 */

public class ScanPhotosAsyncTask extends AsyncTask<Void,Integer,List<ImageInfo>> {

    Context context;
    PhotoRecylerAdapter adapter;
    Handler mHandler;
    ContentLoadingProgressBar progressBar;
    ScanPhotosAsyncTask(Context context, PhotoRecylerAdapter adapter, Handler mHandler, ContentLoadingProgressBar progressBar){
        this.context = context;
        this.adapter = adapter;
        this.mHandler = mHandler;
        this.progressBar = progressBar;
    }
    @Override
    protected List<ImageInfo> doInBackground(Void... params) {
        return DBUtils.scanImageFromPhone(context,mHandler);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
       if(progressBar != null && !progressBar.isShown()){
            progressBar.show();
        }
/*        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.alert);
        progressDialog.setMessage(context.getString(R.string.scanning));
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();*/
    }

    @Override
    protected void onPostExecute(List<ImageInfo> imageInfos) {
        super.onPostExecute(imageInfos);
        if(progressBar != null && progressBar.isShown()){
            progressBar.hide();
            ((LinearLayout)progressBar.getParent()).setVisibility(View.GONE);
        }
        adapter.setPhotoData(imageInfos);
    }


}
