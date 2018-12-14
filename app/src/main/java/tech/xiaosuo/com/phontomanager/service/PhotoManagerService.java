package tech.xiaosuo.com.phontomanager.service;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import cn.bmob.v3.listener.UploadFileListener;

import tech.xiaosuo.com.phontomanager.PhotoRecylerAdapter;
import tech.xiaosuo.com.phontomanager.R;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.PhotoInfoTable;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.database.DataBaseHelper;
import tech.xiaosuo.com.phontomanager.tools.DBUtils;
import tech.xiaosuo.com.phontomanager.tools.Utils;

/**
 * Created by wangshumin on 6/13/2018.
 */

public class PhotoManagerService extends Service {
    private static final String TAG = "PhotoManagerService";
    private final IBinder binder = new LocalBinder();
    Context mContext;
    DataBaseHelper mDbHelper;
    RecyclerView mRecyclerView;
    PhotoRecylerAdapter mRecylerAdapter;
    NotificationCompat.Builder notificationBuilder = null;
    public static int STATUS_NORMAL = 0;
    public static int STATUS_UPLOADING = 1;
    public static int STATUS_DONE = 2;
    int uploadStatus = STATUS_NORMAL;
    Handler mHandler;

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public  class LocalBinder extends Binder {
        public PhotoManagerService getService(){
            return PhotoManagerService.this;
        }
    }//LocalBinder

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }


    /**
     * only upload file,amd insert user info
     *
     */
    public  void uploadOneFile(final ImageInfo imageInfo, final int postion){

        final ImageView imageSyncView;
        final CheckBox checkBox;
        if(imageInfo == null || postion == -1 || getUploadStatus() == STATUS_UPLOADING){
            Log.d(TAG," uploadOneFile  imageInfo is null");
            resetMainFabButton();
            return;
        }

        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(postion);
        if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
            imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
            checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
            imageSyncView.setImageResource(android.R.drawable.stat_notify_sync);
            imageSyncView.setVisibility(View.VISIBLE);
            //animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
        }else{
            imageSyncView = null;
            checkBox = null;
            Log.d(TAG," uploadOneFile  imageSyncView is null");
            resetMainFabButton();
            return;
        }
        //setUploadStatus(STATUS_UPLOADING);
        final ObjectAnimator animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
        animator.start();

        File picFile = Utils.getPhoneImageFile(imageInfo);
        final BmobFile bmobFile = new BmobFile(picFile);
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Log.d(TAG," uploadOneFile the picfile upload success :");
                    Log.d(TAG," uploadOneFile the picfile url:" + bmobFile.getFileUrl());
                    BmobFile file = new BmobFile(bmobFile.getFilename(),bmobFile.getGroup(),bmobFile.getFileUrl());
                    imageInfo.setFile(file);
                    insertBombObject(imageInfo);
                    Toast.makeText(mContext,R.string.upload_success,Toast.LENGTH_SHORT).show();
                    endAnimator(animator,checkBox,imageSyncView);
                    stopForegroundNotification();
                }else{
                    Toast.makeText(mContext,R.string.upload_fail,Toast.LENGTH_SHORT).show();
                    Log.d(TAG," uploadOneFile the pic file upload fail :" + e.getMessage());
                }
                resetMainFabButton();
               // setUploadStatus(STATUS_NORMAL);
            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
                startForegroundNotification(value);
                Log.d(TAG," uploadOneFile the picfile upload pecent value :" + value);
            }
        });
    }


    /**
     *
     * insert object to bmob
     * @param obj
     */
    private void insertBombObject(final ImageInfo obj){

        obj.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                if(e==null){
                    Log.d(TAG, " insertBombObject success");
                    String imgName = obj.getDisplayName();
                    String data = obj.getData();
                    String md5 = obj.getMd5();
                    int cloud = 1;
                    if(mRecylerAdapter != null){
                        mRecylerAdapter.saveUploadStatus(md5);
                    }

                    SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
                    if(DBUtils.isExistInDb(sqLiteDatabase,md5,data)){
                        Log.d(TAG," insertBombObject ,no not insert data into database,because has exist in db: ");
                        return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(PhotoInfoTable.COLUMN_IMAGE_NAME,imgName);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_DATA,data);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_MD5,md5);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD,cloud);
                    long id = sqLiteDatabase.insert(PhotoInfoTable.PHOTO_INFO_TABLE,null,values);
                    Log.d(TAG," insertBombObject to insert phone database,the insert id is: " + id);
                    //  toast("添加数据成功，返回objectId为："+objectId);
                }else{
                    Log.d(TAG, " insertBombObject fail  " + e.getMessage());
                    Toast.makeText(mContext,"insert obj fail," + e.toString(),Toast.LENGTH_SHORT).show();
                    //  toast("创建数据失败：" + e.getMessage());
                }
            }
        });
    }


    /**
     * set the dbhelper from mainactivity.
     * @param mDbHelper
     */
    public void setmDbHelper(DataBaseHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    /**
     *
     * @param mRecyclerView
     */
    public void setmRecyclerView(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    public void setmRecylerAdapter(PhotoRecylerAdapter mRecylerAdapter) {
        this.mRecylerAdapter = mRecylerAdapter;
    }

    /**
     *upload photos in activity ,not use photo manager
     * upload multi photoes to bmob.
     * @param context
     * @param indexMap the indexMap contains recycler itemn postion and ImageInfo. we need the postion to get view,then update view.
     */
    public void uploadMultiFiles(final Context context, final HashMap<Integer, ImageInfo>  indexMap){
        boolean exist = false;
         final HashMap<String,Integer> pathPositionMap = new HashMap<String,Integer>();
         List<Integer> positions = new ArrayList<Integer>();
        if(indexMap == null){
            Log.d(TAG," upload muti files fail,indexMap is null");
            resetMainFabButton();
            return;
        }
        Log.d(TAG," upload muti files begin");
        for(final Map.Entry<Integer, ImageInfo> entry : indexMap.entrySet()){
            ImageInfo imageInfo = entry.getValue();
             exist = DBUtils.isExistInDb(mDbHelper.getReadableDatabase(),imageInfo.getMd5(),imageInfo.getData());
            if(exist){
                positions.add(entry.getKey());
            }
        }


        for(int i=0 ;i<positions.size();i++){
            int itemPosition = positions.get(i);
            indexMap.remove(itemPosition);//update the indexmap , for upload the unupload photo
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
            if(holder instanceof PhotoRecylerAdapter.PhotoHolder){//update the uploaded phohto ui
                CheckBox itemCheckBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                if(itemCheckBox != null){
                    itemCheckBox.setChecked(false);
                }
            }
            // mRecylerAdapter.removeValueFromMap(i);
        }
        Log.d(TAG," upload muti files,need upload photo count:" + indexMap.size());
        if(indexMap.size() == 0){
            Intent intent = new Intent();
            intent.setAction(Utils.ACTION_UNSELECT_ALL);
            mContext.sendBroadcast(intent);
           // mRecylerAdapter.unselectedAll();
           // mRecylerAdapter.notifyDataSetChanged();
            Toast.makeText(mContext, R.string.pls_select_unuplosd_photo,Toast.LENGTH_SHORT).show();
            resetMainFabButton();
            return;
        }
        //newIndexMap = indexMap
        // sort hash map
        List<Map.Entry<Integer, ImageInfo>> sortList = new ArrayList<Map.Entry<Integer, ImageInfo>>(indexMap.entrySet());
        Collections.sort(sortList, new Comparator<Map.Entry<Integer, ImageInfo>>() {
            @Override
            public int compare(Map.Entry<Integer, ImageInfo> o1, Map.Entry<Integer, ImageInfo> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        ArrayList<String> arrayList = new ArrayList<String>();
        int total = indexMap.size();
        for(Map.Entry<Integer, ImageInfo> entry : sortList){
            ImageInfo imgInfo =  entry.getValue();
            String path = imgInfo.getData();
            arrayList.add(path);
            pathPositionMap.put(path,entry.getKey());
        }

        final String[] pathArray = new String[total];
        arrayList.toArray(pathArray);
       // setUploadStatus(STATUS_UPLOADING);
        BmobFile.uploadBatch(pathArray, new UploadBatchListener() {
            ImageView imageSyncView;
            CheckBox checkBox;
            int currentPosition = -1;
            int currentIndex = -1;
            ObjectAnimator animator;
            @Override
            public void onSuccess(List<BmobFile> files,List<String> urls) {
                //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                //2、urls-上传文件的完整url地址
                // Log.d(TAG," uploadMultiFiles success files.size " + files + " urls size: " + urls.size());
                int index = currentIndex;

                ImageInfo imageInfo = indexMap.get(currentPosition);
                BmobFile file = files.get(index);
                Log.d(TAG," uploadMultiFiles success files.size " + file.getFileUrl() + " url : " + urls.get(index));
                //   file.setUrl(urls.get(index));
                imageInfo.setFile(file);
                insertBombObject(imageInfo);
                Log.d(TAG," upload multi photo success end animator");
                endAnimator(animator,checkBox,imageSyncView);
                Utils.removeKeyFromSelectedMap(currentPosition,mRecylerAdapter);
                Log.d(TAG," upload multi photo set sys upload img ok");
                if(urls.size()==pathArray.length){//如果数量相等，则代表文件全部上传完成
                    stopForegroundNotification();
                    resetMainFabButton();
                    Toast.makeText(context,R.string.multi_upload_success,Toast.LENGTH_SHORT).show();
                    //do something
                    // Log.d(TAG," uploadMultiFiles success ");
                }
               // setUploadStatus(STATUS_NORMAL);
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                Log.d(TAG," uploadMultiFiles fail,statuscode:" + statuscode + " errormsg:" + errormsg );
                Toast.makeText(context,statuscode + errormsg,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）
                currentIndex = curIndex - 1;
                int position = pathPositionMap.get(pathArray[currentIndex]);
                if(currentPosition != position){
                    currentPosition = position;
                    RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(currentPosition);
                    if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
                        imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
                        checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                        Log.d(TAG," upload multi photo show animator");
                        imageSyncView.setVisibility(View.VISIBLE);
                        checkBox.setChecked(false);
                        animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
                        animator.start();
                    }
                }
                startForegroundNotificationMulti(totalPercent,curIndex,total);
/*                if(notificationBuilder != null){
                    notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
   *//*                 Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                    notificationBuilder.setLargeIcon(bitmapDrawable.getBitmap());*//*
                    notificationBuilder.setContentTitle(getString(R.string.app_name));
                    notificationBuilder.setContentText(getString(R.string.uploading_notification) + String.valueOf(totalPercent) + " " + getString(R.string.file_number) + curIndex + "/" + total);
                    notificationBuilder.setProgress(100,totalPercent,false);
                    Notification notification = notificationBuilder.build();
                    startForeground(1,notification);
                }*/
                Log.d(TAG," uploadMultiFiles onProgress curPercent: " + curPercent + "  curIndex: " + curIndex + " currentPosition: " + currentPosition + " totalPercent:" + totalPercent + " total " + total);
            }
        });
    }


    /**
     *  end the ObjectAnimator animation
     *
     */
    private void endAnimator(ObjectAnimator animator,CheckBox checkBox,ImageView imageSyncView){
        if(animator != null){
            animator.end();
            if(checkBox != null){
                Log.d(TAG," endAnimator checkBox set false");
                checkBox.setChecked(false);
            }
            if(imageSyncView != null) {
                Log.d(TAG," endAnimator photo set sys upload img");
                imageSyncView.setImageResource(android.R.drawable.stat_sys_upload);
            }
            Log.d(TAG," endAnimator ok.");
        }
    }

    /**
     * for upload only one photo file.
     * @param percent
     */
    private void startForegroundNotification(int percent){
        startForegroundNotificationMulti(percent,1,1);
    }

    /**
     *
     * @param percent
     * @param curIndex
     * @param total
     * //@param isMulti  -> flat that we is uploading muti photos.
     */
    private void startForegroundNotificationMulti(int percent,int curIndex,int total){
        if(notificationBuilder == null){
            notificationBuilder = new NotificationCompat.Builder(mContext);;
        }
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
   /*                 Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                    notificationBuilder.setLargeIcon(bitmapDrawable.getBitmap());*/
            notificationBuilder.setContentTitle(getString(R.string.app_name));
            notificationBuilder.setContentText(getString(R.string.uploading_notification) + String.valueOf(percent) + "% " + getString(R.string.file_number) + curIndex + "/" + total);
            notificationBuilder.setProgress(100,percent,false);
            Notification notification = notificationBuilder.build();
            startForeground(1,notification);
    }

    /**
     * remove the notification.
     */
    private void stopForegroundNotification(){
        stopForeground(true);
    }


    /**
     *
     */
    private void resetMainFabButton(){
        Message msg = new Message();
        msg.what = Utils.MSG_UPLOAD_FINISH;
        mHandler.sendMessage(msg);
    }
}
